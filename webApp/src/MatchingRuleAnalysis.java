import de.uni_mannheim.informatik.wdi.matching.*;
import de.uni_mannheim.informatik.wdi.matching.blocking.BlockingFunction;
import de.uni_mannheim.informatik.wdi.matching.blocking.MultiBlockingKeyGenerator;
import de.uni_mannheim.informatik.wdi.matching.blocking.MultiKeyBlocker;
import de.uni_mannheim.informatik.wdi.matching.blocking.NoBlocker;
import de.uni_mannheim.informatik.wdi.model.*;
import de.uni_mannheim.informatik.wdi.usecase.events.identityresolution.*;
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
        boolean gsNegativeFiles = ui.getGsWithNegativeUserInput();
        int s = 0; // ui.getSampleSizeUserInput();
        double t = ui.getThreshold();

        //get file paths based on user input
        FileLoader fl = new FileLoader();
        String[] paths = fl.getPaths(testing, gsFiles, gsNegativeFiles);


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

        //BASELINE TESTING
     /*   ArrayList<String> results = new ArrayList<>();
        for (int i = 10; i >= 0; i--) {
            //double t = + i / 10.0; // [0,1] in 0.1 steps -> mr_gs_levURI.csv
            double t = 0.9 + i / 100.0; // [0.9,1] in 0.01 steps -> mr_gs_levURI-09-1.csv
            String resultString = runMatching(dataSetD, dataSetY, paths, t);
            results.add(resultString);
        }*/
        //saveResultsToFile("mr_gs_levURI_09-1.csv", results);

        int mrC = 0;

        String resultString = runMatching(dataSetD, dataSetY, paths, mrC, t);

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

    private static String runMatching(FusableDataSet<Event, DefaultSchemaElement> dataSetD, FusableDataSet<Event, DefaultSchemaElement> dataSetY, String[] paths, int mrC, double t) throws Exception {
        //Matching Rule
        String mr = "";
        double ft;
        if (mrC==0) {
            //baseline
            ft = 0.96;
            LinearCombinationMatchingRule<Event, DefaultSchemaElement> matchingRule = new LinearCombinationMatchingRule<>(ft); //0.96
            matchingRule.addComparator(new EventURIComparatorLevenshtein(), 1);
            mr = "Baseline Lev on stripedURI with " + ft;

        } else if (mrC==1) {
            //RR
            //max aggregator
            // (not scaled!) levenshtein on lowercase, striped URI with edit distance: .6308 and w:4
            // (not scaled!) levenshtein on label with edit distance 1.5937 and weight: 9
            ft = 1; //edit distance, 1 (matches) or 0 (non-matches) as similarity value
            MaximumCombinationMatchingRule<Event, DefaultSchemaElement> matchingRule = new MaximumCombinationMatchingRule<>(ft);
            matchingRule.addComparator(new EventURIComparatorLevenshteinEditDistance(0.6308), 1);
            matchingRule.addComparator(new EventLabelComparatorLevenshteinEditDistance(1.5937), 1);

            mr = "RR editDistance on striped, lowercase URI and edit distance on label " + ft;

        } else if (mrC==2) {
            // RH
            //jaccard on striped URI with sim: .4563
            ft = 0.4563;
            LinearCombinationMatchingRule<Event, DefaultSchemaElement> matchingRule = new LinearCombinationMatchingRule<>(ft);
            matchingRule.addComparator(new EventURIComparatorJaccard(), 1);
            mr = "RH: Jaccard on stripedURI with " + ft;
        } else if (mrC==3) {
            //LR
        } else if (mrC==4) {
            //LH
        }
        //...


        //MinimumCombinationMatchingRule<Event, DefaultSchemaElement> matchingRule = new MinimumCombinationMatchingRule<>(t); //0.96

        // add comparators


        //matchingRule.addComparator(new EventDateComparator(), 0.2);



        MultiBlockingKeyGenerator<Event> tokenizedAttributes = BlockingFunction.getStandardBlockingFunctionAllAttributes();
        MultiKeyBlocker<Event, DefaultSchemaElement> blocker = new MultiKeyBlocker<Event, DefaultSchemaElement>(tokenizedAttributes);
        //NoBlocker<Event, DefaultSchemaElement> blocker = new NoBlocker<>();

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
        System.out.println("DBpedia 2 YAGO for " + mr + " with threshold: " + t);
        System.out
                .println(String.format(
                        "Precision: %.4f\nRecall: %.4f\nF1: %.4f\nNumber of predicted: %d",
                        perfTest.getPrecision(), perfTest.getRecall(),
                        perfTest.getF1(), perfTest.getNumberOfPredicted()));
        String resultLine = t + ", " + perfTest.getPrecision() + ", " + perfTest.getRecall() + ", " + perfTest.getF1();
        return resultLine;
    }

}
