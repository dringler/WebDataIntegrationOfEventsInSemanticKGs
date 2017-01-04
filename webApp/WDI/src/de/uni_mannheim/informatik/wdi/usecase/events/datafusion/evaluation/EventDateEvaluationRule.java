package de.uni_mannheim.informatik.wdi.usecase.events.datafusion.evaluation;

import de.uni_mannheim.informatik.wdi.datafusion.EvaluationRule;
import de.uni_mannheim.informatik.wdi.model.Correspondence;
import de.uni_mannheim.informatik.wdi.model.DefaultSchemaElement;
import de.uni_mannheim.informatik.wdi.usecase.events.model.Event;

import java.time.LocalDate;

/**
 * {@link EvaluationRule} for the dates of {@link Event}s. The rule simply
 * compares the dates of two {@link Event}s and checks if one of the dates is the same.
 * Based on DateEvaluationRule. Created on 2017-01-04.
 * @author Daniel Ringler
 *
 */
public class EventDateEvaluationRule extends EvaluationRule<Event, DefaultSchemaElement> {
    @Override
    public boolean isEqual(Event record1, Event record2, DefaultSchemaElement schemaElement) {
        if(record1.getDates().size()==0 && record2.getDates().size()==0)
            return true;
        else if(record1.getDates().size()==0 ^ record2.getDates().size()==0)
            return false;
        else {//compare all dates
            for (LocalDate r1Date : record1.getDates()) {
                for (LocalDate r2Date : record2.getDates()) {
                    if (r1Date.isEqual(r2Date)) {
                        return true;//record1.getDates().getYear() == record2.getDates().getYear();
                    }
                }
            }
            return false;
        }
    }

    /* (non-Javadoc)
     * @see de.uni_mannheim.informatik.wdi.datafusion.EvaluationRule#isEqual(java.lang.Object, java.lang.Object, de.uni_mannheim.informatik.wdi.model.Correspondence)
     */
    @Override
    public boolean isEqual(Event record1, Event record2,
                           Correspondence<DefaultSchemaElement, Event> schemaCorrespondence) {
        return isEqual(record1, record2, (DefaultSchemaElement)null);
    }

}
