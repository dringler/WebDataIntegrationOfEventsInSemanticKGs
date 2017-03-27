package de.uni_mannheim.informatik.wdi.similarity;


import de.uni_mannheim.informatik.wdi.similarity.string.LevenshteinEditDistance;

import java.time.LocalDate;
import java.util.List;

/**
 * Created by Daniel Ringler on 21/12/16.
 */
public class BestListSimilarity {

    /**
     * Compare all strings and return best sim score
     */
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

    /**
     * Stripe prefix for URIs and return best sim score
     */
    public double getBestStripedStringSimilarity(SimilarityMeasure sim, List<String> strings1, List<String> strings2) {
        double bestSimilarity = 0.0;
        for (String s1 : strings1) {
            s1 = stripURIPrefix(s1);
            for (String s2 : strings2) {
                s2 = stripURIPrefix(s2);
                double similarity = sim.calculate(s1, s2);
                bestSimilarity = getHighestSimilarity(similarity, bestSimilarity);
            }
        }
        return bestSimilarity;
    }

    private String stripURIPrefix(String s) {
        if (s.contains("resource")) {
            s = s.substring(s.indexOf("resource")+9, s.length());
        }
        return s;
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

    /**
     * Get similarity score for a edit distance measure on striped and lowercase URIs
     * @param sim
     * @param uris
     * @param uris1
     * @param threshold
     * @return
     */
    public double getBestEditDistance(LevenshteinEditDistance sim, List<String> uris, List<String> uris1, double threshold) {
        

    }
}
