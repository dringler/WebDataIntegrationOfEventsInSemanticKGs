package de.uni_mannheim.informatik.wdi.usecase.events.identityresolution;

import de.uni_mannheim.informatik.wdi.matching.Comparator;
import de.uni_mannheim.informatik.wdi.model.Correspondence;
import de.uni_mannheim.informatik.wdi.model.DefaultSchemaElement;
import de.uni_mannheim.informatik.wdi.similarity.date.YearSimilarity;
import de.uni_mannheim.informatik.wdi.usecase.events.model.Event;

/**
 * {@link Comparator} for {@link Event}s based on the {@link Event#getDate()}
 * value. With a maximal difference of less than a years.
 *
 * @author Daniel Ringler
 *
 */
public class EventDateComparator extends Comparator<Event, DefaultSchemaElement> {
    private static final long serialVersionUID = 1L;
    private YearSimilarity sim = new YearSimilarity(1);

    @Override
    public double compare(
            Event record1,
            Event record2,
            Correspondence<DefaultSchemaElement, Event> schemaCorrespondences) {
        double similarity = sim.calculate(record1.getDate(), record2.getDate());

        return similarity * similarity;
    }


}
