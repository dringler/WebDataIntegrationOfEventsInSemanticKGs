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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import de.uni_mannheim.informatik.wdi.usecase.events.datafusion.evaluation.*;
import de.uni_mannheim.informatik.wdi.usecase.events.datafusion.fusers.*;
import de.uni_mannheim.informatik.wdi.usecase.events.model.*;
import org.joda.time.DateTime;
import org.xml.sax.SAXException;

import de.uni_mannheim.informatik.wdi.datafusion.CorrespondenceSet;
import de.uni_mannheim.informatik.wdi.datafusion.DataFusionEngine;
import de.uni_mannheim.informatik.wdi.datafusion.DataFusionEvaluator;
import de.uni_mannheim.informatik.wdi.datafusion.DataFusionStrategy;
import de.uni_mannheim.informatik.wdi.model.DefaultDataSet;
import de.uni_mannheim.informatik.wdi.model.DefaultSchemaElement;
import de.uni_mannheim.informatik.wdi.model.FusableDataSet;
import de.uni_mannheim.informatik.wdi.model.RecordGroupFactory;

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

		// Load the Data into FusableDataSet
		/*FusableDataSet<Movie, DefaultSchemaElement> ds1 = new FusableDataSet<>();
		ds1.loadFromXML(new File("usecase/movie/input/academy_awards.xml"),
				new MovieFactory(), "/movies/movie");
		ds1.printDataSetDensityReport();

		FusableDataSet<Movie, DefaultSchemaElement> ds2 = new FusableDataSet<>();
		ds2.loadFromXML(new File("usecase/movie/input/actors.xml"),
				new MovieFactory(), "/movies/movie");
		ds2.printDataSetDensityReport();

		FusableDataSet<Movie, DefaultSchemaElement> ds3 = new FusableDataSet<>();
		ds3.loadFromXML(new File("usecase/movie/input/golden_globes.xml"),
				new MovieFactory(), "/movies/movie");
		ds3.printDataSetDensityReport();

		//FusableDataSet<Event, DefaultSchemaElement> dsD = new FusableDataSet<Event, DefaultSchemaElement>();
		//dsD.loadFromXML();
		*/
		FusableDataSet<Event, DefaultSchemaElement> dataDBpedia = new FusableDataSet<>();
		dataDBpedia.loadFromTSV(new File("WDI/usecase/event/input/dbpedia-1_s.tsv"),
				new EventFactory(), "events/event");
		dataDBpedia.printDataSetDensityReport();

		FusableDataSet<Event, DefaultSchemaElement> dataYAGO = new FusableDataSet<>();
		dataYAGO.loadFromTSV(new File("WDI/usecase/event/input/yago-1_s.tsv"),
				new EventFactory(), "events/event");
		dataYAGO.printDataSetDensityReport();

		// Maintain Provenance
		// Scores (e.g. from rating)
		//ds1.setScore(2.0);
		dataDBpedia.setScore(1.0);
		dataYAGO.setScore(1.0);


		// Date (e.g. last update)
		//ds1.setDate(DateTime.parse("2012-01-01"));
		dataDBpedia.setDate(DateTime.parse("2016-04-01"));
		dataYAGO.setDate(DateTime.parse("2015-11-01"));

		// load correspondences
		//CorrespondenceSet<Movie, DefaultSchemaElement> correspondences = new CorrespondenceSet<>();
		CorrespondenceSet<Event, DefaultSchemaElement> correspondences = new CorrespondenceSet<>();
		correspondences
				.loadCorrespondences(
						new File(
								"WDI/usecase/event/output/dbpedia_2_yago_correspondences_s.csv"),
						dataDBpedia, dataYAGO);
/*		correspondences
				.loadCorrespondences(
						new File(
								"usecase/movie/correspondences/actors_2_golden_globes_correspondences.csv"),
						ds2, ds3);*/

		// write group size distribution
		correspondences.printGroupSizeDistribution();

		// define the fusion strategy
		//DataFusionStrategy<Movie, DefaultSchemaElement> strategy = new DataFusionStrategy<>(
		//		new MovieFactory());
		DataFusionStrategy<Event, DefaultSchemaElement> strategy = new DataFusionStrategy<>(
				new EventFactory());
		// add attribute fusers
		// Note: The attribute name is only used for printing the reports
		 /*strategy.addAttributeFuser(new DefaultSchemaElement("Title"), new TitleFuserShortestString(),
		 new TitleEvaluationRule());
		 strategy.addAttributeFuser(new DefaultSchemaElement("Director"),
		 new DirectorFuserLongestString(), new DirectorEvaluationRule());
		 strategy.addAttributeFuser(new DefaultSchemaElement("Date"), new DateFuserVoting(),
		 new DateEvaluationRule());
		 strategy.addAttributeFuser(new DefaultSchemaElement("Actors"),
		 new ActorsFuserUnion(),
		 new ActorsEvaluationRule());*/
		strategy.addAttributeFuser(new DefaultSchemaElement("Label"), new LabelFuserShortestString(),
				new LabelEvaluationRule());
		strategy.addAttributeFuser(new DefaultSchemaElement("Date"), new EventDateFuserFirst(),
				new EventDateEvaluationRule());
		strategy.addAttributeFuser(new DefaultSchemaElement("Coordinates"), new CoordinatesFuserFirst(),
				new CoordinatesEvaluationRule());


		//... all attributes
		//...

		// create the fusion engine
		DataFusionEngine<Event, DefaultSchemaElement> engine = new DataFusionEngine<>(strategy);

		// calculate cluster consistency
		engine.printClusterConsistencyReport(correspondences, null);

		// run the fusion
		FusableDataSet<Event, DefaultSchemaElement> fusedDataSet = engine.run(correspondences, null);

		// write the result
		//fusedDataSet.writeXML(new File("usecase/movie/output/fused.xml"),
		//		new MovieXMLFormatter());

		fusedDataSet.writeCSV(new File("WDI/usecase/event/output/fused.tsv"),
				new EventCSVFormatter());

		// load the gold standard
		DefaultDataSet<Event, DefaultSchemaElement> gs = new FusableDataSet<>();
		gs.loadFromTSV(new File("WDI/usecase/event/goldstandard/fused.tsv"),
				new EventFactory(), "/events/event");

		// evaluate
		//DataFusionEvaluator<Movie, DefaultSchemaElement> evaluator = new DataFusionEvaluator<>(
		//		strategy, new RecordGroupFactory<Movie, DefaultSchemaElement>());
		DataFusionEvaluator<Event, DefaultSchemaElement> evaluator = new DataFusionEvaluator<>(
				strategy, new RecordGroupFactory<Event, DefaultSchemaElement>());
		evaluator.setVerbose(true);
		double accuracy = evaluator.evaluate(fusedDataSet, gs, null);

		System.out.println(String.format("Accuracy: %.2f", accuracy));

	}

}
