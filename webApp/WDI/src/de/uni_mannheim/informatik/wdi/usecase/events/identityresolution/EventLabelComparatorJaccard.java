package de.uni_mannheim.informatik.wdi.usecase.events.identityresolution;

import de.uni_mannheim.informatik.wdi.matching.Comparator;
import de.uni_mannheim.informatik.wdi.model.Correspondence;
import de.uni_mannheim.informatik.wdi.model.DefaultSchemaElement;
import de.uni_mannheim.informatik.wdi.similarity.string.TokenizingJaccardSimilarity;
import de.uni_mannheim.informatik.wdi.usecase.events.model.Event;

/**
 * {@link Comparator} for {@link Event}s based on the {@link Event#getLabel()}
 * value and their {@link TokenizingJaccardSimilarity} value.
 *
 * @author Daniel Ringler
 *
 */
public class EventLabelComparatorJaccard extends Comparator<Event, DefaultSchemaElement> {

    private static final long serialVersionUID = 1L;
    private TokenizingJaccardSimilarity sim = new TokenizingJaccardSimilarity();

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
