import org.apache.jena.query.*;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
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
        if (!getOptionalP)
            dbpediaFileName = "out/dbpedia-1-woOptional.tsv";
        else
            if (!secondOrderP)
                dbpediaFileName = "out/dbpedia-2-wOptional.tsv";
            else
                dbpediaFileName = "out/dbpedia-3-secondOrder.tsv";
            //wOptional, woOptional, secondOrder


        if(secondOrderP && !getOptionalP) {
            System.out.println("Set getOptionalP and secondOrderP to true. secondOrderP cannot be executed without getting the optional properties");
            return;
        }


        //configure log4j
        org.apache.log4j.BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel(Level.OFF); //set console logger off

        String service = "";
        boolean dbIsUp;
        int lineCounter = 0;
        //HashSet<String> xInstancesWithProperties;

        if (dbpedia) {
            service = "http://dbpedia.org/sparql";
            dbIsUp = testConnection(service);
            if (dbIsUp) {
                HashSet<String> dInstances = getDBpediaInstances(service);
                System.out.println(dInstances.size() + " distinct instances received from " + service );
                HashSet<String> dInstancesP = new HashSet<>();
                for (String instance : dInstances) {
                    //System.out.println(i);
                    //get all properties for the instance
                     dInstancesP.addAll(getDBpediaInstanceProperties(service, instance, getOptionalP, secondOrderP));
                    if (lineCounter % 500 == 0 && lineCounter != 0)
                        System.out.println(lineCounter + " instances processed.");
                    lineCounter += 1;
                }
                try {
                    PrintWriter writer = new PrintWriter(dbpediaFileName, "UTF-8");
                    //write header
                    String header =  "uri\tlabel\tdate\tlat\tlong";
                    if (getOptionalP)
                        header = header + "\tname\ttitle\ttime\tstartDate\tlocation\tplace\tcountry\tcity\tsame";//"\tpoint\tgeometry"+
                    writer.println(header);
                    for (String s : dInstancesP) {
                        //write line to csv
                        //System.out.println(s);
                        writer.println(s);
                    }

                    writer.close();
                    System.out.println(dInstancesP.size() + " lines written to " + dbpediaFileName);
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

    private static HashSet<String> getDBpediaInstanceProperties(String service, String instance, boolean getOptionalProperties, boolean secondOrderP) {
        HashSet<String> resultSet = new HashSet<>();
        String queryString = getQueryPrefix();
        queryString = queryString +
                "SELECT ?label ?date ?lat ?long ";
        if (getOptionalProperties)
            queryString = queryString + "?name ?title ?time ?startDate ?location ?place ?country ?city ?same ";// "?point ?geometry "+
        if (secondOrderP)
            queryString = queryString + "?locationSame ?locationLat ?locationLong ?placeSame ?placeLat ?placeLong ?countrySame ?countryLat ?countryLong ?citySame ?cityLat ?cityLong ";

        queryString = queryString +
                "WHERE {\n" +
                " <" + instance + "> rdfs:label ?label .\n" +
                " <" + instance + "> dbo:date ?date .\n" +
                " <" + instance + "> geo:lat ?lat .\n" +
                " <" + instance + "> geo:long ?long .\n";
        if (getOptionalProperties) {
            queryString = queryString +
                    " OPTIONAL { <" + instance + "> foaf:name ?name }\n" +
                    " OPTIONAL { <" + instance + "> dbo:title ?title }\n" +
                    " OPTIONAL { <" + instance + "> dbo:time ?time }\n" +
                    " OPTIONAL { <" + instance + "> dbo:startDate ?startDate }\n" +
                    " OPTIONAL { <" + instance + "> dbo:location ?location }\n" +
                    " OPTIONAL { <" + instance + "> dbo:place ?place }\n" +
                    //" OPTIONAL { <" + instance + "> georss:point ?point }\n" +
                    //" OPTIONAL { <" + instance + "> geo:geometry ?geometry}\n"+
                    " OPTIONAL { <" + instance + "> dbo:country ?country }\n" +
                    " OPTIONAL { <" + instance + "> dbo:city ?city }\n" +
                    " OPTIONAL { <" + instance + "> owl:sameAs ?same }\n";
        }

        if (secondOrderP) {
            queryString = queryString +
                    "  OPTIONAL { ?location owl:sameAs ?locationSame }\n" +
                    "  OPTIONAL { ?location geo:lat ?locationLat }\n" +
                    "  OPTIONAL { ?location geo:long ?locationLong }\n" +
                    "  OPTIONAL { ?place owl:sameAs ?placeSame }\n" +
                    "  OPTIONAL { ?place geo:lat ?placeLat }\n" +
                    "  OPTIONAL { ?place geo:long ?placeLong }\n" +
                    "  OPTIONAL { ?country owl:sameAs ?countrySame }\n" +
                    "  OPTIONAL { ?country geo:lat ?countryLat }\n" +
                    "  OPTIONAL { ?country geo:long ?countryLong }\n" +
                    "  OPTIONAL { ?city owl:sameAs ?citySame }\n" +
                    "  OPTIONAL { ?city geo:lat ?cityLat }\n"+
                    "  OPTIONAL { ?city geo:long ?cityLong }\n";
        }


        queryString = queryString +
                " FILTER langMatches( lang(?label), \'EN\' )\n" +
                "}";
        ResultSet results = queryEndpoint(service, queryString);

        String resultString = "";
        while (results.hasNext()) {
            QuerySolution qs = results.next();
            resultString = instance + "\t" + qs.get("label").toString() + "\t" + qs.get("date").toString() + "\t" + qs.get("lat").toString() + "\t" + qs.get("long").toString();

            if (getOptionalProperties)
                resultString = resultString + getAvailableOptionalProperties(qs, secondOrderP);

            //System.out.println(resultString);
            resultSet.add(resultString);
        }

        return resultSet;
    }

    private static String getAvailableOptionalProperties(QuerySolution qs, boolean secondOrderP) {

        String[] Properties = (!secondOrderP) ? new String[]{"name", "title", "time", "startDate", "location", "place", "country", "city", "same"} :  new String[]{"name", "title", "time", "startDate", "location", "place", "country", "city", "same", "locationSame", "locationLat", "locationLong", "placeSame", "placeLat", "placeLong", "countrySame", "countryLat", "countryLong", "citySame", "cityLat", "cityLong"};
        //"point", "geometry",
        String oP = "";

        for (String p : Properties) {
            oP = (qs.contains(p)) ? oP + "\t " + qs.get(p).toString() : oP + "\t null";
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
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
                "PREFIX dbo: <http://dbpedia.org/ontology/>\n"+
                "PREFIX yago: <http://yago-knowledge.org/resource/>\n"+
                "PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\n"+
                "PREFIX georss: <http://www.georss.org/georss/>\n"+
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"+
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n";
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
