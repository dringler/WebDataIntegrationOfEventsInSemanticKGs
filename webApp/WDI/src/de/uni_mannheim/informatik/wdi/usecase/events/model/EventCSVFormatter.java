package de.uni_mannheim.informatik.wdi.usecase.events.model;

import de.uni_mannheim.informatik.wdi.model.CSVFormatter;
import de.uni_mannheim.informatik.wdi.model.DataSet;
import de.uni_mannheim.informatik.wdi.model.DefaultSchemaElement;

/**
 * {@link CSVFormatter} for {@link Event}s.
 *
 * @author Daniel Ringler
 *
 */
public class EventCSVFormatter extends CSVFormatter<Event,DefaultSchemaElement> {

    @Override
    public String[] getHeader(DataSet<Event, DefaultSchemaElement> dataset) {
        return dataset.getRandomRecord().getAttributeNames();
    }

    @Override
    public String[] format(Event record, DataSet<Event, DefaultSchemaElement> dataset, char s) {
        return record.getAllAttributeValues(s);
    }
}
