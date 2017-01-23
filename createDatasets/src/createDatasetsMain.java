import org.apache.jena.query.*;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import java.io.*;
import java.util.*;

/**
 * Created by Daniel on 06/12/16.
 */
public class createDatasetsMain {
    public static void main(String[] args) {
        // PARAMETERS
        boolean testing = true;

        boolean dbpedia = false;
        boolean yago = true;
        //boolean wikidata = false;
        int k; //0 for DBpedia, 1 for YAGO, 2 for Wikidata

        boolean secondOrderP = true; //second order can only be specified if getOptionalP is true

        String fileName;
        //List<String> secondOrderFileNames = new ArrayList<>();
        String header;
        //String secondOrderHeader = "uri\tlabel\tlat\tlong\tsame";//"uri\ttype\tlat\tlong\tsame";

        //configure log4j
        org.apache.log4j.BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel(Level.OFF); //set console logger off


        int lineProgress = 500;

        header = "uri\tlabel\tdate\tlat\tlong\tsame\tlocation\tlocationLat\tlocationLong\tlocationsame";//\tlocation\tcity\tterritory";

        if (dbpedia) {
            k=0;
            QueryObject dQ = new QueryObject("http://dbpedia.org/sparql");

            if (dQ.testConnection()) {
                //specify fileNames and csv header
                fileName = "out/dbpedia_events.xml";
                //fileName = "out/dbpedia-full.tsv";
                //header = "uri\tlabel\tdate\tlat\tlong\tsame\tplace";//\tlocation\tcity\tterritory";
                //secondOrderHeader = "uri\ttype\tlat\tlong\tsame";

                //secondOrderFileNames.add("out/dbpedia-2-place.tsv");

                //secondOrderFileNames.add("out/dbpedia-2-location.tsv");
                //secondOrderFileNames.add("out/dbpedia-2-city.tsv");
                //secondOrderFileNames.add("out/dbpedia-2-territory.tsv");


                KGVariableNames dbpediaVarNames = new KGVariableNames(k);
                //get event instances from DBpedia

                //HashSet<String> eventInstances = getEventInstances(k, dQ, dbpediaVarNames, testing);
                String eventInstancesFileName = "dEventInstanceURIs.csv";

                HashSet<String> eventInstances = getEventInstancesFromFile(eventInstancesFileName, testing);              System.out.println(eventInstances.size() + " distinct instances received from " + eventInstancesFileName);
                //HashSet<String> eventInstances = getEventInstancesFromDBpediaTable();
                System.out.println(eventInstances.size() + " distinct instances read.");
                
                // get event instance properties including location properties
 //               Map<String, Event> dEvents = getEventInstanceProperties(k, dQ, dbpediaVarNames, eventInstances, testing);
                
  //              writeXML(dEvents, fileName);

                // 1 get and write event instances properties
                //getAndWriteEventInstancePropertiesToFile(k, dQ, dQ.getService(), dbpediaVarNames, eventInstances, fileName, header, lineProgress);

                // 2 get and write second order file
                /*if (secondOrderP) {
                    getAndWritePlaceInstancePropertiesToFile(k, dQ, dQ.getService(), dbpediaVarNames, secondOrderFileNames, secondOrderHeader, lineProgress, testing);
                }*/
            }
        }
        //secondOrderFileNames.clear();
        if (yago) {
            k = 1;

            QueryObject yQ = new QueryObject("https://linkeddata1.calcul.u-psud.fr/sparql");
            if (yQ.testConnection()) {
                //specify fileNames and csv header
                //header = "uri\tlabel\tdate\tlat\tlong\tsame\tplace";
                //fileName = "out/yago-full.tsv";
                fileName = "out/yago_events.xml";

               // secondOrderFileNames.add("out/yago-2-happenedIn.tsv");
                //secondOrderFileNames.add("out/yago-2-isLocatedIn.tsv");

                KGVariableNames yagoVarNames = new KGVariableNames(k);
                //get event instances from YAGO
                String eventInstancesFileName = "yEventInstanceURIs.csv";

                //read event instances
                //a) testing
                /*HashSet<String> eventInstances = new HashSet<>();
                fileName = "out/yago_events_test.xml";
                eventInstances.add("http://yago-knowledge.org/resource/Battle_of_Bouvines");
                eventInstances.add("http://yago-knowledge.org/resource/Battle_of_Waterloo");
                eventInstances.add("http://yago-knowledge.org/resource/1974_FIFA_World_Cup");
                eventInstances.add("http://yago-knowledge.org/resource/ar/يا_أنا_يا_خالتي_(فيلم)");
                eventInstances.add("http://yago-knowledge.org/resource/Benji's_Very_Own_Christmas_Story");
                */

                //b) dynamic from SPARQL
                //HashSet<String> eventInstances = getEventInstances(k, yQ, yagoVarNames, testing);
                //System.out.println(eventInstances.size() + " distinct instances received from " + yQ.getService());
                //writeEventInstancesToCSV(eventInstances, eventInstancesFileName);



                //c) from file
                HashSet<String> eventInstances = getEventInstancesFromFile(eventInstancesFileName, testing);
                System.out.println(eventInstances.size() + " distinct instances received from " + eventInstancesFileName);



                // get event instance properties including location properties
                Map<String, Event> yEvents = getEventInstanceProperties(k, yQ, yagoVarNames, eventInstances, testing);

                writeXML(yEvents, fileName);
                // 1 get event instances properties
                //getAndWriteEventInstancePropertiesToFile(k, yQ, yQ.getService(), yagoVarNames, eventInstances, fileName, header, lineProgress);

                // 2 get and write second order file
                /*if (secondOrderP) {
                    getAndWritePlaceInstancePropertiesToFile(k, yQ, yQ.getService(), yagoVarNames, secondOrderFileNames, secondOrderHeader, lineProgress, testing);
                }*/


            }


        }

        /*if (wikidata) {
            k = 2;
            service = "https://query.wikidata.org/sparql";
            dbIsUp = testConnection(service);
            if (dbIsUp) {

            }
        }*/

    }

    private static void writeEventInstancesToCSV(HashSet<String> eventInstances, String fileName) {
        try {
            PrintWriter writer = new PrintWriter(fileName, "UTF-8");
            int counter = 0;
            for (String event : eventInstances) {
                writer.println(event);
                counter++;
            }
            writer.close();
            System.out.println(counter + " lines written to " + fileName);
        } catch (IOException e) {
            System.out.println("error while writing to file " + fileName);
        }
    }
    //417126 distinct events received that do not contain quotes
    //416,924
    private static HashSet<String> getEventInstancesFromFile(String fileName, boolean testing) {
        HashSet<String> eventInstances = new HashSet<>(63100, 0.9f);
        int lineCounter = 0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = reader.readLine()) != null) {
                if (eventInstances.contains(line)) {
                    System.out.println(line + " already contained in set");
                }
                eventInstances.add(line);
                lineCounter++;
                //if (testing && lineCounter > 24)
                //    break;
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            System.out.println(fileName + " not found");
        }
        System.out.println(lineCounter + " lines read");
        return eventInstances;

    }

    private static void writeXML(Map<String, Event> eventMap, String fileName) {
        EventXMLFormatter xmlFormatter = new EventXMLFormatter();
        boolean written = xmlFormatter.parseAndWriteXML(eventMap, fileName);
        if (written)
            System.out.println("results written to " + fileName);


    }

    private static Map<String, Event> getEventInstanceProperties(int k,
                                                          QueryObject queryObject,
                                                          KGVariableNames varNames,
                                                         HashSet<String> eventInstances,
                                                                 boolean testing) {
        float loadFactor = 0.9f;
        //init set capacity: distinct URIS / loadFactor (little bit more)
        int eventSetCapacity = 63035; //dbpedia: 56729 distinct events
        int placeURICapacity = 10755; //dbpedia: 9677 distinct places

        if (k==1) {
            eventSetCapacity = 463640; //yago: 417272 distinct events
            placeURICapacity = 11850; //yago: 10657 distinct places
        }

        //set of all events
        Map<String, Event> eventMap = new HashMap<>(eventSetCapacity, loadFactor);

        //set of all distinct locationURIs
        HashSet<String> locationURIs = new HashSet<>(placeURICapacity, loadFactor);

        //k: placeURI, v: Set of eventURIs
        HashMap<String, Set<String>> locationWithEventsMap= new HashMap<>(placeURICapacity, loadFactor);

        int counter = 0;
        // get properties for each event and add event to eventSet
        for (String eventURI : eventInstances) {
            if (!testing) {
                //create new event object
                Event event = new Event(eventURI);
                //get event properties
                ResultSet results = getEventInstancePropertiesResultSet(k, queryObject, varNames, eventURI);
                if (results != null) {
                    while (results.hasNext()) {
                        QuerySolution qs = results.next();
                        //english label always present
                        event.addLabel(qs.get("label").toString());
                        //others are optional
                        if (qs.contains("date"))
                            event.addDate(qs.get("date").toString());
                        if (qs.contains("lat") && qs.contains(("long")))
                            event.addCoordinatePair(qs.get("lat").toString() + "," + qs.get("long").toString());
                        if (qs.contains("same"))
                            event.addSame(qs.get("same").toString());
                        if (qs.contains("location")) {
                            String locationString = qs.get("location").toString();
                            locationURIs.add(locationString);
                            if (locationWithEventsMap.containsKey(locationString)) {
                                locationWithEventsMap.get(locationString).add(eventURI);
                            } else {
                                //create new k,v pair
                                HashSet<String> locationEvents = new HashSet<>();
                                locationEvents.add(eventURI);
                                locationWithEventsMap.put(locationString, locationEvents);
                            }
                        }
                    }
                    eventMap.put(eventURI, event);


                } else {
                    System.out.println("results==null for " + eventURI);
                }
            } else {
                //testing
                ResultSet results = testURI(k, queryObject, eventURI);
                if (results == null) {
                    System.out.println(eventURI + " returns no results");
                }
            }
            counter++;
            printProgress(counter, eventInstances);
        }
        //done adding all direct properties to the events
        System.out.println(eventMap.size() + " events added to eventMap");


        // for each location
        counter = 0;
        for (String locationURI : locationURIs) {
            if (!testing) {
                Location location = new Location(locationURI);
                //get location properties
                ResultSet results = getLocationInstancePropertiesResultSet(k, queryObject, varNames, locationURI);
                //process all results
                if (results != null) {
                    while (results.hasNext()) {
                        QuerySolution qs = results.next();
                        //english label always present
                        location.addLabel(qs.get("label").toString());
                        //add optional properties
                        if (qs.contains("lat") && qs.contains(("long")))
                            location.addCoordinatePair(qs.get("lat").toString() + "," + qs.get("long").toString());
                        if (qs.contains("same"))
                            location.addSame(qs.get("same").toString());
                    }
                }

                //add location to all events with this location
                for (String eventURI : locationWithEventsMap.get(locationURI)) {
                    eventMap.get(eventURI).addLocation(location);
                }
            } else {
                //testing
                ResultSet results = testURI(k, queryObject, locationURI);
                if (results == null) {
                    System.out.println(locationURI + " returns no results");
                }
            }
            counter++;
            printProgress(counter, locationURIs);
        }
        System.out.println(locationURIs.size() + " locations processed.");
        return eventMap;

    }

    private static void printProgress(int counter, HashSet<String> eventInstances) {
        if (counter % (float) (eventInstances.size() / 100) == 0) {
            System.out.println(
                    ((float) counter / eventInstances.size()) +
                            " of event Instances processed.");
        }
    }

    //test if JENA is able to process the URI
    private static ResultSet testURI(int k, QueryObject queryObject, String uri) {
        String queryString = getQueryPrefix(k);

        queryString = queryString +
                "SELECT ?label WHERE {\n" +
                "<" + uri +"> rdfs:label ?label .\n" +
                "}";
        return queryObject.queryEndpoint(queryString);

    }


/*
    private static HashSet<String> getEventInstancesFromDBpediaTable() {
        String fileName = "/Users/curtis/MT/DBpediaTables/EventInstanceURIs.csv";
        HashSet<String> eventInstanceURIs = new HashSet<>();
        try {
            Scanner scanner = new Scanner(new FileReader(fileName));
            while(scanner.hasNext()){
                eventInstanceURIs.add(scanner.next());
            }
        } catch (IOException e){
            System.out.println("error wile reading " + fileName);
        }

    return eventInstanceURIs;
    }
    */

    /**
     * get and write the location instance properties to file
     * @param k 0:DBpedia, 1:YAGO, 2:Wikidata
     * @param queryObject
     * @param service sparql url
     * @param varNames
     * @param secondOrderFileNames
     * @param secondOrderHeader
     * @param lineProgress print message after lineProgress lines
     */
    private static void getAndWritePlaceInstancePropertiesToFile(int k, QueryObject queryObject, String service, KGVariableNames varNames, List<String> secondOrderFileNames, String secondOrderHeader, int lineProgress, boolean testing) {

        for (String secondOrderFileName : secondOrderFileNames) {
            String propertyName = getPropertyName(secondOrderFileName);
            HashSet<String> placeInstances = getLocationInstances(k, queryObject, varNames, propertyName, testing);
            System.out.println(placeInstances.size() + " distinct "+  propertyName + " instances received from " + service);
            int lineCounter = 0;
            int writtenLines = 0;
            try {
                PrintWriter writer2 = new PrintWriter(secondOrderFileName, "UTF-8");
                //write header
                writer2.println(secondOrderHeader);

                HashSet<String> placeInstancesProperties = new HashSet<>();
                for (String placeInstance : placeInstances) {
                    placeInstancesProperties.addAll(getLocationInstanceProperties(k, queryObject, varNames, placeInstance));
                    //write lines to file
                    placeInstancesProperties.forEach(writer2::println);
                    writtenLines += placeInstancesProperties.size();
                    placeInstancesProperties.clear();
                    lineCounter = increaseLineCounter(lineCounter, lineProgress);
                }
                writer2.close();
                System.out.println(writtenLines + " lines written to " + secondOrderFileName);

            } catch (IOException e) {
                System.out.println("error while writing to file " + secondOrderFileName);
            }
            placeInstances.clear();
        }
    }

    private static String getPropertyName(String secondOrderFileName) {
        //secondOrderFileNames.add("out/dbpedia-2-territory.tsv");
        //secondOrderFileNames.add("out/yago-2-happenedIn.tsv");

        return secondOrderFileName.substring(
                secondOrderFileName.indexOf("2-")+2,
                secondOrderFileName.indexOf(".tsv"));
    }

    /**
     * get and write the event instance properties to file
     * @param k 0:DBpedia, 1:YAGO, 2:Wikidata
     * @param queryObject
     * @param service sparql url
     * @param varNames
     * @param eventInstances URIs of all distinct event instances
     * @param fileName
     * @param header
     * @param lineProgress print message after lineProgress lines
     */
    private static void getAndWriteEventInstancePropertiesToFile(int k, QueryObject queryObject, String service, KGVariableNames varNames, HashSet<String> eventInstances, String fileName, String header, int lineProgress) {
        int lineCounter = 0;
        int writtenLines = 0;
        try {
            PrintWriter writer = new PrintWriter(fileName, "UTF-8");
            //write header
            writer.println(header);
            //get the properties for each event instance
            HashSet<String> eventInstanceProperties = new HashSet<>();
            for (String instance : eventInstances) {
                //get all properties for the instance
                if (!instance.contains("\"")) {
                    eventInstanceProperties.addAll(getInstanceProperties(k, queryObject, varNames, instance));
                    //write all lines to file
                    eventInstanceProperties.forEach(writer::println);
                    writtenLines += eventInstanceProperties.size();
                    //clear set of lines that have been written to file
                    eventInstanceProperties.clear();
                    lineCounter = increaseLineCounter(lineCounter, lineProgress);
                } else {
                    System.out.println("Instance contains quote: " + instance);
                }
            }
            writer.close();
            System.out.println(writtenLines + " lines written to " + fileName);
        } catch (IOException e) {
            System.out.println("error while writing to file " + fileName);
        }

    }
    /**
     * Increment the lineCounter and print a console log if lineProgress is reached
     * @param lineProgress
     * @return incremented lineCounter value
     */
    private static int increaseLineCounter(int lineCounter, int lineProgress) {
        int newLineCounterValue = lineCounter + 1;
        if (newLineCounterValue % lineProgress == 0) {
            System.out.println(newLineCounterValue + " instances processed.");
        }
        return newLineCounterValue;
    }

    private static ResultSet getLocationInstancePropertiesResultSet(int k, QueryObject queryObject, KGVariableNames varNames, String placeURI) {
        String queryString = getQueryPrefix(k);
        queryString = queryString +
                "SELECT ?label ?lat ?long ?same WHERE {\n" + //type, country, city, ...
                "<" + placeURI +"> rdfs:label ?label .\n"+
                //" OPTIONAL { <" + placeInstance +"> a ?type }\n"+
                " OPTIONAL { <" + placeURI + "> " + varNames.getLatVar() + " ?lat }\n" +
                " OPTIONAL { <" + placeURI +"> " + varNames.getLongVar() + " ?long }\n"+
                " OPTIONAL { <" + placeURI +"> owl:sameAs ?same }\n"+
                " FILTER langMatches( lang(?label), \'" + varNames.getEnVar() + "\' ) }";
        return queryObject.queryEndpoint(queryString);
    }
    /**
     * Get all location instance properties. Might (optional) be the following:
     * geo:lat, geo:long, owl:sameAs.
     * @param k 0:DBpedia, 1:YAGO, 2:Wikidata
     * @param queryObject
     * @param varNames
     *@param locationInstance URI of the instance  @return HashSet<String> including all instance properties
     */
    private static HashSet<String> getLocationInstanceProperties(int k, QueryObject queryObject, KGVariableNames varNames, String locationInstance) {
        //String geoNamesURL = "http://sws.geonames.org";
        //String otherKgURL = varNames.getOtherKgURL();

        HashSet<String> resultSet = new HashSet<>();

        ResultSet results = getLocationInstancePropertiesResultSet(k, queryObject, varNames, locationInstance);

        //ResultSetFormatter.outputAsXML(System.out, results);


        List<String> properties = new ArrayList<>();
        properties.add("label");
       // properties.add("type");
        properties.add("lat");
        properties.add("long");
        properties.add("same");
        String resultString = "";
        boolean oneLineAdded = false;
        while (results.hasNext()) {
            QuerySolution qs = results.next();
            resultString =  locationInstance + getAvailableOptionalProperties(qs, properties);

            //only add sameAs if they link to yago, dbpedia, or geonames BUT add at least one line with lat&long if no sameAs link goes to yago, dbpedia, or geonames
            /*if (qs.contains("same")) {
                String sameAs = qs.get("same").toString();
                if (sameAs.startsWith(geoNamesURL) || sameAs.startsWith(otherKgURL)) {
                    resultSet.add(resultString);
                    oneLineAdded = true;
                }

            }  else {
                resultSet.add(resultString);
                oneLineAdded = true;
            }*/
            resultSet.add(resultString);
            oneLineAdded = true;
        }
        //check if at least one line was added
        if (!oneLineAdded) {
            resultSet.add(resultString);
        }
        return resultSet;
    }

    /**
     * Get all distinct location instances that appear as location object for an event instance.
     * @param k 0:DBpedia, 1:YAGO, 2:Wikidata
     * @param queryObject
     * @param varNames
     * @param propertyName
     * @return HashSet<String> including all location instance URIs
     */
    private static HashSet<String> getLocationInstances(int k, QueryObject queryObject, KGVariableNames varNames, String propertyName, boolean testing) {
        HashSet<String> instanceSet = new HashSet<>();
        String queryString = getQueryPrefix(k);
        queryString = queryString +
                "SELECT DISTINCT ?location WHERE {\n" +
                " ?event a " + varNames.getEventClass() + " .\n" +
                " ?event rdfs:label ?label .\n" +
                " ?event " + varNames.getLocationVar() + " ?location .\n" +
                //" ?event " + varNames.getDateVar() + " ?date .\n" +
                //comment if optional
                //" ?event " + varNames.getLatVar() + "?lat .\n" +
                //" ?event " + varNames.getLongVar() + " ?long .\n" +
                //" ?event " + varNames.getPlaceVar(propertyName) + " ?location .\n" +
                " FILTER langMatches( lang(?label), \'" + varNames.getEnVar() + "\' )\n" +
                "}";
        if (testing) {
            queryString = queryString + " LIMIT 25";
        }

        ResultSet results = queryObject.queryEndpoint(queryString);
        while (results.hasNext()) {
            QuerySolution qs = results.next();
            if (qs.get("location").isURIResource()) {
                instanceSet.add(qs.get("location").toString());
                //System.out.println(qs.get("event").toString());
            }
            //System.out.println(qs.get("count").toString());
        }

        return instanceSet;
    }

    /**
     * Get all Instance properties
     * DBpedia: rdfs:label, dbo:date, and optionally: geo:lat, geo:long, owl:sameAs, dbo:place
     * @param k 0:DBpedia, 1:YAGO, 2:Wikidata
     * @param queryObject
     * @param varNames
     */
    private static HashSet<String> getInstanceProperties(int k, QueryObject queryObject, KGVariableNames varNames, String instance) {
        //get variable names
        List<String> optionalProperties = new ArrayList<>();
        //optionalProperties.add("lat");
        //optionalProperties.add("long");
        //optionalProperties.add("same");
        //optionalProperties.add("place");
        //optionalProperties.add("location");

        HashSet<String> resultSet = new HashSet<>();


        ResultSet results = getEventInstancePropertiesResultSet(k, queryObject, varNames, instance);



        String resultString;

        while (results.hasNext()) {
            QuerySolution qs = results.next();
            //resultString = instance + "\t" + qs.get("label").toString() + "\t" + qs.get("date").toString();
            resultString = instance + "\t" + qs.get("label").toString() + "\t" + qs.get("date").toString() + "\t" + qs.get("lat").toString() + "\t" + qs.get("long").toString();
            resultString = resultString + getAvailableOptionalProperties(qs, optionalProperties);

            resultSet.add(resultString);

        }

        return resultSet;
    }

    private static ResultSet getEventInstancePropertiesResultSet(int k, QueryObject queryObject, KGVariableNames varNames, String instance) {
        String queryString = getQueryPrefix(k);

        queryString = queryString +
                "SELECT ?label ?date ?lat ?long ?same ?location";

        /*if (k==0) {
            queryString = queryString + " ?city ?territory";
            optionalProperties.add("city");
            optionalProperties.add("territory");
        }*/

        queryString = queryString +
                " WHERE {\n" +
                " <" + instance + "> rdfs:label ?label .\n" +
                //" <" + instance + "> " + varNames.getDateVar() + " ?date .\n" +
                //" <" + instance + "> " + varNames.getLatVar() + " ?lat .\n" +
                //" <" + instance + "> " + varNames.getLongVar() + " ?long .\n"+
                " OPTIONAL { <" + instance + "> " + varNames.getDateVar() + " ?date }\n" +
                " OPTIONAL { <" + instance + "> " + varNames.getLatVar() + " ?lat }\n" +
                " OPTIONAL { <" + instance + "> " + varNames.getLongVar() + " ?long }\n"+
                " OPTIONAL { <" + instance + "> owl:sameAs ?same }\n"+
                " OPTIONAL { <" + instance + "> " + varNames.getLocationVar() + " ?location }\n";
        // " OPTIONAL { <" + instance + "> " + varNames.getPlace2Var() + " ?location }\n";

      /*  if (k==0) {
            queryString = queryString +
                    " OPTIONAL { <" + instance + "> dbo:city ?city }\n"+
                    " OPTIONAL { <" + instance + "> dbo:territory ?territory }\n";
        }*/
        queryString = queryString +
                " FILTER langMatches( lang(?label), \'" + varNames.getEnVar() + "\' )\n" +
                "}";
        //System.out.println(instance);
        return queryObject.queryEndpoint(queryString);
    }

    /**
     * Get the optional properties of the QuerySolution.
     * Check if the QuerySolution has the provided properties. Insert "null" otherwise.
     * @param qs
     * @param optionalProperties to check
     * @return String (tab-separated with property values or "null" if property value is not available)
     */
    private static String getAvailableOptionalProperties(QuerySolution qs, List<String> optionalProperties) {
        String oP = "";
        for (String p : optionalProperties) {
            //get value for property or add "null" is property is null
            oP = (qs.contains(p)) ? oP + "\t" + qs.get(p).toString() : oP + "\tnull";
        }
        return oP;
    }

    /**
     * Get all distinct event instances that have the following attributes:
     * rdfs:label, dbo:date, geo:lat, geo:long
     * @param k 0:DBpedia, 1:YAGO, 2:Wikidata
     * @param queryObject
     * @param varNames
     * @return HashSet<String> including all instance URIs
     */
    private static HashSet<String> getEventInstances(int k, QueryObject queryObject, KGVariableNames varNames, boolean testing) {
        HashSet<String> instanceSet = new HashSet<>();
        String queryString = getQueryPrefix(k);

        queryString = queryString +
                    //"SELECT (COUNT(DISTINCT ?event) as ?count) WHERE {\n" +
                    "SELECT DISTINCT ?event WHERE {\n" +
                    " ?event a " + varNames.getEventClass() + " .\n" +
                    " ?event rdfs:label ?label .\n" +
                //comment if properties should be OPTIONAL
                    //" ?event " + varNames.getDateVar() +" ?date .\n" +
                    //" ?event " + varNames.getLatVar() + "?lat .\n" +
                    //" ?event " + varNames.getLongVar() + " ?long .\n" +
                    " FILTER langMatches( lang(?label), \'" + varNames.getEnVar() + "\' )\n" +
                    "}";

        if (testing) {
            queryString = queryString + " LIMIT 200";
        }

        ResultSet results = queryObject.queryEndpoint(queryString);

        while (results.hasNext()) {
            QuerySolution qs = results.next();
            if (qs.get("event").isURIResource()) {
                String eventURI = qs.get("event").toString();
                if (!eventURI.contains("\"") && !eventURI.contains("`")) {
                    instanceSet.add(eventURI);
                } //else {
                   // System.out.println(eventURI + " could not be added as the URI contains quotes");
                //}
                //System.out.println(qs.get("event").toString());
            }
            //System.out.println(qs.get("count").toString());
        }

        return instanceSet;
    }
    /**
     * Get query prefix
     * @param k 0:DBpedia, 1:YAGO, 2:Wikidata
     * @return String of the query prefix
     */
    private static String getQueryPrefix(int k) {
        String p = "";
        if (k==0) {
            p =     "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                    //"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
                    "PREFIX dbo: <http://dbpedia.org/ontology/>\n" +
                    "PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\n";
                    // "PREFIX georss: <http://www.georss.org/georss/>\n"+
                    //"PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n";

        } else if (k==1) {
            p =     "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                    //"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
                    "PREFIX yago: <http://yago-knowledge.org/resource/>\n";
                    //"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n";
        }
        /*else if (k==2) {
            p =     "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                    //"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
                    "PREFIX wdt: <http://www.wikidata.org/prop/direct/>\n" +
                    "PREFIX wd: <http://www.wikidata.org/entity/>\n" +
                    "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n";
        }*/
        return p;
    }


}
