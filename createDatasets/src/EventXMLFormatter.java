import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.Map;
import java.util.Set;

/**
 * Created by Daniel on 17/01/17.
 * Based on XMLFormatter, MovieXMLFormatter, and ActorXMLFormatter by Oliver Lehmberg
 * and https://www.mkyong.com/java/how-to-create-xml-file-in-java-dom/
 */
public class EventXMLFormatter {

    public EventXMLFormatter() {}

    public boolean parseAndWriteXML(Map<String, Event> eventMap, String fileName) {

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            //create doc
            Document doc = docBuilder.newDocument();
            //add root element: event
            Element rootElementEvents = doc.createElement("events");
            doc.appendChild(rootElementEvents);

            //for each event
            for (Event event : eventMap.values()) {
                //create event
                Element eventElement = doc.createElement("event");
                rootElementEvents.appendChild(eventElement);

                //set uri as attribute value
                /*Attr eventURI = doc.createAttribute("uri");
                eventURI.setValue(event.getUri());
                eventElement.setAttributeNode(eventURI);*/
                eventElement.setAttribute("uri", event.getUri());

                // ADD ELEMENTS: label, date, coordinates, same
                addElements(doc, eventElement, "label", event.getLabels());
                addElements(doc, eventElement, "date", event.getDates());
                addElements(doc, eventElement, "coordinates", event.getCoordinatePairs());
                addElements(doc, eventElement, "same", event.getSames());

                // add location hierarchy
                Element root2ElementPlaces = doc.createElement("locations");
                eventElement.appendChild(root2ElementPlaces);
                //ADD LOCATIONS
                for (Location location : event.getLocations()) {
                    if (location.getLabels().size() > 0) {
                        Element locationElement = doc.createElement("location");
                        root2ElementPlaces.appendChild(locationElement);
                        locationElement.setAttribute("uri", location.getUri());

                        //ADD ELEMENTS: label, coordinates, sames
                        addElements(doc, locationElement, "label", location.getLabels());
                        addElements(doc, locationElement, "coordinates", location.getCoordinatePairs());
                        addElements(doc, locationElement, "same", location.getSames());
                    }
                }
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(fileName));
            //StreamResult result = new StreamResult(System.out);
            transformer.transform(source, result);

            return true;
        } catch (ParserConfigurationException pce){
            pce.printStackTrace();
        } catch (TransformerConfigurationException tce) {
            tce.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void addElements(Document doc, Element mainElement, String elementName, Set<String> values) {
        for (String value : values) {
            Element element = doc.createElement(elementName);
            element.appendChild(doc.createTextNode(value));
            mainElement.appendChild(element);
        }
    }


}
