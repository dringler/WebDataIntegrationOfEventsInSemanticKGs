package de.uni_mannheim.informatik.wdi.usecase.events.datafusion.fusers;

import de.uni_mannheim.informatik.wdi.datafusion.AttributeValueFuser;
import de.uni_mannheim.informatik.wdi.datafusion.conflictresolution.date.FirstDate;
import de.uni_mannheim.informatik.wdi.model.*;
import de.uni_mannheim.informatik.wdi.usecase.events.model.Event;
import java.time.LocalDate;


/**
 * {@link AttributeValueFuser} for the date of {@link Event}s.
 *
 * @author Daniel Ringler
 *
 */
public class EventDateFuserFirst extends AttributeValueFuser<LocalDate, Event, DefaultSchemaElement> {
    public EventDateFuserFirst() {
        super(new FirstDate<LocalDate, Event, DefaultSchemaElement>());
    }

    @Override
    public boolean hasValue(Event record, Correspondence<DefaultSchemaElement, Event> correspondence) {
        return record.hasValue(Event.DATES);
    }

    @Override
    protected LocalDate getValue(Event record, Correspondence<DefaultSchemaElement, Event> correspondence) {
        if (record.getDates().size()>0) {
            for(LocalDate date : record.getDates()) {
                    return date;
            }
        }
        return null;
    }

    @Override
    public void fuse(RecordGroup<Event, DefaultSchemaElement> group, Event fusedRecord, ResultSet<Correspondence<DefaultSchemaElement, Event>> schemaCorrespondences, DefaultSchemaElement schemaElement) {
        FusedValue<LocalDate, Event, DefaultSchemaElement> fused = getFusedValue(group, schemaCorrespondences, schemaElement);
        fusedRecord.setSingleDate(fused.getValue());
        fusedRecord.setAttributeProvenance(Event.DATES, fused.getOriginalIds());
    }

}
