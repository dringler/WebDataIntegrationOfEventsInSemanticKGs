/** 
 *
 * Copyright (C) 2015 Data and Web Science Group, University of Mannheim, Germany (code@dwslab.de)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.uni_mannheim.informatik.wdi.usecase.events;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import de.uni_mannheim.informatik.wdi.model.*;
import de.uni_mannheim.informatik.wdi.usecase.events.datafusion.evaluation.*;
import de.uni_mannheim.informatik.wdi.usecase.events.datafusion.fusers.*;
import de.uni_mannheim.informatik.wdi.usecase.events.model.*;
import org.joda.time.DateTime;
import org.xml.sax.SAXException;

import de.uni_mannheim.informatik.wdi.datafusion.CorrespondenceSet;
import de.uni_mannheim.informatik.wdi.datafusion.DataFusionEngine;
import de.uni_mannheim.informatik.wdi.datafusion.DataFusionEvaluator;
import de.uni_mannheim.informatik.wdi.datafusion.DataFusionStrategy;

/**
 * Class containing the standard setup to perform a data fusion task, reading
 * input data from the movie usecase.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * * @author Daniel Ringler
 * 
 */
public class Events_DataFusion_Main {

	public static void main(String[] args) throws XPathExpressionException,
			ParserConfigurationException, SAXException, IOException,
			TransformerException {

		char separator = '+';
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
		LocalDate fromDate = LocalDate.MIN;
		LocalDate toDate = LocalDate.MAX;
		String keyword = "";

		// Load the Data into FusableDataSet
		FusableDataSet<Event, DefaultSchemaElement> fusableDataSetD = new FusableDataSet<>();
		fusableDataSetD.loadFromTSV(new File("WDI/usecase/event/input/dbpedia-1_s.tsv"),
				new EventFactory(), "events/event", separator, dateTimeFormatter, false, fromDate, false, toDate, true, keyword);


		FusableDataSet<Event, DefaultSchemaElement> fusableDataSetY = new FusableDataSet<>();
		fusableDataSetY.loadFromTSV(new File("WDI/usecase/event/input/yago-1_s.tsv"),
				new EventFactory(), "events/event", separator, dateTimeFormatter, false, fromDate, false, toDate, true, keyword);


		FusableDataSet<Event, DefaultSchemaElement> fusedDataSet = runDataFusion(fusableDataSetD,
				fusableDataSetY,
				null,
				separator, dateTimeFormatter, fromDate, toDate, keyword);

	}

	public static FusableDataSet<Event,DefaultSchemaElement> runDataFusion(FusableDataSet<Event, DefaultSchemaElement> fusableDataSetD,
                                                                           FusableDataSet<Event, DefaultSchemaElement> fusableDataSetY,
                                                                           ResultSet<Correspondence<Event, DefaultSchemaElement>> correspondences,
                                                                           char separator,
                                                                           DateTimeFormatter dateTimeFormatter,
                                                                           LocalDate fromDate,
                                                                           LocalDate toDate, String keyword) throws IOException {

		//FusableDataSet<Event, DefaultSchemaElement> fusableDataSetD = (FusableDataSet<Event, DefaultSchemaElement>) dataSetD;
		System.out.println("DBpedia Data Set Density Report:");
		fusableDataSetD.printDataSetDensityReport();

		//FusableDataSet<Event, DefaultSchemaElement> fusableDataSetY = (FusableDataSet<Event, DefaultSchemaElement>) dataSetY;
		System.out.println("YAGO Data Set Density Report:");
		fusableDataSetY.printDataSetDensityReport();

		// Maintain Provenance
		// Scores (e.g. from rating)
		fusableDataSetD.setScore(1.0);
		fusableDataSetY.setScore(1.0);

		// Date (e.g. last update)
		fusableDataSetD.setDate(DateTime.parse("2016-04-01"));
		fusableDataSetY.setDate(DateTime.parse("2015-11-01"));

		CorrespondenceSet<Event, DefaultSchemaElement> correspondencesSet = new CorrespondenceSet<>();
		if (correspondences != null) {
			correspondencesSet
					.loadCorrespondences(
							correspondences,
							fusableDataSetD, fusableDataSetY);
		} else { //load correspondences from disk
			correspondencesSet
					.loadCorrespondences(
							new File(
									"WDI/usecase/event/output/dbpedia_2_yago_correspondences_s.csv"),
							fusableDataSetD, fusableDataSetY);

		}
		correspondencesSet.printGroupSizeDistribution();

		DataFusionStrategy<Event, DefaultSchemaElement> strategy = new DataFusionStrategy<>(
				new EventFactory());

		strategy.addAttributeFuser(new DefaultSchemaElement("Label"), new EventLabelFuserAll(),
				new EventLabelEvaluationRule());
		strategy.addAttributeFuser(new DefaultSchemaElement("Date"), new EventDateFuserAll(),
				new EventDateEvaluationRule());
		strategy.addAttributeFuser(new DefaultSchemaElement("Coordinates"), new EventCoordinatesFuserFirst(),
				new EventCoordinatesEvaluationRule());

		//... all attributes
		//...

		DataFusionEngine<Event, DefaultSchemaElement> engine = new DataFusionEngine<>(strategy);

		// calculate cluster consistency
		engine.printClusterConsistencyReport(correspondencesSet, null);

		// run the fusion
		FusableDataSet<Event, DefaultSchemaElement> fusedDataSet = engine.run(correspondencesSet, null);

		// write the result
		//fusedDataSet.writeCSV(new File("WDI/usecase/event/output/fused.tsv"),new EventCSVFormatter());


		// load the gold standard
		DefaultDataSet<Event, DefaultSchemaElement> gs = new FusableDataSet<>();
		gs.loadFromTSV(new File("../data/fused.tsv"),
				new EventFactory(), "/events/event", separator, dateTimeFormatter, false, fromDate, false, toDate, false, keyword);

		//gs.splitMultipleValues(separator);
		// evaluate
		//DataFusionEvaluator<Movie, DefaultSchemaElement> evaluator = new DataFusionEvaluator<>(
		//		strategy, new RecordGroupFactory<Movie, DefaultSchemaElement>());
		DataFusionEvaluator<Event, DefaultSchemaElement> evaluator = new DataFusionEvaluator<>(
				strategy, new RecordGroupFactory<Event, DefaultSchemaElement>());
		evaluator.setVerbose(true);
		double accuracy = evaluator.evaluate(fusedDataSet, gs, null);

		System.out.println(String.format("Accuracy: %.2f", accuracy));

		return fusedDataSet;
	}
}
