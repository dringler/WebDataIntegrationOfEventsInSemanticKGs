package de.uni_mannheim.informatik.wdi.usecase.events.identityresolution;

import de.uni_mannheim.informatik.wdi.matching.Comparator;
import de.uni_mannheim.informatik.wdi.model.Correspondence;
import de.uni_mannheim.informatik.wdi.model.DefaultSchemaElement;
import de.uni_mannheim.informatik.wdi.similarity.BestListSimilarity;
import de.uni_mannheim.informatik.wdi.similarity.date.YearSimilarityLocalDate;
import de.uni_mannheim.informatik.wdi.usecase.events.model.Event;

import java.time.LocalDate;
import java.util.List;

/**
 * {@link Comparator} for {@link Event}s based on the {@link Event#getDates()}
 * values. With a maximal difference of less than a years.
 *
 * @author Daniel Ringler
 *
 */
public class EventDateComparator extends Comparator<Event, DefaultSchemaElement> {
    private static final long serialVersionUID = 1L;
    private BestListSimilarity bestListSimilarity = new BestListSimilarity();
    private YearSimilarityLocalDate sim = new YearSimilarityLocalDate(1);

    @Override
    public double compare(
            Event record1,
            Event record2,
            Correspondence<DefaultSchemaElement, Event> schemaCorrespondences) {

        return bestListSimilarity.getBestDatesSimilarity(sim, record1.getDates(), record2.getDates());
    }


}
