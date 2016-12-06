import org.apache.jena.query.*;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

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

        //configure log4j
        org.apache.log4j.BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel(Level.OFF); //set console logger off

        String service = "";
        boolean dbIsUp;
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
                     dInstancesP.addAll(getDBpediaInstanceProperties(service, instance));

                }
                for (String s : dInstancesP) {
                    //save to csv
                    //System.out.println(s);
                }
                System.out.println(dInstancesP.size());
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

    private static HashSet<String> getDBpediaInstanceProperties(String service, String instance) {
        HashSet<String> resultSet = new HashSet<>();
        String queryString = getQueryPrefix();
        queryString = queryString +
                "SELECT ?label ?date ?geometry WHERE {\n" +
                " <" + instance + "> rdfs:label ?label .\n" +
                " <" + instance + "> dbo:date ?date .\n" +
                " <" + instance + ">geo:geometry ?geometry .\n" +
                " FILTER langMatches( lang(?label), \'EN\' )\n" +
                "}";
        ResultSet results = queryEndpoint(service, queryString);
        while (results.hasNext()) {
            QuerySolution qs = results.next();
            String resultString = instance + "\t" + qs.get("label").toString() + "\t" + qs.get("date").toString() + "\t" + qs.get("geometry").toString();
            resultSet.add(resultString);
        }

        return resultSet;
    }

    private static HashSet<String> getDBpediaInstances(String service) {
        HashSet<String> resultSet = new HashSet<>();
        String queryString = getQueryPrefix();
        queryString = queryString +
                //"SELECT (COUNT(DISTINCT ?event) as ?count) WHERE {\n" +
                "SELECT DISTINCT ?event WHERE {\n" +
                    " ?event a dbo:Event .\n" +
                    " ?event rdfs:label ?label .\n" +
                    " ?event dbo:date ?date .\n" +
                    " ?event geo:geometry ?geometry .\n" +
                "}";

        ResultSet results = queryEndpoint(service, queryString);
        while (results.hasNext()) {
            QuerySolution qs = results.next();
            if (qs.get("event").isURIResource()) {
                resultSet.add(qs.get("event").toString());
                //System.out.println(qs.get("event").toString());
            }
            //System.out.println(qs.get("count").toString());
        }

        return resultSet;
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
