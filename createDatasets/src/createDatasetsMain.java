import org.apache.jena.query.*;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by curtis on 06/12/16.
 */
public class createDatasetsMain {
    public static void main(String[] args) {
        // PARAMETERS
        boolean dbpedia = true;
        boolean yago = true;
        boolean wikidata = false;
        int k; //0 for DBpedia, 1 for YAGO, 2 for Wikidata

        boolean getOptionalP = true;
        boolean secondOrderP = true; //second order can only be specified if getOptionalP is true

        String fileName;
        String secondOrderFileName;
        String header;
        String secondOrderHeader;

        if (secondOrderP && !getOptionalP) {
            System.out.println("Set getOptionalP and secondOrderP to true. secondOrderP cannot be executed without getting the optional properties");
            return;
        }


        //configure log4j
        org.apache.log4j.BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel(Level.OFF); //set console logger off

        String service;
        boolean dbIsUp;
        int lineProgress = 500;
        //HashSet<String> xInstancesWithProperties;

        if (dbpedia) {
            k=0;
            service = "http://dbpedia.org/sparql";
            dbIsUp = testConnection(service);
            if (dbIsUp) {
                //specify fileNames and csv header
                header = "uri\tlabel\tdate\tlat\tlong";
                if (!getOptionalP) {
                    fileName = "out/dbpedia-1-basic.tsv";
                } else {
                    fileName = "out/dbpedia-2-wOptional.tsv";
                    header = header + "\tlabel2\tsame\tplace";
                }
                secondOrderFileName = "out/dbpedia-3-secondOrder.tsv";
                secondOrderHeader = "place\tpLat\tpLong\tpSame";

                KGVariableNames dbpediaVarNames = new KGVariableNames(k);
                //get event instances from DBpedia
                HashSet<String> eventInstances = getEventInstances(k, service, dbpediaVarNames);
                System.out.println(eventInstances.size() + " distinct instances received from " + service);
                // 1 get and write event instances properties
                getAndWriteEventInstancePropertiesToFile(k, service, dbpediaVarNames, getOptionalP, eventInstances, fileName, header, lineProgress);

                // 2 get and write second order file
                if (secondOrderP) {
                    getandWritePlaceInstancePropertiesToFile(k, service, dbpediaVarNames, secondOrderFileName, secondOrderHeader, lineProgress);
                }
            }
        }
        if (yago) {
            k = 1;
            service = "https://linkeddata1.calcul.u-psud.fr/sparql";
            dbIsUp = testConnection(service);
            if (dbIsUp) {
                //specify fileNames and csv header
                header = "uri\tlabel\tdate\tlat\tlong";
                if (!getOptionalP) {
                    fileName = "out/yago-1-basic.tsv";
                } else {
                    fileName = "out/yago-2-wOptional.tsv";
                    header = header + "\tlabel2\tsame\tplace";
                }
                secondOrderFileName = "out/yago-3-secondOrder.tsv";
                secondOrderHeader = "place\tpLat\tpLong\tpSame";

                KGVariableNames yagoVarNames = new KGVariableNames(k);
                //get event instances from YAGO
                HashSet<String> eventInstances = getEventInstances(k, service, yagoVarNames);
                System.out.println(eventInstances.size() + " distinct instances received from " + service);

                // 1 get event instances properties
                getAndWriteEventInstancePropertiesToFile(k, service, yagoVarNames, getOptionalP, eventInstances, fileName, header, lineProgress);

                // 2 get and write second order file
                if (secondOrderP) {
                    getandWritePlaceInstancePropertiesToFile(k, service, yagoVarNames, secondOrderFileName, secondOrderHeader, lineProgress);
                }


            }


        }


        if (wikidata) {
            k = 2;
            service = "https://query.wikidata.org/sparql";
            dbIsUp = testConnection(service);
            if (dbIsUp) {

            }
        }


    }
    /**
     * get and write the place instance properties to file
     * @param k 0:DBpedia, 1:YAGO, 2:Wikidata
     * @param service sparql url
     * @param varNames
     * @param secondOrderFileName
     * @param secondOrderHeader
     * @param lineProgress print message after lineProgress lines
     */
    private static void getandWritePlaceInstancePropertiesToFile(int k, String service, KGVariableNames varNames, String secondOrderFileName, String secondOrderHeader, int lineProgress) {
        HashSet<String> placeInstances = getPlaceInstances(k, service, varNames);
        System.out.println(placeInstances.size() + " distinct place instances received from " + service);
        int lineCounter = 0;
        int writtenLines = 0;
        try {
            PrintWriter writer2 = new PrintWriter(secondOrderFileName, "UTF-8");
            //write header
            writer2.println(secondOrderHeader);

            HashSet<String> placeInstancesProperties = new HashSet<>();
            for (String placeInstance : placeInstances) {
                placeInstancesProperties.addAll(getPlaceInstanceProperties(k, service, varNames, placeInstance));
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
    }

    /**
     * get and write the event instance properties to file
     * @param k 0:DBpedia, 1:YAGO, 2:Wikidata
     * @param service sparql url
     * @param varNames
     * @param getOptionalP boolean
     * @param eventInstances URIs of all distinct event instances
     * @param fileName
     * @param header
     * @param lineProgress print message after lineProgress lines
     */
    private static void getAndWriteEventInstancePropertiesToFile(int k, String service, KGVariableNames varNames, boolean getOptionalP, HashSet<String> eventInstances, String fileName, String header, int lineProgress) {
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
                eventInstanceProperties.addAll(getInstanceProperties(k, service, varNames, instance, getOptionalP));
                //write all lines to file
                eventInstanceProperties.forEach(writer::println);
                writtenLines += eventInstanceProperties.size();
                //clear set of lines that have been written to file
                eventInstanceProperties.clear();
                lineCounter = increaseLineCounter(lineCounter, lineProgress);
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
        if (newLineCounterValue % lineProgress == 0)
            System.out.println(newLineCounterValue + " instances processed.");
        return newLineCounterValue;
    }

    /**
     * Get all place instance properties. Might (optional) be the following:
     * geo:lat, geo:long, owl:sameAs.
     * owl:sameAs is only included if it points to geonames, yago or wikidata
     * @param k 0:DBpedia, 1:YAGO, 2:Wikidata
     * @param service sparql url
     * @param varNames
     *@param placeInstance URI of the instance  @return HashSet<String> including all instance properties
     */
    private static HashSet<String> getPlaceInstanceProperties(int k, String service, KGVariableNames varNames, String placeInstance) {
        String geoNamesURL = "http://sws.geonames.org";
        String otherKgURL = varNames.getOtherKgURL();

        HashSet<String> resultSet = new HashSet<>();
        String queryString = getQueryPrefix(k);
        queryString = queryString +
                "SELECT ?lat ?long ?same WHERE {\n" +
                " OPTIONAL { <" + placeInstance + "> " + varNames.getLatVar() + " ?lat }\n" +
                " OPTIONAL { <" + placeInstance +"> " + varNames.getLongVar() + " ?long }\n"+
                " OPTIONAL { <" + placeInstance +"> owl:sameAs ?same }}";
        ResultSet results = queryEndpoint(service, queryString);
        List<String> properties = new ArrayList<>();
        properties.add("lat");
        properties.add("long");
        properties.add("same");
        String resultString = "";
        boolean oneLineAdded = false;
        while (results.hasNext()) {
            QuerySolution qs = results.next();
            resultString =  placeInstance + getAvailableOptionalProperties(qs, properties);

            //only add sameAs if they link to yago, dbpedia, or geonames BUT add at least one line with lat&long if no sameAs link goes to yago, dbpedia, or geonames
            if (qs.contains("same")) {
                String sameAs = qs.get("same").toString();
                if (sameAs.startsWith(geoNamesURL) || sameAs.startsWith(otherKgURL)) {
                    resultSet.add(resultString);
                    oneLineAdded = true;
                }

            }  else {
                resultSet.add(resultString);
                oneLineAdded = true;
            }
        }
        //check if at least one line was added
        if (!oneLineAdded) {
            resultSet.add(resultString);
        }
        return resultSet;
    }

    /**
     * Get all distinct place instances that appear as dbo:place object for an event instance.
     * @param k 0:DBpedia, 1:YAGO, 2:Wikidata
     * @param service sparql url
     * @param varNames
     * @return HashSet<String> including all place instance URIs
     */
    private static HashSet<String> getPlaceInstances(int k, String service, KGVariableNames varNames) {
        HashSet<String> instanceSet = new HashSet<>();
        String queryString = getQueryPrefix(k);
        queryString = queryString +
                "SELECT DISTINCT ?place WHERE {\n" +
                " ?event a " + varNames.getEventClass() + " .\n" +
                " ?event rdfs:label ?label .\n" +
                " ?event " + varNames.getDateVar() + " ?date .\n" +
                " ?event " + varNames.getLatVar() + " ?lat .\n" +
                " ?event " + varNames.getLongVar() + " ?long .\n" +
                " ?event " + varNames.getPlaceVar() + " ?place .\n" +
                "}";

        ResultSet results = queryEndpoint(service, queryString);
        while (results.hasNext()) {
            QuerySolution qs = results.next();
            if (qs.get("place").isURIResource()) {
                instanceSet.add(qs.get("place").toString());
                //System.out.println(qs.get("event").toString());
            }
            //System.out.println(qs.get("count").toString());
        }

        return instanceSet;
    }

    /**
     * Get all Instance properties
     * DBpedia: rdfs:label, dbo:date, geo:lat, geo:long, and optionally: foaf:name, owl:sameAs, and dbo:place
     * @param k 0:DBpedia, 1:YAGO, 2:Wikidata
     * @param service sparql url
     * @param varNames
     *@param getOptionalProperties get optional properties as well (foaf:name, owl:sameAs, and dbo:place for DBpedia)  @return HashSet<String> including all instance properties
     */
    private static HashSet<String> getInstanceProperties(int k, String service, KGVariableNames varNames, String instance, boolean getOptionalProperties) {
        //get variable names



        HashSet<String> resultSet = new HashSet<>();
        String queryString = getQueryPrefix(k);
        queryString = queryString +
                "SELECT ?label ?date ?lat ?long ";
        if (getOptionalProperties)
            queryString = queryString + "?name ?same ?place ";// "?point ?geometry "+

        queryString = queryString +
                "WHERE {\n" +
                " <" + instance + "> rdfs:label ?label .\n" +
                " <" + instance + "> " + varNames.getDateVar() + " ?date .\n" +
                " OPTIONAL { <" + instance + "> " + varNames.getLatVar() + " ?lat }\n" +
                " OPTIONAL { <" + instance + "> " + varNames.getLongVar() + " ?long }\n"+
                " OPTIONAL { <" + instance + "> owl:sameAs ?same }\n";
        if (getOptionalProperties) {
            queryString = queryString +
                    " OPTIONAL { <" + instance + "> " + varNames.getLabel2Var() +  "?name }\n" +
                    " OPTIONAL { <" + instance + "> " + varNames.getPlaceVar() + " ?place }\n";
        }

        queryString = queryString +
                " FILTER langMatches( lang(?label), \'" + varNames.getEnVar() + "\' )\n" +
                "}";
        //System.out.println(queryString);
        ResultSet results = queryEndpoint(service, queryString);

        String resultString;
        List<String> optionalProperties = new ArrayList<>();
        optionalProperties.add("lat");
        optionalProperties.add("long");
        optionalProperties.add("same");

        if (getOptionalProperties) {
            optionalProperties.add("name");
            optionalProperties.add("place");
        }
        while (results.hasNext()) {
            QuerySolution qs = results.next();
            resultString = instance + "\t" + qs.get("label").toString() + "\t" + qs.get("date").toString();
            if (getOptionalProperties)
                resultString = resultString + getAvailableOptionalProperties(qs, optionalProperties);

            resultSet.add(resultString);

        }

        return resultSet;
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
            oP = (qs.contains(p)) ? oP + "\t " + qs.get(p).toString() : oP + "\tnull";
        }
        return oP;
    }

    /**
     * Get all distinct event instances that have the following attributes:
     * rdfs:label, dbo:date, geo:lat, geo:long
     * @param k 0:DBpedia, 1:YAGO, 2:Wikidata
     * @param service sparql url
     * @param varNames
     * @return HashSet<String> including all instance URIs
     */
    private static HashSet<String> getEventInstances(int k, String service, KGVariableNames varNames) {
        HashSet<String> instanceSet = new HashSet<>();
        String queryString = getQueryPrefix(k);

        queryString = queryString +
                    //"SELECT (COUNT(DISTINCT ?event) as ?count) WHERE {\n" +
                    "SELECT DISTINCT ?event WHERE {\n" +
                    " ?event a " + varNames.getEventClass() + " .\n" +
                    " ?event rdfs:label ?label .\n" +
                    " ?event " + varNames.getDateVar() +" ?date .\n" +
                   // " ?event " + varNames.getLatVar() + "?lat .\n" +
                    //" ?event " + varNames.getLongVar() + " ?long .\n" +
                    " FILTER langMatches( lang(?label), \'" + varNames.getEnVar() + "\' )\n" +
                    "}";

        ResultSet results = queryEndpoint(service, queryString);
        while (results.hasNext()) {
            QuerySolution qs = results.next();
            if (qs.get("event").isURIResource()) {
                instanceSet.add(qs.get("event").toString());
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
                    "PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\n" +
                    // "PREFIX georss: <http://www.georss.org/georss/>\n"+
                    "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n";

        } else if (k==1) {
            p =     "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                    //"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
                    "PREFIX yago: <http://yago-knowledge.org/resource/>\n"+
                    "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n";
        } else if (k==2) {
            p =     "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                    //"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
                    "PREFIX wdt: <http://www.wikidata.org/prop/direct/>\n" +
                    "PREFIX wd: <http://www.wikidata.org/entity/>\n" +
                    "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n";
        }
        return p;
    }
    /**
     * Query the SPARQL endpoint
     * @param service sparql endpoint URL
     * @param queryString  query to send to the endpoint
     * @return ResultSet
     */
    private static ResultSet queryEndpoint(String service, String queryString) {
        Query query = QueryFactory.create(queryString);
        QueryExecution qe =  QueryExecutionFactory.sparqlService(service, query);
        ResultSet results = qe.execSelect();
        ResultSet resultsCopy = ResultSetFactory.copyResults(results);
        qe.close();
        return resultsCopy;
    }
    /**
     * Test the service connection
     * @param service sparql url to test
     * @return boolean (true if service is up)
     */
    public static boolean testConnection(String service) {
        boolean isUp = false;
        String queryTest = "ASK {}";

        QueryExecution qeTest = QueryExecutionFactory.sparqlService(service, queryTest);

        try {
            if(qeTest.execAsk()) {
                System.out.println(service + " is up");
                isUp = true;
            }
        } catch (QueryExceptionHTTP e) {
            System.out.println(service + " is down: " + e);
        } finally {
            qeTest.close();
        }
        return isUp;
    }
}
