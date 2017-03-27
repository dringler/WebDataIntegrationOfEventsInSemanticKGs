package de.uni_mannheim.informatik.wdi.similarity.string;

import com.wcohen.ss.Levenstein;
import de.uni_mannheim.informatik.wdi.similarity.SimilarityMeasure;

/**
 * {@link SimilarityMeasure}, that calculates the Levenshtein edit distance
 * between two strings.
 *
 * @author Daniel Ringler
 *
 */
public class LevenshteinEditDistance extends SimilarityMeasure<String> {

    private static final long serialVersionUID = 1L;

    @Override
    public double calculate(String first, String second) {
        if (first == null || second == null) {
            return -1.0;
        } else {
            Levenstein l = new Levenstein();

            //double score = Math.abs(l.score(first, second));
            //score = score / Math.max(first.length(), second.length());
            //return 1 - score;
            return Math.abs(l.score(first, second));
        }
    }

}
