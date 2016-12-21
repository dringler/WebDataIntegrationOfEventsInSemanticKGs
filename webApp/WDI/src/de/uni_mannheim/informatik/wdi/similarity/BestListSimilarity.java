package de.uni_mannheim.informatik.wdi.similarity;


import java.time.LocalDate;
import java.util.List;

/**
 * Created by Daniel Ringler on 21/12/16.
 */
public class BestListSimilarity {



    public double getBestStringSimilarity(SimilarityMeasure sim, List<String> strings1, List<String> strings2) {
        double bestSimilarity = 0.0;
        for (String s1 : strings1) {
            for (String s2 : strings2) {
                double similarity = sim.calculate(s1, s2);
                bestSimilarity = getHighestSimilarity(similarity, bestSimilarity);
            }
        }
        return bestSimilarity;
    }

    public double getBestDatesSimilarity(SimilarityMeasure sim, List<LocalDate> dates1, List<LocalDate> dates2) {
        double bestSimilarity = 0.0;
        for (LocalDate d1 : dates1) {
            for (LocalDate d2 : dates2) {
                double similarity = sim.calculate(d1, d2);
                bestSimilarity = getHighestSimilarity(similarity, bestSimilarity);
            }
        }
        return bestSimilarity;
    }


    private double getHighestSimilarity(double similarity, double bestSimilarity) {
        if (similarity >= bestSimilarity)
            return similarity;
        return bestSimilarity;
    }
}
