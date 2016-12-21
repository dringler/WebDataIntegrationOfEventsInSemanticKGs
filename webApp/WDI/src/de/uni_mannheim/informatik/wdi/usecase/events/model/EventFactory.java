package de.uni_mannheim.informatik.wdi.usecase.events.model;

import de.uni_mannheim.informatik.wdi.model.MatchableFactory;
import de.uni_mannheim.informatik.wdi.model.Pair;
import org.w3c.dom.Node;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;

/**
 * A {@link MatchableFactory} for {@link Event}s.
 *
 * @author Daniel Ringler
 *
 */
public class EventFactory extends MatchableFactory<Event> {

    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
    @Override
    public Event createModelFromElement(Node node, String provenanceInfo) {
        String id = getValueFromChildElement(node, "id");

        // create the object with id and provenance information
        Event event = new Event(id, provenanceInfo);


        event.addLabel(getValueFromChildElement(node, "labels"));
        //event.setLat(Double.valueOf(getValueFromChildElement(node, "lat")));
        //event.setLon(Double.valueOf(getValueFromChildElement(node, "lon")));

        // convert the date string into a DateTime object
        /*try {
            String date = getValueFromChildElement(node, "date");
            if (date != null) {
                LocalDate dt = LocalDate.parse(date);
                event.setDate(dt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
        return event;
    }
    public Event createModelFromTSVline(String[] values, String provenanceInfo) {
        //values for basic data file: 0:uri, 1:label, 2:date, 3:lat, 4:long
        Event event = new Event(values[0], provenanceInfo);

        //fill the attributes
        event.addLabel(values[1]);

        // 1214-07-27^^http://www.w3.org/2001/XMLSchema#date
        event.addDate(LocalDate.parse(values[2].substring(0, values[2].indexOf("^"))));

        //50.5833^^http://www.w3.org/2001/XMLSchema#float	3.225^^http://www.w3.org/2001/XMLSchema#float
        //event.setLat(Double.valueOf(values[3].substring(0, values[3].indexOf("^"))));
        //event.setLon(Double.valueOf(values[4].substring(0, values[4].indexOf("^"))));
        Pair<Double, Double> p = new Pair<>(
                Double.valueOf(values[3].substring(0, values[3].indexOf("^"))),
                Double.valueOf(values[4].substring(0, values[4].indexOf("^")))
        );
        event.addCoordinates(p);

        event.addSame(values[5]);
        Location location = new Location(values[6], provenanceInfo);
        event.addLocation(location);

        return event;
    }

    public Event createModelFromMultpleTSVline(HashSet<String[]> gatheredValues, String provenanceInfo) {
        Event event = null;
        boolean firstLine = true;
        for (String[] values : gatheredValues) {
            if (firstLine) {
                event = new Event(values[0], provenanceInfo);
                firstLine = false;
            }
            //fill the attributes
            //add label after removing the language tag
            event.addLabel(values[1].substring(0, values[1].indexOf("@")));


            // 1214-07-27^^http://www.w3.org/2001/XMLSchema#date
            //incomplete date 1863-##-##^^http://www.w3.org/2001/XMLSchema#date
            String date = values[2].substring(0, values[2].indexOf("^")).replace("##", "01");
            try {
                event.addDate(LocalDate.parse(date, formatter));
            } catch (DateTimeParseException e) {
                //System.out.println(values[0] + " " + date);
            }

            //50.5833^^http://www.w3.org/2001/XMLSchema#float	3.225^^http://www.w3.org/2001/XMLSchema#float
            //event.setLat(Double.valueOf(values[3].substring(0, values[3].indexOf("^"))));
            //event.setLon(Double.valueOf(values[4].substring(0, values[4].indexOf("^"))));
            Pair<Double, Double> p = new Pair<>(
                    Double.valueOf(values[3].substring(0, values[3].indexOf("^"))),
                    Double.valueOf(values[4].substring(0, values[4].indexOf("^")))
            );
            event.addCoordinates(p);

            event.addSame(values[5]);
            Location location = new Location(values[6], provenanceInfo);
            event.addLocation(location);
        }

        return event;
    }
}
