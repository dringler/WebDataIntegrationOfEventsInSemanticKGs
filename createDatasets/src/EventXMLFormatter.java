import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Daniel on 17/01/17.
 * Based on XMLFormatter, MovieXMLFormatter, and ActorXMLFormatter by Oliver Lehmberg
 * and https://www.mkyong.com/java/how-to-create-xml-file-in-java-dom/
 */
public class EventXMLFormatter {

    public EventXMLFormatter() {}

    //https://www.mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
    public Map<String, Event> readXML(int k, String fileName, boolean returnMap, boolean writeNT, boolean filterDirectSameAsLinksOnly) {
        Map<String,Event> eventMap = new HashMap<>();
        //nt writer
        EventNTFormatter ntFormatter = new EventNTFormatter(k, fileName +"_directLinks.nt");


        try {
            File xmlFile = new File(fileName + ".xml");

            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(xmlFile);

            NodeList eventNodeList = doc.getElementsByTagName("event");

            int eventCounter = 0;
            // for all elements
            for (int eIndex = 0; eIndex < eventNodeList.getLength(); eIndex++) {

                Node eventNode = eventNodeList.item(eIndex);

                if (eventNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eventElement = (Element) eventNode;

                    String uri = eventElement.getAttribute("uri");

                    Event event = readEventElementFromXML(eventElement, uri, xpath, k, filterDirectSameAsLinksOnly);
                    if (event != null) {

                        if (returnMap) {
                            eventMap.put(uri, event);
                        }
                        if (writeNT) {
                            ntFormatter.writeEventToNT(event);
                        }
                    }//end if(event != null)
                } //end if node.getNodeType() == Node.ELEMENT_NODE

                eventCounter++;
                if(eventCounter % (eventNodeList.getLength()/100) == 0) {
                    System.out.println(((float) eventCounter/eventNodeList.getLength())*100 + "%, " + eventCounter + " of " + eventNodeList.getLength() +  " events processed.");
                }


            }//end for all event nodes


        } catch (ParserConfigurationException pce){
            pce.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ntFormatter.closeWriter();
        return eventMap;
    }

    private Event readEventElementFromXML(Element eventElement, String uri, XPath xpath, int k, boolean filterDirectSameAsLinksOnly) {

        Event event = new Event(uri);
        //label
        //NodeList labelNodeList = eventElement.getElementsByTagName("label"); -> gets all label elements (also from the location elements!)

        try {

            //same
            NodeList eventElementNodeList = (NodeList) xpath.evaluate("same", eventElement,
                    XPathConstants.NODESET);
            if (eventElementNodeList.getLength() > 0) {
                for (int i = 0; i < eventElementNodeList.getLength(); i++) {
                    event.addSame(eventElementNodeList.item(i).getTextContent());
                }
            }

            //filter events that do not have a direct link to the other KG
            if (filterDirectSameAsLinksOnly) {
                //check if event has any owl:sameAs links
                if (!event.hasSame()) {
                    return null;
                } else {
                    String otherURI;
                    if (k == 0) {
                        otherURI = "http://yago-knowledge.org/resource/";
                    } else {
                        otherURI = "http://dbpedia.org/resource/";
                    }
                    boolean eventContainsDirectLink = false;
                    for (String same : event.getSames()) {
                        if (same.contains(otherURI)) {
                            eventContainsDirectLink = true;
                            break;
                        }
                    }
                    if (!eventContainsDirectLink) {
                        return null;
                    }
                }
            }

            eventElementNodeList = (NodeList) xpath.evaluate("label", eventElement,
                    XPathConstants.NODESET);
            if (eventElementNodeList.getLength() > 0) {
                for (int i = 0; i < eventElementNodeList.getLength(); i++) {
                    event.addLabel(eventElementNodeList.item(i).getTextContent());
                }
            }

            //date
            eventElementNodeList = (NodeList) xpath.evaluate("date", eventElement,
                    XPathConstants.NODESET);
            if (eventElementNodeList.getLength() > 0) {
                for (int i = 0; i < eventElementNodeList.getLength(); i++) {
                    event.addDate(eventElementNodeList.item(i).getTextContent());
                }
            }

            //coordinates
            eventElementNodeList = (NodeList) xpath.evaluate("coordinates", eventElement,
                    XPathConstants.NODESET);
            if (eventElementNodeList.getLength() > 0) {
                for (int i = 0; i < eventElementNodeList.getLength(); i++) {
                    event.addCoordinatePair(eventElementNodeList.item(i).getTextContent());
                }
            }



            //location
            eventElementNodeList = (NodeList) xpath.evaluate("locations/location", eventElement, XPathConstants.NODESET);
            if (eventElementNodeList.getLength() > 0) {
                for (int j = 0; j < eventElementNodeList.getLength(); j++) {

                    Element locationElement = (Element) eventElementNodeList.item(j);
                    //get location URI
                    String locationURI = locationElement.getAttribute("uri");
                    //create new location element
                    Location location = new Location(locationURI);

                    //labels
                    NodeList locationElementNodeList = (NodeList) xpath.evaluate("label", locationElement,
                            XPathConstants.NODESET);
                    if (locationElementNodeList.getLength() > 0) {
                        for (int i = 0; i < locationElementNodeList.getLength(); i++) {
                            location.addLabel(locationElementNodeList.item(i).getTextContent());
                        }
                    }
                    //coordinates
                    locationElementNodeList = (NodeList) xpath.evaluate("coordinates", locationElement,
                            XPathConstants.NODESET);
                    if (locationElementNodeList.getLength() > 0) {
                        for (int i = 0; i < locationElementNodeList.getLength(); i++) {
                            location.addCoordinatePair(locationElementNodeList.item(i).getTextContent());
                        }
                    }

                    //same
                    locationElementNodeList = (NodeList) xpath.evaluate("same", locationElement,
                            XPathConstants.NODESET);
                    if (locationElementNodeList.getLength() > 0) {
                        for (int i = 0; i < locationElementNodeList.getLength(); i++) {
                            location.addSame(locationElementNodeList.item(i).getTextContent());
                        }
                    }

                    //add location to event
                    event.addLocation(location);

                }//end for each location
            }//end check location length

        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return event;
    }

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
