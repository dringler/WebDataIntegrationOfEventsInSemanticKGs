package de.uni_mannheim.informatik.wdi.similarity;


import de.uni_mannheim.informatik.wdi.similarity.date.YearSimilarityLocalDate;
import de.uni_mannheim.informatik.wdi.similarity.date.YearSimilarityYear;
import de.uni_mannheim.informatik.wdi.similarity.string.LevenshteinEditDistance;
import org.joda.time.Years;

import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Created by Daniel Ringler on 21/12/16.
 */
public class BestListSimilarity {

    private double compareDistanceWithThreshold(double lowestDistance, double threshold) {
        if (lowestDistance <= threshold) {
            return 1.0;
        } else {
            return 0.0;
        }
    }

    private double getLowestDistance(double editDistance, double lowestDistance) {
        if (lowestDistance == -1.0)
            return editDistance;
        if (editDistance < lowestDistance)
            return editDistance;
        return lowestDistance;
    }

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
        if (dates1==null || dates2==null) {
            return -1.0;
        }
        for (LocalDate d1 : dates1) {
            for (LocalDate d2 : dates2) {
                double similarity = sim.calculate(d1.toString(), d2.toString());
                bestSimilarity = getHighestSimilarity(similarity, bestSimilarity);
            }
        }
        return bestSimilarity;
    }
    public double getBestDatesEditDistance(SimilarityMeasure sim, List<LocalDate> dates1, List<LocalDate> dates2, double threshold) {
        double lowestDistance = -1.0;
        if (dates1.size()==0 || dates2.size()==0) {
            return lowestDistance;
        }
        for (LocalDate d1 : dates1) {
            for (LocalDate d2 : dates2) {
                double editDistance = sim.calculate(d1.toString(), d2.toString());
                lowestDistance = getLowestDistance(editDistance, lowestDistance);
            }
        }
        return compareDistanceWithThreshold(lowestDistance, threshold);
    }


    private double getHighestSimilarity(double similarity, double bestSimilarity) {
        if (similarity >= bestSimilarity)
            return similarity;
        return bestSimilarity;
    }

    /**
     * Get similarity score for a edit distance measure on striped and lowercase attributes
     * @param sim
     * @param strings1
     * @param strings2
     * @param threshold
     * @return
     */
    public double getBestEditDistanceStripedLowercase(LevenshteinEditDistance sim, List<String> strings1, List<String> strings2, double threshold) {
        double lowestDistance = -1.0;
        for (String s1 : strings1) {
            s1 = stripURIPrefix(s1).toLowerCase();
            for (String s2 : strings2) {
                s2 = stripURIPrefix(s2).toLowerCase();
                double editDistance = sim.calculate(s1, s2);
                lowestDistance = getLowestDistance(editDistance, lowestDistance);
            }
        }
        return compareDistanceWithThreshold(lowestDistance, threshold);

    }

    /**
     * Get similarity score for a edit distance measure
     * @param sim
     * @param strings1
     * @param strings2
     * @param threshold
     * @return
     */
    public double getBestEditDistance(LevenshteinEditDistance sim, List<String> strings1, List<String> strings2, double threshold) {
        double lowestDistance = -1.0;
        for (String s1 : strings1) {
            for (String s2 : strings2) {
                double editDistance = sim.calculate(s1, s2);
                lowestDistance = getLowestDistance(editDistance, lowestDistance);
            }
        }
        return compareDistanceWithThreshold(lowestDistance, threshold);

    }

    public double getBestDatesSimilarityWithTokenizedStrings(YearSimilarityYear sim, List<String> strings1, List<String> strings2, double threshold, DateTimeFormatter formatter) {
        double bestSimilarity = -1.0;
        for (String s1 : strings1) {
            for (String t1 : s1.split("\\s+")) {
                for (String s2 : strings2) {
                    for (String t2 : s2.split("\\s+")) {
                        try {
                            Year d1 = Year.parse(t1, formatter);
                            Year d2 = Year.parse(t2, formatter);

                            double similarity = sim.calculate(d1, d2);
                            bestSimilarity = getHighestSimilarity(similarity, bestSimilarity);
                        } catch (DateTimeParseException e) {
                            //System.out.println(t1 + " or " + t2 + " could not be parsed.");
                            //return -1.0;
                        }
                    }
                }
            }
        }
        return bestSimilarity;
    }
}
