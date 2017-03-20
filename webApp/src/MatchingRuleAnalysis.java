import de.uni_mannheim.informatik.wdi.matching.LinearCombinationMatchingRule;
import de.uni_mannheim.informatik.wdi.matching.MatchingEngine;
import de.uni_mannheim.informatik.wdi.matching.MatchingEvaluator;
import de.uni_mannheim.informatik.wdi.matching.blocking.NoBlocker;
import de.uni_mannheim.informatik.wdi.model.*;
import de.uni_mannheim.informatik.wdi.usecase.events.identityresolution.EventDateComparator;
import de.uni_mannheim.informatik.wdi.usecase.events.identityresolution.EventLabelComparatorLevenshtein;
import de.uni_mannheim.informatik.wdi.usecase.events.identityresolution.EventURIComparatorJaccard;
import de.uni_mannheim.informatik.wdi.usecase.events.identityresolution.EventURIComparatorLevenshtein;
import de.uni_mannheim.informatik.wdi.usecase.events.model.Event;
import de.uni_mannheim.informatik.wdi.usecase.events.model.EventFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

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
        boolean gsFiles = ui.getGsUserInput();
        int s = ui.getSampleSizeUserInput();

        //get file paths based on user input
        FileLoader fl = new FileLoader();
        String[] paths = fl.getPaths(testing, gsFiles);


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

        int numberOfRuns = 11;
        ArrayList<String> results = new ArrayList<>();
        for (int i = 10; i >= 0; i--) {
            double t = i / 10.0;
            String resultString = runMatching(dataSetD, dataSetY, paths, t);
            results.add(resultString);
        }

        saveResultsToFile("mr_gs_levURI.csv", results);

    }

    private static void saveResultsToFile(String fileName, ArrayList<String> results) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("./out/"+fileName));
        String header = "t, p, r, f1";
        writer.write(header + "\n");
        for (String result : results) {
            writer.write(result + "\n");
        }
        writer.close();
        System.out.println("results written to " + fileName);
    }

    private static String runMatching(FusableDataSet<Event, DefaultSchemaElement> dataSetD, FusableDataSet<Event, DefaultSchemaElement> dataSetY, String[] paths, double t) throws Exception {
        //run Matching Rule
        LinearCombinationMatchingRule<Event, DefaultSchemaElement> matchingRule = new LinearCombinationMatchingRule<>(
                t); //0.7
        // add comparators
        matchingRule.addComparator(new EventURIComparatorLevenshtein(), 1);
        //matchingRule.addComparator(new EventDateComparator(), 0.2);
        //matchingRule.addComparator(new EventURIComparatorJaccard(), 0.5437);

        NoBlocker<Event, DefaultSchemaElement> blocker = new NoBlocker<>();

        // Initialize Matching Engine
        MatchingEngine<Event, DefaultSchemaElement> engine = new MatchingEngine<>();

        // Execute the matching
        System.out.println("Start Matching");
        long time1 = System.currentTimeMillis();
        ResultSet<Correspondence<Event, DefaultSchemaElement>> correspondences = engine.runIdentityResolution(
                dataSetD, dataSetY, null, matchingRule,
                blocker, true, 0.5);
        long time2 = System.currentTimeMillis();
        double matchingTime = time2-time1;
        System.out.println("Done with Matching: " + matchingTime + " ms for " + dataSetD.getSize() * dataSetY.getSize() + " comparisons.");

        // load the gold standard (test set)
        MatchingGoldStandard gs = new MatchingGoldStandard();
        gs.loadFromTSVFile(new File(paths[2]));

        // evaluate your result
        MatchingEvaluator<Event, DefaultSchemaElement> evaluator = new MatchingEvaluator<Event, DefaultSchemaElement>(true);
        Performance perfTest = evaluator.evaluateMatching(correspondences.get(), gs, false);
        correspondences = null;

        // print the evaluation result
        System.out.println("DBpedia 2 YAGO with Levenshtein on URI. Threshold: " + t);
        System.out
                .println(String.format(
                        "Precision: %.4f\nRecall: %.4f\nF1: %.4f\nNumber of predicted: %d",
                        perfTest.getPrecision(), perfTest.getRecall(),
                        perfTest.getF1(), perfTest.getNumberOfPredicted()));
        String resultLine = t + ", " + perfTest.getPrecision() + ", " + perfTest.getRecall() + ", " + perfTest.getF1();
        return resultLine;
    }

}
