import de.uni_mannheim.informatik.wdi.matching.LinearCombinationMatchingRule;
import de.uni_mannheim.informatik.wdi.matching.MatchingEngine;
import de.uni_mannheim.informatik.wdi.matching.MatchingEvaluator;
import de.uni_mannheim.informatik.wdi.matching.blocking.NoBlocker;
import de.uni_mannheim.informatik.wdi.model.*;
import de.uni_mannheim.informatik.wdi.usecase.events.identityresolution.EventDateComparator;
import de.uni_mannheim.informatik.wdi.usecase.events.identityresolution.EventLabelComparatorLevenshtein;
import de.uni_mannheim.informatik.wdi.usecase.events.identityresolution.EventURIComparatorJaccard;
import de.uni_mannheim.informatik.wdi.usecase.events.model.Event;
import de.uni_mannheim.informatik.wdi.usecase.events.model.EventFactory;

import java.io.File;

/**
 * Created by Daniel on 08/02/17.
 */
public class MatchingRuleAnalysis {
    public static void main(String[] args) throws Exception {
        System.out.println("Matching Rule Analysis");
        //System.out.println("Working Directory = " +System.getProperty("user.dir"));

        //get user input: use sample or full data files?
        UserInput ui = new UserInput();
        boolean testing = ui.getDatasetUserInput();
        int s = ui.getSampleSizeUserInput();

        //get file paths based on user input
        FileLoader fl = new FileLoader();
        String[] paths = fl.getPaths(testing);


        FusableDataSet<Event, DefaultSchemaElement> dataSetD = new FusableDataSet<>();
        FusableDataSet<Event, DefaultSchemaElement> dataSetY = new FusableDataSet<>();

        //load XML data sets
        dataSetD.loadFromXML(new File(paths[0]),
                new EventFactory(null, false, null, false, null, false, ""),
                "events/event");
        dataSetY.loadFromXML(new File(paths[1]),
                new EventFactory(null, false, null, false, null, false, ""),
                "events/event");

        if (s>0) {
            dataSetD.sampleRecords(s);
            dataSetY.sampleRecords(s);
        }

        //run Matching Rule
        LinearCombinationMatchingRule<Event, DefaultSchemaElement> matchingRule = new LinearCombinationMatchingRule<>(
                1); //0.7
        // add comparators
        //matchingRule.addComparator(new EventLabelComparatorLevenshtein(), 0.8);
        //matchingRule.addComparator(new EventDateComparator(), 0.2);
        matchingRule.addComparator(new EventURIComparatorJaccard(), 0.5437);

        NoBlocker<Event, DefaultSchemaElement> blocker = new NoBlocker<>();

        // Initialize Matching Engine
        MatchingEngine<Event, DefaultSchemaElement> engine = new MatchingEngine<>();

        // Execute the matching
        ResultSet<Correspondence<Event, DefaultSchemaElement>> correspondences = engine.runIdentityResolution(
                dataSetD, dataSetY, null, matchingRule,
                blocker);

        // load the gold standard (test set)
        MatchingGoldStandard gs = new MatchingGoldStandard();
        gs.loadFromTSVFile(new File(paths[2]));

        // evaluate your result
        MatchingEvaluator<Event, DefaultSchemaElement> evaluator = new MatchingEvaluator<Event, DefaultSchemaElement>(true);
        Performance perfTest = evaluator.evaluateMatching(correspondences.get(), gs, false);

        // print the evaluation result
        System.out.println("DBpedia <-> YAGO");
        System.out
                .println(String.format(
                        "Precision: %.4f\nRecall: %.4f\nF1: %.4f\nNumber of predicted: %d",
                        perfTest.getPrecision(), perfTest.getRecall(),
                        perfTest.getF1(), perfTest.getNumberOfPredicted()));

    }

}
