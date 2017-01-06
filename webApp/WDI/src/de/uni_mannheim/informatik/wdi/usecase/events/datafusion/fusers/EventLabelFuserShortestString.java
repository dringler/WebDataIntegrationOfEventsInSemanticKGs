package de.uni_mannheim.informatik.wdi.usecase.events.datafusion.fusers;

import de.uni_mannheim.informatik.wdi.datafusion.AttributeValueFuser;
import de.uni_mannheim.informatik.wdi.datafusion.conflictresolution.string.ShortestString;
import de.uni_mannheim.informatik.wdi.model.*;
import de.uni_mannheim.informatik.wdi.usecase.events.model.Event;
import de.uni_mannheim.informatik.wdi.usecase.events.model.Movie;

import java.util.List;

/**
 * {@link AttributeValueFuser} for the titles of {@link Event}s.
 * based on TitleFuserShortestString
 * created on 2017-01-04
 * @author Daniel Ringler
 *
 */
public class EventLabelFuserShortestString extends
        AttributeValueFuser<String, Event, DefaultSchemaElement> {


    public EventLabelFuserShortestString() {
        super(new ShortestString<Event, DefaultSchemaElement>());
    }

    @Override
    public void fuse(RecordGroup<Event, DefaultSchemaElement> group, Event fusedRecord, ResultSet<Correspondence<DefaultSchemaElement, Event>> schemaCorrespondences, DefaultSchemaElement schemaElement) {

        // get the fused value
        FusedValue<String, Event, DefaultSchemaElement> fused = getFusedValue(group, schemaCorrespondences, schemaElement);

        // set the value for the fused record
        fusedRecord.setSingleLabel(fused.getValue());

        // add provenance info
        fusedRecord.setAttributeProvenance(Event.LABELS, fused.getOriginalIds());
    }

    @Override
    public boolean hasValue(Event record, Correspondence<DefaultSchemaElement, Event> correspondence) {
        return record.hasValue(Event.LABELS);
    }

    @Override
    protected String getValue(Event record, Correspondence<DefaultSchemaElement, Event> correspondence) {
        String labels = "";
        for (String label : record.getLabels()) {
            labels += label + ",";
        }
        if (labels.length()>0)
            labels = labels.substring(0,labels.length()-1);
        return labels;
    }

}
