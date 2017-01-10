package de.uni_mannheim.informatik.wdi.usecase.events.model;

import de.uni_mannheim.informatik.wdi.model.DefaultSchemaElement;
import de.uni_mannheim.informatik.wdi.model.Pair;
import de.uni_mannheim.informatik.wdi.model.Record;
import de.uni_mannheim.informatik.wdi.usecase.events.model.Location;
import org.apache.commons.lang3.StringUtils;


import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

/**
 * A {@link Record} which represents an actor
 *
 * @author Daniel Ringler
 *
 */
public class Event extends Record<DefaultSchemaElement> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<String> uris = new ArrayList<>();
    private List<String> labels = new ArrayList<>();
    private List<LocalDate> dates = new ArrayList<>();
    private List<Pair<Double, Double>> coordinates = new ArrayList<>();
    //private List<String> cities;
    //private List<String> countries;
    private List<Location> locations = new ArrayList<>();
    private List<String> participants = new ArrayList<>();
    private List<String> sames = new ArrayList<>();

    public Event(String identifier, String provenance) {
        super(identifier, provenance);
    }

    private String[] attributeNames = {"URIs", "Labels", "Dates", "Lat", "Long", "Locations", "Participants", "Sames"};
    public String[] getAttributeNames() {
        return attributeNames;
    }

    public String[] getAllAttributeValues() {
        //separator for multiple values
        char separator = '+';

        //get URIs
        String allURIs = "";
        if (hasValue(URIS)) {
            Collections.sort(uris);
            for (String uri : uris) {
                allURIs += uri + separator;
            }
            allURIs = allURIs.substring(0, allURIs.length()-1);
        }
        //get Labels
        String allLabels = "";
        if (hasValue(LABELS)) {
            for (String label : labels) {
                allLabels += label + separator;
            }
            allLabels = allLabels.substring(0,allLabels.length()-1);
        }
        //get Dates
        String allDates = "";
        if (hasValue(DATES)) {
            for (LocalDate date : dates) {
                allDates += date.toString() + separator;
            }
            allDates = allDates.substring(0, allDates.length()-1);
        }
        //get coordinates
        String allLat = "";
        if (hasValue(COORDINATES)) {
            for (Pair<Double, Double> p : coordinates) {
                allLat += p.getFirst().toString() + separator;
            }
            allLat = allLat.substring(0, allLat.length()-1);
        }
        String allLong = "";
        if (hasValue(COORDINATES)) {
            for (Pair<Double, Double> p : coordinates) {
                allLong += p.getSecond().toString() + separator;
            }
            allLong = allLong.substring(0, allLong.length()-1);
        }

        //get locations
       String allLocations = "";
       /*  if (hasValue(LOCATIONS)) {
            for (Location location : locations) {

            }
        }*/

       //get participants
        String allParticipants = "";

        //get Sames
        String allSames = "";
        String [] allValues = {allURIs, allLabels, allDates, allLat, allLong, allLocations, allParticipants, allSames};
        return allValues;
    }

    //getter
    public List<String> getUris() {
        return uris;
    }
    public List<String> getLabels() {
		return labels;
    }

    public List<LocalDate> getDates() { return dates; }

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

    //setter
    public void setURIs(List<String> uris) {
        this.uris = uris;
    }
    public void setLabels(List<String> labels) {
        this.labels = labels;
    }
    public void setDates(List<LocalDate> dates) {
        this.dates = dates;
    }
    public void setCoordinates(List<Pair<Double, Double>> coordinates) { this.coordinates = coordinates;  }
    public void setLocations(List<Location> locations) { this.locations = locations; }
    public void setParticipants(List<String> participants) { this.participants = participants; }
    public void setSames(List<String> sames) { this.sames = sames; }

    public void setSingleURI(String uri) {
        this.uris.clear();
        this.uris.add(uri);
    }
    /*
    * clear labels and add one new label
    * @param label
    */
    public void setSingleLabel(String label) {
        this.labels.clear();
        this.labels.add(label);
    }

    public void setSingleDate(LocalDate date) {
        this.dates.clear();
        this.dates.add(date);
    }

    public void setSingleCoordinates(Pair<Double, Double> p) {
        this.coordinates.clear();
        this.coordinates.add(p);
    }


    //adder
    public void addURI(String uri) {
        if (!this.uris.contains(uri))
            this.uris.add(uri);
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

    public static final DefaultSchemaElement URIS = new DefaultSchemaElement("URIs");
    public static final DefaultSchemaElement LABELS = new DefaultSchemaElement("Labels");
    public static final DefaultSchemaElement DATES = new DefaultSchemaElement("Dates");
    public static final DefaultSchemaElement COORDINATES = new DefaultSchemaElement("Coordinates");
    //public static final DefaultSchemaElement CITIES = new DefaultSchemaElement("Cities");
    //public static final DefaultSchemaElement COUNTRIES = new DefaultSchemaElement("Countries");
    public static final DefaultSchemaElement LOCATIONS = new DefaultSchemaElement("Locations");
    public static final DefaultSchemaElement PARTICIPANTS = new DefaultSchemaElement("Participants");
    public static final DefaultSchemaElement SAMES = new DefaultSchemaElement("Sames");

    public DefaultSchemaElement[] getDefaultSchemaElements() {
        DefaultSchemaElement [] allDefaultSchemaElements = {URIS, LABELS, DATES, COORDINATES, LOCATIONS, PARTICIPANTS, SAMES};
        return allDefaultSchemaElements;
    }

    /* (non-Javadoc)
     * @see de.uni_mannheim.informatik.wdi.model.Record#hasValue(java.lang.Object)
     */
    @Override
    public boolean hasValue(DefaultSchemaElement attribute) {
        if(attribute==URIS)
            return uris.size()>0;
        if(attribute==LABELS)
            return labels.size()>0;
        else if(attribute==DATES)
            return dates.size()>0;
        else if(attribute==COORDINATES)
            return coordinates.size()>0;//!=null;//!Double.isNaN(lon);
        /**else if(attribute==CITIES)
            return cities!=null;
        else if(attribute==COUNTRIES)
            return countries!=null;*/
        else if (attribute==LOCATIONS)
            return locations.size()>0;
        else if (attribute==PARTICIPANTS)
            return participants.size()>0;
        else if (attribute==SAMES)
            return sames.size()>0;
        return false;
    }

    private Map<DefaultSchemaElement, Collection<String>> provenance = new HashMap<>();
    private Collection<String> recordProvenance;

    public void setRecordProvenance(Collection<String> provenance) {
        //this.provenance.put("RECORD", provenance);
        recordProvenance = provenance;
    }

    public Collection<String> getRecordProvenance() {
        //return provenance.get("RECORD");
        return recordProvenance;
    }

    public void setAttributeProvenance(DefaultSchemaElement attribute,
                                       Collection<String> provenance) {
        this.provenance.put(attribute, provenance);
    }

    public Collection<String> getAttributeProvenance(String attribute) {
        return provenance.get(attribute);
    }

    public String getMergedAttributeProvenance(DefaultSchemaElement attribute) {
        Collection<String> prov = provenance.get(attribute);

        if (prov != null) {
            return StringUtils.join(prov, "+");
        } else {
            return "";
        }
    }



}
