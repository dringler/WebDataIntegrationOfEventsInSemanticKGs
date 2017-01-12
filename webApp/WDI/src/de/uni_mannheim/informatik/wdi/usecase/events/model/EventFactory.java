package de.uni_mannheim.informatik.wdi.usecase.events.model;

import de.uni_mannheim.informatik.wdi.model.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.w3c.dom.Node;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * A {@link MatchableFactory} for {@link Event}s.
 *
 * @author Daniel Ringler
 *
 */
public class EventFactory extends MatchableFactory<Event> implements
        FusableFactory<Event, DefaultSchemaElement> {


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
        event.addURI(values[0]);

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

    /**
     * Returns an event with all gathered values (have the same URI)
     * @param gatheredValues
     * @param provenanceInfo
     * @param separator
     * @param dateTimeFormatter
     * @param filterFrom
     * @param fromDate
     * @param filterTo
     * @param toDate
     * @param filterByKeyword
     * @param keyword
     * @return Event
     */
    public Event createModelFromMultpleTSVline(HashSet<String[]> gatheredValues,
                                               String provenanceInfo,
                                               char separator,
                                               DateTimeFormatter dateTimeFormatter,
                                               boolean filterFrom,
                                               LocalDate fromDate,
                                               boolean filterTo,
                                               LocalDate toDate,
                                               boolean filterByKeyword,
                                               String keyword) {

        Event event = null;
        boolean firstLine = true;
        for (String[] values : gatheredValues) {
            if (firstLine) {
                event = new Event(values[0], provenanceInfo);
                firstLine = false;
            }
            //fill the attributes
            //add uri
            event.addURI(values[0]);

            //add label after removing the language tag
            if (values[1].contains("@"))
                event.addLabel(values[1].substring(0, values[1].indexOf("@")));
            else
                event.addLabel(values[1]);


            // 1214-07-27^^http://www.w3.org/2001/XMLSchema#date
            //incomplete date 1863-##-##^^http://www.w3.org/2001/XMLSchema#date
            String date = values[2].replace("##", "01");
            if (values[2].contains("^"))
                date = date.substring(0, date.indexOf("^"));

            try {
                LocalDate localDate = LocalDate.parse(date, dateTimeFormatter);
                //check date against user input parameters
                if (filterFrom) { //&& filterTo) {
                    //if (localDate.isAfter(toDate) || localDate.isBefore(fromDate)) {
                    if (localDate.isBefore(fromDate)) {
                        return null;
                    }
                }
                if (filterTo) {
                    if (localDate.isAfter(toDate)) {
                        return null;
                    }
                }
                event.addDate(localDate);
            } catch (DateTimeParseException e) {
                //System.out.println(values[0] + " " + date);
                return null;
            }



            //50.5833^^http://www.w3.org/2001/XMLSchema#float	3.225^^http://www.w3.org/2001/XMLSchema#float
            //event.setLat(Double.valueOf(values[3].substring(0, values[3].indexOf("^"))));
            //event.setLon(Double.valueOf(values[4].substring(0, values[4].indexOf("^"))));
            if (values.length>4) {
                String latString = values[3];
                if (latString.contains("^"))
                    latString = latString.substring(0, latString.indexOf("^"));
                String longString = values[4];
                if (longString.contains("^"))
                    longString = longString.substring(0, longString.indexOf("^"));

                Pair<Double, Double> p = new Pair<>(
                        Double.valueOf(latString),
                        Double.valueOf(longString)
                );
                event.addCoordinates(p);
            }
            if (values.length>5) {
                event.addSame(values[5]);
            }
            if (values.length>6) {
                Location location = new Location(values[6], provenanceInfo);
                event.addLocation(location);
            }
        }

        //filter labels by keyword
        if(filterByKeyword) {
            if (!event.getLabels().stream().anyMatch(label -> label.trim().toLowerCase().contains(keyword.toLowerCase()))) {
                return null;
            } /*else {
                System.out.println(keyword + " found for " + event.getLabels());
            }*/
        }

        return event;
    }
    @Override
    public Event createInstanceForFusion(RecordGroup<Event, DefaultSchemaElement> cluster) {

        List<String> ids = new LinkedList<>();

        for (Event m : cluster.getRecords()) {
            ids.add(m.getIdentifier());
        }

        Collections.sort(ids);

        String mergedId = StringUtils.join(ids, '+');

        return new Event(mergedId, "fused");
    }

}
