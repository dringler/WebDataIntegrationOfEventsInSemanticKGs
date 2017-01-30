import java.util.HashSet;
import java.util.Set;

/**
 * Created by Daniel on 17/01/17.
 */
public class Location {
    private String uri;
    private Set<String> labels;
    private Set<String> coordinatePairs;
    private Set<String> sames;

    //constructor
    public Location(){};
    public Location(String uri) {
        this.uri = uri;
        this.labels = new HashSet<>();
        this.coordinatePairs = new HashSet<>();
        this.sames = new HashSet<>();
    }

    //getter
    public String getUri() {return uri;}
    public Set<String> getLabels() {return labels;}
    public Set<String> getCoordinatePairs() {return coordinatePairs;}
    public Set<String> getSames() {return sames;}

    //setter
    public void setUri(String uri) {this.uri = uri;}
    public void setLabels(Set<String> labels) {this.labels = labels;}
    public void setCoordinatePairs(Set<String> coordinatePairs) {this.coordinatePairs = coordinatePairs;}
    public void setSames(Set<String> sames) {this.sames = sames;}

    //adder
    public void addLabel(String label) {
        this.labels.add(label);
    }
    public void addCoordinatePair(String coordinatePair) {
        this.coordinatePairs.add(coordinatePair);
    }
    public void addSame(String same) {
        this.sames.add(same);
    }

    public boolean hasLabel() {
        if (this.labels.size()>0)
            return true;
        return false;
    }

    public boolean hasCoordinatePair() {
        if (this.coordinatePairs.size()>0)
            return true;
        return false;
    }
    public boolean hasSame() {
        if (this.sames.size()>0)
            return true;
        return false;
    }

}
