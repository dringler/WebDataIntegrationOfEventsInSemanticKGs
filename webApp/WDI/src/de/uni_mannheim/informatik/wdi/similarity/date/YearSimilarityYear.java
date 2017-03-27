package de.uni_mannheim.informatik.wdi.similarity.date;

import de.uni_mannheim.informatik.wdi.similarity.SimilarityMeasure;

import java.time.Year;

/**
 * Created by Daniel Ringler
 */
public class YearSimilarityYear extends SimilarityMeasure<Year> {

    private static final long serialVersionUID = 1L;
    private int maxDifference;
    /**
     * Initialize {@link de.uni_mannheim.informatik.wdi.similarity.date.YearSimilarityYear} with a maximal difference (in years).
     * In case the difference is larger the maximal difference, the calculated
     * similarity is 0. In the other cases its 1-(diff/maxDifference).
     *
     * @param maxDifference
     *            maximal difference between two dates in years.
     */
    public YearSimilarityYear(int maxDifference) {
        this.maxDifference = maxDifference;
    }



    public double calculate(Year first, Year second) {
        if (first == null || second == null) {
            return 0.0;
        } else {
            int diff = Math.abs(first.compareTo(second));
            double norm = Math.min((double) diff / (double) maxDifference, 1.0);
            return  1- norm;
        }
    }


}
