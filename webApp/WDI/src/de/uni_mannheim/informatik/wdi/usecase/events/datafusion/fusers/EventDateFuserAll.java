package de.uni_mannheim.informatik.wdi.usecase.events.datafusion.fusers;

import de.uni_mannheim.informatik.wdi.datafusion.AttributeValueFuser;
import de.uni_mannheim.informatik.wdi.datafusion.conflictresolution.list.Union;
import de.uni_mannheim.informatik.wdi.model.*;
import de.uni_mannheim.informatik.wdi.usecase.events.model.Event;

import java.time.LocalDate;
import java.util.List;

/**
 * {@link AttributeValueFuser} for the dates of {@link Event}s.
 * Based on ActorFuserUnion. Created on 2017-01-06
 * @author Daniel Ringler
 *
 */
public class EventDateFuserAll extends AttributeValueFuser<List<LocalDate>, Event, DefaultSchemaElement> {
    public EventDateFuserAll() {
        super(new Union<LocalDate, Event, DefaultSchemaElement>());
    }

    @Override
    public boolean hasValue(Event record, Correspondence<DefaultSchemaElement, Event> correspondence) {
        return record.hasValue(Event.DATES);
    }

    @Override
    protected List<LocalDate> getValue(Event record, Correspondence<DefaultSchemaElement, Event> correspondence) {
        return record.getDates();
    }


    @Override
    public void fuse(RecordGroup<Event, DefaultSchemaElement> group, Event fusedRecord, ResultSet<Correspondence<DefaultSchemaElement, Event>> schemaCorrespondences, DefaultSchemaElement schemaElement) {
        FusedValue<List<LocalDate>, Event, DefaultSchemaElement> fused = getFusedValue(group, schemaCorrespondences, schemaElement);
        fusedRecord.setDates(fused.getValue());
        fusedRecord.setAttributeProvenance(Event.DATES, fused.getOriginalIds());
    }

}
