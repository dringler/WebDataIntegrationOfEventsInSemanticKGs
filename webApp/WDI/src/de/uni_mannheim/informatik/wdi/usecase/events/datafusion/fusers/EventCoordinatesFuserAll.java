package de.uni_mannheim.informatik.wdi.usecase.events.datafusion.fusers;

import de.uni_mannheim.informatik.wdi.datafusion.AttributeValueFuser;
import de.uni_mannheim.informatik.wdi.datafusion.conflictresolution.coordinates.FirstCoordinates;
import de.uni_mannheim.informatik.wdi.datafusion.conflictresolution.list.Union;
import de.uni_mannheim.informatik.wdi.model.*;
import de.uni_mannheim.informatik.wdi.usecase.events.model.Event;

import java.util.List;

/**
 * {@link AttributeValueFuser} for the coordinates of {@link Event}s.
 * Created on 2017-01-06
 * @author Daniel Ringler
 *
 */
public class EventCoordinatesFuserAll extends AttributeValueFuser<List<Pair<Double, Double>>, Event, DefaultSchemaElement> {
    public EventCoordinatesFuserAll() {
        super(new Union<Pair<Double, Double>, Event, DefaultSchemaElement>());
    }

    @Override
    public boolean hasValue(Event record, Correspondence<DefaultSchemaElement, Event> correspondence) {
        return record.hasValue(Event.COORDINATES);
    }

    @Override
    protected List<Pair<Double, Double>> getValue(Event record, Correspondence<DefaultSchemaElement, Event> correspondence) {
        return record.getCoordinates();
    }

    @Override
    public void fuse(RecordGroup<Event, DefaultSchemaElement> group, Event fusedRecord, ResultSet<Correspondence<DefaultSchemaElement, Event>> schemaCorrespondences, DefaultSchemaElement schemaElement) {
        FusedValue<List<Pair<Double, Double>>, Event, DefaultSchemaElement> fused = getFusedValue(group, schemaCorrespondences, schemaElement);
        fusedRecord.setCoordinates(fused.getValue());
        fusedRecord.setAttributeProvenance(Event.COORDINATES, fused.getOriginalIds());
    }

}
