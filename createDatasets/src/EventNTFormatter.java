import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Created by Daniel on 28/01/17.
 */
public class EventNTFormatter {
    private BufferedWriter writer;
    //general properties
    private String rdfType = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";
    private String rdfsLabel = "<http://www.w3.org/2000/01/rdf-schema#label>";
    private String owlSameAs = "<http://www.w3.org/2002/07/owl#sameAs>";

    //event class & properties
    private String eventClass = "";
    private String dateProperty = "";
    private String latProperty = "";
    private String longProperty = "";
    private String locationProperty = "";

    public EventNTFormatter(int k, String fileName) {
        try {
            this.writer = new BufferedWriter(new FileWriter(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (k==0) {//dbpedia
            this.eventClass = "http://dbpedia.org/ontology/Event";
            this.dateProperty = "<http://dbpedia.org/ontology/date>";
            this.latProperty = "<http://www.w3.org/2003/01/geo/wgs84_pos#lat>";
            this.longProperty = "<http://www.w3.org/2003/01/geo/wgs84_pos#long>";
            this.locationProperty = "<http://dbpedia.org/ontology/place>";

        } else {//yago
            this.eventClass = "http://yago-knowledge.org/resource/wordnet_event_100029378";
            this.dateProperty = "<http://yago-knowledge.org/resource/happenedOnDate>";
            this.latProperty = "<http://yago-knowledge.org/resource/hasLatitude>";
            this.longProperty = "<http://yago-knowledge.org/resource/hasLongitude>";
            this.locationProperty = "<http://yago-knowledge.org/resource/isLocatedIn>";
        }
    };

    public boolean writeNTFromMap(Map<String, Event> eventMap) {

        try {
            for (Event event : eventMap.values()) {
                writeEventToNT(event);
            }

            this.writer.close();
        }	catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void writeEventToNT(Event event) {
        String eventURI = event.getUri();
        //event class
        writeNTLine(eventURI, this.rdfType, this.eventClass);
        //labels
        if (event.hasLabel())
            writeLabels(eventURI, event.getLabels());
        //dates
        if (event.hasDate())
            writeDates(eventURI, event.getDates());
        //coordinatePair
        if (event.hasCoordinatePair())
            writeCoordinates(eventURI, event.getCoordinatePairs());
        //sames
        if (event.hasSame())
            writeSames(eventURI, event.getSames());
        //locations
        if (event.hasLocation()) {
            for (Location location : event.getLocations()) {
                String locationURI = location.getUri();
                writeNTLine(eventURI, this.locationProperty, locationURI);

                //location label
                if (location.hasLabel())
                    writeLabels(locationURI, location.getLabels());
                //location coordinatePair
                if (location.hasCoordinatePair())
                    writeCoordinates(locationURI, location.getCoordinatePairs());
                //same
                if (location.hasSame())
                    writeSames(locationURI, location.getSames());

            }
        }
    }

    private void writeSames(String uri, Set<String> sames) {
        for (String same : sames) {
            writeNTLine(uri, this.owlSameAs, same);
        }
    }

    private void writeCoordinates(String uri, Set<String> coordinatePairs) {
        for (String coordinatePair : coordinatePairs) {
            String[] coordinates = getCoordinatesFromString(coordinatePair);
            if (coordinates.length>1) {
                writeNTLine(uri, this.latProperty, coordinates[0]);
                writeNTLine(uri, this.longProperty, coordinates[1]);
            }
        }
    }

    private void writeDates(String uri, Set<String> dates) {
        for (String date : dates) {
            writeNTLine(uri, this.dateProperty, date);
        }
    }

    private void writeLabels(String uri, Set<String> labels) {
        for (String label : labels) {
            writeNTLine(uri, this.rdfsLabel, label);
        }
    }

    private String[] getCoordinatesFromString(String coordinatePair) {
        String[] coordinates = new String[2];
        if (coordinatePair.indexOf(",") != -1) {
            coordinates[0] = coordinatePair.substring(0, coordinatePair.indexOf(","));
            coordinates[1] = coordinatePair.substring(coordinatePair.indexOf(",") + 1, coordinatePair.length());
        } else {
            System.out.println("No valid coordinate Pair for: "+ coordinatePair);
        }
        return coordinates;
    }

    private void writeNTLine(String s, String p, String o) {
        try {
            this.writer.write("<"+ s + "> " + p + " <" + o + "> .\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeWriter() {
        try {
            this.writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
