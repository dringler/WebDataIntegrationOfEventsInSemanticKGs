import org.apache.jena.query.*;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created by curtis on 06/12/16.
 */
public class createDatasetsMain {
    public static void main(String[] args) {
        // PARAMETERS
        boolean dbpedia = true;
        boolean yago = false;
        boolean wikidata = false;

        boolean getOptionalP = false;
        boolean secondOrderP = false; //second order can only be specified if getOptionalP is true

        String dbpediaFileName = "";
        String dbpediaSecondOrderFileName = "out/dbpedia-3-secondOrder.tsv";

        if (!getOptionalP)
            dbpediaFileName = "out/dbpedia-1-woOptional.tsv";
        else
            dbpediaFileName = "out/dbpedia-2-wOptional.tsv";

        //wOptional, woOptional, secondOrder


        if (secondOrderP && !getOptionalP) {
            System.out.println("Set getOptionalP and secondOrderP to true. secondOrderP cannot be executed without getting the optional properties");
            return;
        }


        //configure log4j
        org.apache.log4j.BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel(Level.OFF); //set console logger off

        String service = "";
        boolean dbIsUp;
        int lineCounter = 0;
        int writtenLines = 0;
        int lineProgress = 500;
        //HashSet<String> xInstancesWithProperties;

        if (dbpedia) {
            service = "http://dbpedia.org/sparql";
            dbIsUp = testConnection(service);
            if (dbIsUp) {
                HashSet<String> dInstances = getDBpediaInstances(service);
                System.out.println(dInstances.size() + " distinct instances received from " + service);

                try {
                    // 1 get event instances properties
                    PrintWriter writer = new PrintWriter(dbpediaFileName, "UTF-8");
                    //write header
                    String header = "uri\tlabel\tdate\tlat\tlong";
                    if (getOptionalP)
                        header = header + "\tname\tsame\tplace";

                    writer.println(header);

                    HashSet<String> dInstancesP = new HashSet<>();
                    for (String instance : dInstances) {
                        //System.out.println(i);
                        //get all properties for the instance
                        dInstancesP.addAll(getDBpediaInstanceProperties(service, instance, getOptionalP));
                        //write all lines to file
                        for (String s : dInstancesP) {
                            //write line to csv
                            //System.out.println(s);
                            writer.println(s);
                        }
                        writtenLines = writtenLines + dInstancesP.size();
                        //clear set of lines that have been written to file
                        dInstancesP.clear();
                        if (lineCounter % lineProgress == 0 && lineCounter != 0)
                            System.out.println(lineCounter + " instances processed.");
                        lineCounter += 1;
                    }
                    writer.close();
                    System.out.println(writtenLines + " lines written to " + dbpediaFileName);
                    writtenLines = 0;
                    lineCounter = 0;
                    // 2 get second order file
                    if (secondOrderP) {
                        HashSet<String> dPlaceInstances = getDBpediaPlaceInstances(service);
                        System.out.println(dPlaceInstances.size() + " distinct place instances received from " + service);
                        PrintWriter writer2 = new PrintWriter(dbpediaSecondOrderFileName, "UTF-8");
                        //write header
                        writer2.println("place\tpLat\tpLong\tpSame");

                        HashSet<String> dPlaceInstancesP = new HashSet<>();
                        for (String placeInstance : dPlaceInstances) {
                            dPlaceInstancesP.addAll(getDBpediaPlaceInstanceProperties(service, placeInstance));

                            for (String s : dPlaceInstancesP) {
                                writer2.println(s);
                            }
                            writtenLines = writtenLines + dPlaceInstancesP.size();
                            dPlaceInstancesP.clear();
                            if (lineCounter % lineProgress == 0 && lineCounter != 0)
                                System.out.println(lineCounter + " place instances processed.");
                            lineCounter += 1;

                        }
                        writer2.close();
                        System.out.println(writtenLines + " lines written to " + dbpediaSecondOrderFileName);
                    }
                } catch (IOException e) {
                    System.out.println("error while writing to file");
                }
                //System.out.println(dInstancesP.size() + " lines received from " + service);
            }

        }
        if (yago) {
            service = "https://linkeddata1.calcul.u-psud.fr/sparql";
            dbIsUp = testConnection(service);
        }
        if (wikidata) {
            service = "https://query.wikidata.org/sparql";
            dbIsUp = testConnection(service);
        }


    }

    private static HashSet<String> getDBpediaPlaceInstanceProperties(String service, String placeInstance) {
        HashSet<String> resultSet = new HashSet<>();
        String queryString = getQueryPrefix();
        queryString = queryString +
                "SELECT ?lat ?long ?same WHERE {\n" +
                " OPTIONAL { <" + placeInstance + "> geo:lat ?lat }\n" +
                " OPTIONAL { <" + placeInstance +"> geo:long ?long }\n"+
                " OPTIONAL { <" + placeInstance +"> owl:sameAs ?same }}";
        ResultSet results = queryEndpoint(service, queryString);
        String[] properties = {"lat", "long", "same"};
        String resultString = "";
        boolean oneLineAdded = false;
        while (results.hasNext()) {
            QuerySolution qs = results.next();
            resultString =  placeInstance + getAvailableOptionalProperties(qs, properties);

            //only add sameAs  if they link to yago, wikidata or geonames BUT add at least one line with lat&long if no sameAs link goes to yago, wkidata or geonames
            if (qs.contains("same")) {
                String sameAs = qs.get("same").toString();
                if (sameAs.startsWith("http://sws.geonames.org") || sameAs.startsWith("http://yago-knowledge.org") || sameAs.startsWith("http://www.wikidata.org")) {
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

    private static HashSet<String> getDBpediaPlaceInstances(String service) {
        HashSet<String> instanceSet = new HashSet<>();
        String queryString = getQueryPrefix();
        queryString = queryString +
                //"SELECT (COUNT(DISTINCT ?event) as ?count) WHERE {\n" +
                "SELECT DISTINCT ?place WHERE {\n" +
                " ?event a dbo:Event .\n" +
                " ?event rdfs:label ?label .\n" +
                " ?event dbo:date ?date .\n" +
                " ?event geo:lat ?lat .\n" +
                " ?event geo:long ?long .\n" +
                " ?event dbo:place ?place .\n" +
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

    private static HashSet<String> getDBpediaInstanceProperties(String service, String instance, boolean getOptionalProperties) {
        HashSet<String> resultSet = new HashSet<>();
        String queryString = getQueryPrefix();
        queryString = queryString +
                "SELECT ?label ?date ?lat ?long ";
        if (getOptionalProperties)
            queryString = queryString + "?name ?same ?place ";// "?point ?geometry "+


        queryString = queryString +
                "WHERE {\n" +
                " <" + instance + "> rdfs:label ?label .\n" +
                " <" + instance + "> dbo:date ?date .\n" +
                " <" + instance + "> geo:lat ?lat .\n" +
                " <" + instance + "> geo:long ?long .\n";
        if (getOptionalProperties) {
            queryString = queryString +
                    " OPTIONAL { <" + instance + "> foaf:name ?name }\n" +
                    " OPTIONAL { <" + instance + "> owl:sameAs ?same }\n" +
                    " OPTIONAL { <" + instance + "> dbo:place ?place }\n";

        }

        queryString = queryString +
                " FILTER langMatches( lang(?label), \'EN\' )\n" +
                "}";
        //System.out.println(queryString);
        ResultSet results = queryEndpoint(service, queryString);

        //boolean addSet = false;
        String resultString;
        String[] properties = {"name", "same",  "place"};
        //HashSet<String> secondOrderResultStringSet = null;
        while (results.hasNext()) {
            QuerySolution qs = results.next();
            resultString = instance + "\t" + qs.get("label").toString() + "\t" + qs.get("date").toString() + "\t" + qs.get("lat").toString() + "\t" + qs.get("long").toString();
           // addSet = false;
            if (getOptionalProperties)
                resultString = resultString + getAvailableOptionalProperties(qs, properties);
               /* if(secondOrderP)
                    //check is qs has an value for the dbo:place property
                    if (qs.contains("place")) {
                        addSet = true;
                        secondOrderResultStringSet = getSecondOrderProperties(service, qs, qs.get("place").toString(), resultString);
                        //System.out.println(qs.get("place").toString());
                    } else { //no dbo:place property -> set the 3 values to null
                        resultString = resultString +  "\tnull\tnull\tnull";
                    }*/




            //System.out.println(resultString);
            /*if (addSet) { //check if multiple strings are returned -> add all to resultSet
                for (String s : secondOrderResultStringSet) {
                    resultSet.add(s);
                }
            } else { //add single returned string to resultSet*/
            resultSet.add(resultString);
            //}
        }

        return resultSet;
    }

    private static HashSet<String> getSecondOrderProperties(String service, QuerySolution qs, String place, String stringWoSecondOrder) {
        HashSet<String> resultSet = new HashSet<>();

        String[] Properties = {"pLat", "pLong",  "pSame"};
        //create query
        String queryString = getQueryPrefix();
        queryString = queryString +
                "SELECT ?pLat ?pLong ?pSame WHERE {\n" +
                " OPTIONAL { <" + place + "> geo:lat ?pLat }\n" +
                " OPTIONAL { <" + place + "> geo:long ?pLong }\n" +
                " OPTIONAL { <" + place + "> owl:sameAs ?pSame }}\n";
        ResultSet results = queryEndpoint(service, queryString);
        while (results.hasNext()) {
            QuerySolution qsP = results.next();
            String secondOrderString = "";
            //check all properties (lat, long, same) and add if available
            for (String p : Properties) {
                if (qsP.contains(p)) {
                    secondOrderString = secondOrderString + "\t"+qsP.get(p).toString();
                } else {
                    secondOrderString = secondOrderString + "\tnull";
                }
            }
            //add complete string to set
            resultSet.add(stringWoSecondOrder + secondOrderString);

        }


        return resultSet;

    }

    private static String getAvailableOptionalProperties(QuerySolution qs, String[] properties) {
        //String[] Properties = (!secondOrderP) ? new String[]{"name", "same",  "place"} :  new String[]{"name", "same", "place", "placeSame", "placeLat", "placeLong"};
        //String[] Properties = (!secondOrderP) ? new String[]{"name", "title", "time", "startDate", "location", "place", "country", "city", "same"} :  new String[]{"name", "title", "time", "startDate", "location", "place", "country", "city", "same", "locationSame", "locationLat", "locationLong", "placeSame", "placeLat", "placeLong", "countrySame", "countryLat", "countryLong", "citySame", "cityLat", "cityLong"};
        //"point", "geometry",
        String oP = "";
        for (String p : properties) {
            //get value for property or add "null" is property is null
            oP = (qs.contains(p)) ? oP + "\t " + qs.get(p).toString() : oP + "\tnull";
            //get second order properties if dbo:place is not null
        }
        return oP;
    }

    private static HashSet<String> getDBpediaInstances(String service) {
        HashSet<String> instanceSet = new HashSet<>();
        String queryString = getQueryPrefix();
        queryString = queryString +
                //"SELECT (COUNT(DISTINCT ?event) as ?count) WHERE {\n" +
                "SELECT DISTINCT ?event WHERE {\n" +
                    " ?event a dbo:Event .\n" +
                    " ?event rdfs:label ?label .\n" +
                    " ?event dbo:date ?date .\n" +
                    " ?event geo:lat ?lat .\n" +
                    " ?event geo:long ?long .\n" +
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

    private static ResultSet queryEndpoint(String service, String queryString) {
        Query query = QueryFactory.create(queryString);
        QueryExecution qe =  QueryExecutionFactory.sparqlService(service, query);
        ResultSet results = qe.execSelect();
        ResultSet resultsCopy = ResultSetFactory.copyResults(results);
        qe.close();
        return resultsCopy;
    }

    private static String getQueryPrefix() {
        String p =
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"+
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
                //"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
                "PREFIX dbo: <http://dbpedia.org/ontology/>\n"+
                //"PREFIX yago: <http://yago-knowledge.org/resource/>\n"+
                "PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\n"+
               // "PREFIX georss: <http://www.georss.org/georss/>\n"+
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n";
                //"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n";
        return p;
    }

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
