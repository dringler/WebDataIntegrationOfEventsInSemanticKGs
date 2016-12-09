package de.uni_mannheim.informatik.wdi.usecase.events.model;

import de.uni_mannheim.informatik.wdi.model.DefaultSchemaElement;
import de.uni_mannheim.informatik.wdi.model.Record;


import java.io.Serializable;
import java.time.LocalDate;

/**
 * A {@link Record} which represents an actor
 *
 * @author Daniel Ringler
 *
 */
public class Event extends Record<DefaultSchemaElement> implements Serializable {

	/*
	 * example entry <actor> <name>Janet Gaynor</name>
	 * <birthday>1906-01-01</birthday> <birthplace>Pennsylvania</birthplace>
	 * </actor>
	 */

    private static final long serialVersionUID = 1L;
    private String label;
    private LocalDate date;
    private double lat;
    private double lon;

    public Event(String identifier, String provenance) {
        super(identifier, provenance);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int result = 31 + ((label == null) ? 0 : label.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Event other = (Event) obj;
        if (label == null) {
            if (other.label != null)
                return false;
        } else if (!label.equals(other.label))
            return false;
        return true;
    }

    public static final DefaultSchemaElement LABEL = new DefaultSchemaElement("Label");
    public static final DefaultSchemaElement DATE = new DefaultSchemaElement("Date");
    public static final DefaultSchemaElement LAT = new DefaultSchemaElement("Lat");
    public static final DefaultSchemaElement LON = new DefaultSchemaElement("Lon");

    /* (non-Javadoc)
     * @see de.uni_mannheim.informatik.wdi.model.Record#hasValue(java.lang.Object)
     */
    @Override
    public boolean hasValue(DefaultSchemaElement attribute) {
        if(attribute==LABEL)
            return label!=null;
        else if(attribute==DATE)
            return date!=null;
        else if(attribute==LAT)
            return !Double.isNaN(lat);
        else if(attribute==LON)
            return !Double.isNaN(lon);
        return false;
    }
}
