package de.uni_mannheim.informatik.wdi.similarity.date;

import de.uni_mannheim.informatik.wdi.similarity.SimilarityMeasure;
import org.joda.time.DateTime;
import org.joda.time.Years;

import java.time.LocalDate;

/**
 * Created by Daniel Ringler on 21/12/16.
 */
public class YearSimilarityLocalDate extends SimilarityMeasure<LocalDate> {

    private static final long serialVersionUID = 1L;
    private int maxDifference;
        /**
         * Initialize {@link de.uni_mannheim.informatik.wdi.similarity.date.YearSimilarityLocalDate} with a maximal difference (in years).
         * In case the difference is larger the maximal difference, the calculated
         * similarity is 0. In the other cases its 1-(diff/maxDifference).
         *
         * @param maxDifference
         *            maximal difference between two dates in years.
         */
        public YearSimilarityLocalDate(int maxDifference) {
            this.maxDifference = maxDifference;
        }



        public double calculate(LocalDate first, LocalDate second) {
            if (first == null || second == null) {
                return 0.0;
            } else {
                int diff;
                if (first.isBefore(second))
                    diff = Math.abs(first.until(second).getYears());
                else
                    diff = Math.abs(second.until(first).getYears());
                double norm = Math.min((double) diff / (double) maxDifference, 1.0);

                return  1- norm;
            }
        }


}
