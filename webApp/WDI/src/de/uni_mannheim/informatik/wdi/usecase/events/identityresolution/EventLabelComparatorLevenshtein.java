package de.uni_mannheim.informatik.wdi.usecase.events.identityresolution;

import de.uni_mannheim.informatik.wdi.matching.Comparator;
import de.uni_mannheim.informatik.wdi.model.Correspondence;
import de.uni_mannheim.informatik.wdi.model.DefaultSchemaElement;
import de.uni_mannheim.informatik.wdi.similarity.string.LevenshteinSimilarity;
import de.uni_mannheim.informatik.wdi.usecase.events.model.Event;

/**
 * {@link Comparator} for {@link Event}s based on the {@link Event#getLabel()}
 * value and their {@link LevenshteinSimilarity} value.
 * Based on the MovieTitleComparatorLevenshtein class
 *
 * @author Daniel Ringler
 *
 */
public class EventLabelComparatorLevenshtein extends Comparator<Event, DefaultSchemaElement> {
    private static final long serialVersionUID = 1L;
    private LevenshteinSimilarity sim = new LevenshteinSimilarity();

    @Override
    public double compare(
            Event record1,
            Event record2,
            Correspondence<DefaultSchemaElement, Event> schemaCorrespondences) {
        double similarity = sim.calculate(record1.getLabel(),
                record2.getLabel());

        return similarity;
    }

}





