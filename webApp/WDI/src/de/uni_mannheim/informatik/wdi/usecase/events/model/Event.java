package de.uni_mannheim.informatik.wdi.usecase.events.model;

import de.uni_mannheim.informatik.wdi.model.DefaultSchemaElement;
import de.uni_mannheim.informatik.wdi.model.Pair;
import de.uni_mannheim.informatik.wdi.model.Record;
import de.uni_mannheim.informatik.wdi.usecase.events.model.Location;


import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * A {@link Record} which represents an actor
 *
 * @author Daniel Ringler
 *
 */
public class Event extends Record<DefaultSchemaElement> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<String> labels;
    private List<LocalDate> dates;
    private List<Pair<Double, Double>> coordinates;
    //private List<String> cities;
    //private List<String> countries;
    private List<Location> locations;
    private List<String> participants;
    private List<String> sames;

    public Event(String identifier, String provenance) {
        super(identifier, provenance);
    }


    public List<String> getLabels() {
		return labels;
    }

    public List<LocalDate> getDates() {
        return dates;
    }

    public List<Pair<Double, Double>> getCoordinates() {
        return coordinates;
    }

   /*public List<String> getCities() {  return cities; }

    public List<String> getCountries() { return countries;}*/

    public List<Location> getLocations() {
        return locations;
    }

    public List<String> getParticipants() {  return participants; }

    public List<String> getSames() {
        return sames;
    }


    public void addLabel(String label) {
        if (!this.labels.contains(label))
            this.labels.add(label);
    }

    public void addDate(LocalDate date) {
        if (!this.dates.contains(date))
            this.dates.add(date);

    }

    public void addCoordinates(Pair<Double, Double> coordinates) {
        if(!this.coordinates.contains(coordinates))
            this.coordinates.add(coordinates);
    }

    /*public void addCity(String city) {
        if (!this.cities.contains(city))
            this.cities.add(city);
    }

    public void addCountry(String country) {
        if(!this.countries.contains(country))
            this.countries.add(country);
    }*/

    public void addLocation(Location location) {
        if(!this.locations.contains(location))
            this.locations.add(location);
    }

    public void addParticipant(String participant) {
        if(!this.participants.contains(participant))
            this.participants.add(participant);
    }

    public void addSame(String same) {
        if(!this.sames.contains(same))
            this.sames.add(same);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int result = 31 + ((labels == null) ? 0 : labels.hashCode());
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
        if (labels == null) {
            if (other.labels != null)
                return false;
        } else if (!labels.equals(other.labels))
            return false;
        return true;
    }

    public static final DefaultSchemaElement LABELS = new DefaultSchemaElement("Labels");
    public static final DefaultSchemaElement DATES = new DefaultSchemaElement("Dates");
    public static final DefaultSchemaElement COORDINATES = new DefaultSchemaElement("Coordinates");
    //public static final DefaultSchemaElement CITIES = new DefaultSchemaElement("Cities");
    //public static final DefaultSchemaElement COUNTRIES = new DefaultSchemaElement("Countries");
    public static final DefaultSchemaElement LOCATIONS = new DefaultSchemaElement("Locations");
    public static final DefaultSchemaElement PARTICIPANTS = new DefaultSchemaElement("Participants");
    public static final DefaultSchemaElement SAMES = new DefaultSchemaElement("Sames");

    /* (non-Javadoc)
     * @see de.uni_mannheim.informatik.wdi.model.Record#hasValue(java.lang.Object)
     */
    @Override
    public boolean hasValue(DefaultSchemaElement attribute) {
        if(attribute==LABELS)
            return labels!=null;
        else if(attribute==DATES)
            return dates!=null;
        else if(attribute==COORDINATES)
            return coordinates!=null;//!Double.isNaN(lon);
        /**else if(attribute==CITIES)
            return cities!=null;
        else if(attribute==COUNTRIES)
            return countries!=null;*/
        else if (attribute==LOCATIONS)
            return locations!=null;
        else if (attribute==PARTICIPANTS)
            return participants!=null;
        else if (attribute==SAMES)
            return sames!=null;
        return false;
    }
}
