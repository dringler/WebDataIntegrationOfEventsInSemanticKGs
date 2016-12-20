package de.uni_mannheim.informatik.wdi.usecase.events.model;

import de.uni_mannheim.informatik.wdi.model.MatchableFactory;
import de.uni_mannheim.informatik.wdi.model.Pair;
import org.w3c.dom.Node;
import java.time.LocalDate;

/**
 * A {@link MatchableFactory} for {@link Event}s.
 *
 * @author Daniel Ringler
 *
 */
public class EventFactory extends MatchableFactory<Event> {
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

        return event;
    }
}
