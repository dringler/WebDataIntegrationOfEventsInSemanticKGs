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
import java.time.format.DateTimeFormatter;
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
        //double t = ui.getThreshold();

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
     /*
        for (int i = 10; i >= 0; i--) {
            //double t = + i / 10.0; // [0,1] in 0.1 steps -> mr_gs_levURI.csv
            double t = 0.9 + i / 100.0; // [0.9,1] in 0.01 steps -> mr_gs_levURI-09-1.csv
            String resultString = runMatching(dataSetD, dataSetY, paths, t);
            results.add(resultString);
        }*/

        /*ArrayList<String> results = new ArrayList<>();
        for (int mrC= 1; mrC < 5; mrC++) {
            String resultString = runMatching(dataSetD, dataSetY, paths, mrC);
            results.add(resultString);
        }
        saveResultsToFile("mr_all_5.csv", results);
        */
        int mrC = 4; //0:baseline, 1:rr, 2:rh, 3:lr, 4:lh
        runMatching(dataSetD, dataSetY, paths, mrC);
    }

    private static void saveResultsToFile(String fileName, ArrayList<String> results) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("./out/"+fileName));
        //String header = "t, p, r, f1";
        String header = "t, foundCorrespondences, p, r, f1, numberOfPredicted";
        writer.write(header + "\n");
        for (String result : results) {
            writer.write(result + "\n");
        }
        writer.close();
        System.out.println("results written to " + fileName);
    }

    private static String runMatching(FusableDataSet<Event, DefaultSchemaElement> dataSetD, FusableDataSet<Event, DefaultSchemaElement> dataSetY, String[] paths, int mrC) throws Exception {
        //Matching Rule

        String mr;
        double ft;
        if (mrC==0) {
            //baseline
            ft = 0.96;
            LinearCombinationMatchingRule<Event, DefaultSchemaElement> matchingRule = new LinearCombinationMatchingRule<>(ft); //0.96
            matchingRule.addComparator(new EventURIComparatorLevenshtein(), 1);
            mr = "Baseline Lev on stripedURI with " + ft;

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
            System.out.println("Done with Matching: " + matchingTime + " ms");

            // load the gold standard (test set)
            MatchingGoldStandard gs = new MatchingGoldStandard();
            gs.loadFromTSVFile(new File(paths[2]));

            // evaluate your result
            MatchingEvaluator<Event, DefaultSchemaElement> evaluator = new MatchingEvaluator<Event, DefaultSchemaElement>(true);
            Performance perfTest = evaluator.evaluateMatching(correspondences.get(), gs, false);
            long foundCorrespondences = correspondences.size();
            correspondences = null;

            // print the evaluation result
            System.out.println("DBpedia 2 YAGO for " + mr);
            System.out.println(String.format("Precision: %.4f\nRecall: %.4f\nF1: %.4f\nNumber of predicted: %d",
                    perfTest.getPrecision(), perfTest.getRecall(), perfTest.getF1(), perfTest.getNumberOfPredicted()));
            String resultLine = mr + ", " + foundCorrespondences + ", " + perfTest.getPrecision() + ", " + perfTest.getRecall() + ", " + perfTest.getF1() + ", " + perfTest.getNumberOfPredicted();
            return resultLine;

        } else if (mrC==1) {
            //RR
            //max aggregator
            // (not scaled!) levenshtein on lowercase, striped URI with edit distance: .6308 and w:4
            // (not scaled!) levenshtein on label with edit distance 1.5937 and weight: 9
            ft = 1; //edit distance, 1 (matches) or 0 (non-matches) as similarity value
            double t1 = 0.6308;
            double t2 = 1.5937;
            MaximumCombinationMatchingRule<Event, DefaultSchemaElement> matchingRule = new MaximumCombinationMatchingRule<>(ft);
            matchingRule.addComparator(new EventURIComparatorLevenshteinEditDistance(t1), 4);
            matchingRule.addComparator(new EventLabelComparatorLevenshteinEditDistance(t2), 9);
            mr = "RR editDistance on striped and lowercase URI("+t1+") and edit distance on label("+t2+")";

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
            System.out.println("Done with Matching: " + matchingTime + " ms");

            // load the gold standard (test set)
            MatchingGoldStandard gs = new MatchingGoldStandard();
            gs.loadFromTSVFile(new File(paths[2]));

            // evaluate your result
            MatchingEvaluator<Event, DefaultSchemaElement> evaluator = new MatchingEvaluator<Event, DefaultSchemaElement>(true);
            Performance perfTest = evaluator.evaluateMatching(correspondences.get(), gs, false);
            long foundCorrespondences = correspondences.size();
            correspondences = null;

            // print the evaluation result
            System.out.println("DBpedia 2 YAGO for " + mr);
            System.out.println(String.format("Precision: %.4f\nRecall: %.4f\nF1: %.4f\nNumber of predicted: %d",
                    perfTest.getPrecision(), perfTest.getRecall(), perfTest.getF1(), perfTest.getNumberOfPredicted()));
            String resultLine = mr + ", " + foundCorrespondences + ", " + perfTest.getPrecision() + ", " + perfTest.getRecall() + ", " + perfTest.getF1() + ", " + perfTest.getNumberOfPredicted();
            return resultLine;

        } else if (mrC==2) {
            // RH
            //jaccard on striped URI with sim: .4563
            ft = 0.4563;
            LinearCombinationMatchingRule<Event, DefaultSchemaElement> matchingRule = new LinearCombinationMatchingRule<>(ft);
            matchingRule.addComparator(new EventURIComparatorJaccard(), 1);
            mr = "RH: Jaccard on stripedURI with " + ft;

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
            System.out.println("Done with Matching: " + matchingTime + " ms");

            // load the gold standard (test set)
            MatchingGoldStandard gs = new MatchingGoldStandard();
            gs.loadFromTSVFile(new File(paths[2]));

            // evaluate your result
            MatchingEvaluator<Event, DefaultSchemaElement> evaluator = new MatchingEvaluator<Event, DefaultSchemaElement>(true);
            Performance perfTest = evaluator.evaluateMatching(correspondences.get(), gs, false);
            long foundCorrespondences = correspondences.size();
            correspondences = null;

            // print the evaluation result
            System.out.println("DBpedia 2 YAGO for " + mr);
            System.out.println(String.format("Precision: %.4f\nRecall: %.4f\nF1: %.4f\nNumber of predicted: %d",
                    perfTest.getPrecision(), perfTest.getRecall(), perfTest.getF1(), perfTest.getNumberOfPredicted()));
            String resultLine = mr + ", " + foundCorrespondences + ", " + perfTest.getPrecision() + ", " + perfTest.getRecall() + ", " + perfTest.getF1() + ", " + perfTest.getNumberOfPredicted();
            return resultLine;
        } else if (mrC==3) {
            //LR
            // Levenshtein edit distance on date and labels
            ft = 1.0;
            LinearCombinationMatchingRule<Event, DefaultSchemaElement> matchingRule = new LinearCombinationMatchingRule<>(ft);
            matchingRule.addComparator(new EventLabelComparatorLevenshteinEditDistance(1.1275), 16);
            matchingRule.addComparator(new EventDateComparatorLevenshteinEditDistance(2.2752), 18);
            mr = "LR: Edit Distance on Labels and Dates with " + ft;

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
            System.out.println("Done with Matching: " + matchingTime + " ms");

            // load the gold standard (test set)
            MatchingGoldStandard gs = new MatchingGoldStandard();
            gs.loadFromTSVFile(new File(paths[2]));

            // evaluate your result
            MatchingEvaluator<Event, DefaultSchemaElement> evaluator = new MatchingEvaluator<Event, DefaultSchemaElement>(true);
            Performance perfTest = evaluator.evaluateMatching(correspondences.get(), gs, false);
            long foundCorrespondences = correspondences.size();
            correspondences = null;

            // print the evaluation result
            System.out.println("DBpedia 2 YAGO for " + mr + " with threshold: " + ft);
            System.out.println(String.format("Precision: %.4f\nRecall: %.4f\nF1: %.4f\nNumber of predicted: %d",
                    perfTest.getPrecision(), perfTest.getRecall(), perfTest.getF1(), perfTest.getNumberOfPredicted()));
            String resultLine = mr + ", " + foundCorrespondences + ", " + perfTest.getPrecision() + ", " + perfTest.getRecall() + ", " + perfTest.getF1() + ", " + perfTest.getNumberOfPredicted();
            return resultLine;

        } else if (mrC==4) {
            //LH
            // Levenshtein edit distance on date and date comparator on labels
            ft = 1.0;
            double t1 = 0.7;
            double t2 = 0.463;
            LinearCombinationMatchingRule<Event, DefaultSchemaElement> matchingRule = new LinearCombinationMatchingRule<>(ft);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy");
            matchingRule.addComparator(new EventLabelComparatorDate(t1, formatter), 7);
            matchingRule.addComparator(new EventDateComparatorLevenshteinEditDistance(t2), 13);
            mr = "LR: Edit Distance on Date("+t2+") and Date Comparator on Labels("+t1+") with " + ft;

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
            System.out.println("Done with Matching: " + matchingTime + " ms");

            // load the gold standard (test set)
            MatchingGoldStandard gs = new MatchingGoldStandard();
            gs.loadFromTSVFile(new File(paths[2]));

            // evaluate your result
            MatchingEvaluator<Event, DefaultSchemaElement> evaluator = new MatchingEvaluator<Event, DefaultSchemaElement>(true);
            Performance perfTest = evaluator.evaluateMatching(correspondences.get(), gs, false);
            long foundCorrespondences = correspondences.size();
            correspondences = null;

            // print the evaluation result
            System.out.println("DBpedia 2 YAGO for " + mr);
            System.out.println(String.format("Precision: %.4f\nRecall: %.4f\nF1: %.4f\nNumber of predicted: %d",
                    perfTest.getPrecision(), perfTest.getRecall(), perfTest.getF1(), perfTest.getNumberOfPredicted()));
            String resultLine = mr + ", " + foundCorrespondences + ", " + perfTest.getPrecision() + ", " + perfTest.getRecall() + ", " + perfTest.getF1() + ", " + perfTest.getNumberOfPredicted();
            return resultLine;
        }
        //...

        //MinimumCombinationMatchingRule<Event, DefaultSchemaElement> matchingRule = new MinimumCombinationMatchingRule<>(t); //0.96
        // add comparators
        //matchingRule.addComparator(new EventDateComparator(), 0.2);



        return "";
    }

}
