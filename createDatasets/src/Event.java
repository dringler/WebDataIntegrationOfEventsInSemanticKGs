import java.util.HashSet;
import java.util.Set;

/**
 * Created by Daniel on 17/01/17.
 */
public class Event {
    private String uri;
    private Set<String> labels;
    private Set<String> dates;
    private Set<String> coordinatePairs;
    private Set<String> sames;
    private Set<Location> locations;

    public Event(String uri) {
        this.uri = uri;
        this.labels = new HashSet<>();
        this.dates = new HashSet<>();
        this.coordinatePairs = new HashSet<>();
        this.sames = new HashSet<>();
        this.locations = new HashSet<>();
    }

    //getter
    public String getUri() {return uri;}
    public Set<String> getLabels() {return labels;}
    public Set<String> getDates() {return dates;}
    public Set<String> getCoordinatePairs() {return coordinatePairs;}
    public Set<String> getSames() {return sames;}
    public Set<Location> getLocations() {return locations;}

    //setter
    public void setUri(String uri) {this.uri = uri;}
    public void setLabels(Set<String> labels) {this.labels = labels;}
    public void setDates(Set<String> dates) {this.dates = dates;}
    public void setCoordinatePairs(Set<String> coordinatePairs) {this.coordinatePairs = coordinatePairs;}
    public void setSames(Set<String> sames) {this.sames = sames;}
    public void setLocations(Set<Location> locations) {this.locations = locations;}

    //adder
    public void addLabel(String label) {
        this.labels.add(label);
    }
    public void addDate(String date) {
        this.dates.add(date);
    }
    public void addCoordinatePair(String coordinatePair) {
        this.coordinatePairs.add(coordinatePair);

    }
    public void addSame(String same) {
        this.sames.add(same);
    }

    public void addLocation(Location location) {
        this.locations.add(location);
    }

}
