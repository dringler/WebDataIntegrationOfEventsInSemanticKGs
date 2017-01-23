package de.uni_mannheim.informatik.wdi.usecase.events.model;

import de.uni_mannheim.informatik.wdi.model.DefaultSchemaElement;
import de.uni_mannheim.informatik.wdi.model.Pair;
import de.uni_mannheim.informatik.wdi.model.Record;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniel Ringler on 19/12/16.
 */
public class Location extends Record<DefaultSchemaElement> implements Serializable {

    private List<String> labels = new ArrayList<>();
    private List<String> types = new ArrayList<>();
    private List<Pair<Double, Double>> coordinates = new ArrayList<>();
    private List<String> countries;
    private List<String> cities;
    private boolean isACountry;
    private boolean isAState;
    private boolean isACity;
    private List<String> sames = new ArrayList<>();

    //countries, cities, capital ...
    //immer das spezifischste attribut mitnehmen (land < staat < stadt)

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

    //SETTER
    public void setLabels(List<String> labels) {this.labels = labels;}
    public void setSames(List<String> sames) {this.sames = sames;}
    public void setCoordinates(List<Pair<Double, Double>> coordinates) {this.coordinates = coordinates;}

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
            return labels.size()>0;
        else if(attribute==TYPES)
            return types.size()>0;
        else if(attribute==COORDINATES)
            return coordinates.size()>0;//!Double.isNaN(lon);
        else if (attribute==SAMES)
            return sames.size()>0;
        return false;
    }

    @Override
    public int getNumberOfValues(DefaultSchemaElement attribute) {
        if(attribute==LABELS)
            return labels.size();
        else if(attribute==TYPES)
            return types.size();
        else if(attribute==COORDINATES)
            return coordinates.size();//!Double.isNaN(lon);
        else if (attribute==SAMES)
            return sames.size();
        return 0;
    }

    @Override
    public DefaultSchemaElement[] getDefaultSchemaElements() {
        DefaultSchemaElement [] allDefaultSchemaElements = {LABELS, COORDINATES, SAMES};//TYPES
        return allDefaultSchemaElements;
    }
}
