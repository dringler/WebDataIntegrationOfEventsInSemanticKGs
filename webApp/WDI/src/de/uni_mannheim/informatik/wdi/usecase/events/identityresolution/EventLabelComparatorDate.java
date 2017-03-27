package de.uni_mannheim.informatik.wdi.usecase.events.identityresolution;

import de.uni_mannheim.informatik.wdi.matching.Comparator;
import de.uni_mannheim.informatik.wdi.model.Correspondence;
import de.uni_mannheim.informatik.wdi.model.DefaultSchemaElement;
import de.uni_mannheim.informatik.wdi.similarity.BestListSimilarity;
import de.uni_mannheim.informatik.wdi.similarity.date.YearSimilarityYear;
import de.uni_mannheim.informatik.wdi.usecase.events.model.Event;

import java.time.format.DateTimeFormatter;

/**
* {@link Comparator} for {@link Event}s based on the {@link Event#getLabels()}
* value and their {@link YearSimilarityYear} value.
* Based on the MovieTitleComparatorLevenshtein class
*
* @author Daniel Ringler
*
*/
public class EventLabelComparatorDate  extends Comparator<Event, DefaultSchemaElement> {
    private static final long serialVersionUID = 1L;

    private BestListSimilarity bestListSimilarity = new BestListSimilarity();
    private YearSimilarityYear sim = new YearSimilarityYear(1);
    private double threshold;
    private DateTimeFormatter formatter;

    public EventLabelComparatorDate(double t, DateTimeFormatter f) {
        threshold = t;
        formatter = f;
    }

    @Override
    public double compare(
            Event record1,
            Event record2,
            Correspondence<DefaultSchemaElement, Event> schemaCorrespondences) {
        return bestListSimilarity.getBestDatesSimilarityWithTokenizedStrings(sim, record1.getLabels(), record2.getLabels(), threshold, formatter);
    }

}

