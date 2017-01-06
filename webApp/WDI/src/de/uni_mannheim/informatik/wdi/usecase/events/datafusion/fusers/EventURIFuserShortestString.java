package de.uni_mannheim.informatik.wdi.usecase.events.datafusion.fusers;

import de.uni_mannheim.informatik.wdi.datafusion.AttributeValueFuser;
import de.uni_mannheim.informatik.wdi.datafusion.conflictresolution.string.ShortestString;
import de.uni_mannheim.informatik.wdi.model.*;
import de.uni_mannheim.informatik.wdi.usecase.events.model.Event;

/**
 * {@link AttributeValueFuser} for the URIs of {@link Event}s.
 * based on TitleFuserShortestString
 * created on 2017-01-06
 * @author Daniel Ringler
 *
 */
public class EventURIFuserShortestString extends
        AttributeValueFuser<String, Event, DefaultSchemaElement> {


    public EventURIFuserShortestString() {
        super(new ShortestString<Event, DefaultSchemaElement>());
    }

    @Override
    public void fuse(RecordGroup<Event, DefaultSchemaElement> group, Event fusedRecord, ResultSet<Correspondence<DefaultSchemaElement, Event>> schemaCorrespondences, DefaultSchemaElement schemaElement) {

        // get the fused value
        FusedValue<String, Event, DefaultSchemaElement> fused = getFusedValue(group, schemaCorrespondences, schemaElement);

        // set the value for the fused record
        fusedRecord.setSingleURI(fused.getValue());

        // add provenance info
        fusedRecord.setAttributeProvenance(Event.URIS, fused.getOriginalIds());
    }

    @Override
    public boolean hasValue(Event record, Correspondence<DefaultSchemaElement, Event> correspondence) {
        return record.hasValue(Event.URIS);
    }

    @Override
    protected String getValue(Event record, Correspondence<DefaultSchemaElement, Event> correspondence) {
        String uris = "";
        for (String uri : record.getUris()) {
            uris += uri + ",";
        }
        if (uris.length()>0)
            uris = uris.substring(0,uris.length()-1);
        return uris;
    }
}
