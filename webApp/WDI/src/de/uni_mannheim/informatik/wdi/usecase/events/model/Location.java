package de.uni_mannheim.informatik.wdi.usecase.events.model;

import de.uni_mannheim.informatik.wdi.model.DefaultSchemaElement;
import de.uni_mannheim.informatik.wdi.model.Pair;
import de.uni_mannheim.informatik.wdi.model.Record;

import java.io.Serializable;
import java.util.List;

/**
 * Created by curtis on 19/12/16.
 */
public class Location extends Record<DefaultSchemaElement> implements Serializable {

    private List<String> labels;
    private List<String> types;
    private List<Pair<Double, Double>> coordinates;
    private List<String> sames;

    public Location(String identifier, String provenance) {
        super(identifier, provenance);
    }

    //GETTER
    public List<String> getLabels() {
        return labels;
    }
    public List<String> getTypes() {
        return types;
    }
    public List<Pair<Double, Double>> getCoordinates() {
        return coordinates;
    }
    public List<String> getSames() {
        return sames;
    }


    //ADDER
    public void addLabel(String label) {
        if (!this.labels.contains(label))
            this.labels.add(label);
    }
    public void addType(String type) {
        if (!this.types.contains(type))
            this.types.add(type);
    }

    public void addCoordinates(Pair<Double, Double> coordinates) {
        if(!this.coordinates.contains(coordinates))
            this.coordinates.add(coordinates);
    }

    public void addSame(String same) {
        if (!this.sames.contains(same))
            this.sames.add(same);
    }


    public static final DefaultSchemaElement LABELS = new DefaultSchemaElement("Labels");
    public static final DefaultSchemaElement TYPES = new DefaultSchemaElement("Types");
    public static final DefaultSchemaElement COORDINATES = new DefaultSchemaElement("Coordinates");
    public static final DefaultSchemaElement SAMES = new DefaultSchemaElement("Sames");

    /* (non-Javadoc)
     * @see de.uni_mannheim.informatik.wdi.model.Record#hasValue(java.lang.Object)
     */
    @Override
    public boolean hasValue(DefaultSchemaElement attribute) {
        if(attribute==LABELS)
            return labels!=null;
        else if(attribute==TYPES)
            return types!=null;
        else if(attribute==COORDINATES)
            return coordinates!=null;//!Double.isNaN(lon);
        else if (attribute==SAMES)
            return sames!=null;
        return false;
    }
}
