package de.uni_mannheim.informatik.wdi.usecase.events.datafusion.fusers;

import de.uni_mannheim.informatik.wdi.datafusion.AttributeValueFuser;
import de.uni_mannheim.informatik.wdi.datafusion.conflictresolution.coordinates.FirstCoordinates;
import de.uni_mannheim.informatik.wdi.model.*;
import de.uni_mannheim.informatik.wdi.usecase.events.model.Event;

/**
 * {@link AttributeValueFuser} for the date of {@link Event}s.
 *
 * @author Daniel Ringler
 *
 */
public class EventCoordinatesFuserFirst extends AttributeValueFuser<Pair<Double, Double>, Event, DefaultSchemaElement> {
    public EventCoordinatesFuserFirst() {
        super(new FirstCoordinates<Pair<Double, Double>, Event, DefaultSchemaElement>());
    }

    @Override
    public boolean hasValue(Event record, Correspondence<DefaultSchemaElement, Event> correspondence) {
        return record.hasValue(Event.COORDINATES);
    }

    @Override
    protected Pair<Double, Double> getValue(Event record, Correspondence<DefaultSchemaElement, Event> correspondence) {
        if (record.getDates().size()>0) {
            for(Pair<Double, Double> p : record.getCoordinates()) {
                return p;
            }
        }
        return null;
    }

    @Override
    public void fuse(RecordGroup<Event, DefaultSchemaElement> group, Event fusedRecord, ResultSet<Correspondence<DefaultSchemaElement, Event>> schemaCorrespondences, DefaultSchemaElement schemaElement) {
        FusedValue<Pair<Double, Double>, Event, DefaultSchemaElement> fused = getFusedValue(group, schemaCorrespondences, schemaElement);
        fusedRecord.setSingleCoordinates(fused.getValue());
        fusedRecord.setAttributeProvenance(Event.COORDINATES, fused.getOriginalIds());
    }

}
