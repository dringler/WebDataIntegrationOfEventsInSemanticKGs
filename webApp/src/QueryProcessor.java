import org.apache.jena.query.*;

/**
 * @author Daniel Ringler
 * Created on 13/10/16.
 */
public class QueryProcessor {
    /**
     Get user parameters from Web App and trigger the Data Integration Process
     @param  d query DBpedia (boolean)
     @param w query Wikidata (boolean)
     @param y query YAGO (boolean)
     @param cat category to query
     @param fD fromDate (String)
     @param tD toDate (String)
     @return JSON to update the D3.JS chart
     */
    public String getUserData(boolean d, boolean w, boolean y, String cat, String fD, String tD) {
        System.out.println("DATA RECEIVED");
        System.out.println(cat);
        //data received, start data integration process
        //step 1: data collection
        String dataCollectionResult = dataCollection(d, w, y, cat, fD, tD);


        //step 2: data translation
        
        //http://stackoverflow.com/questions/6514876/most-efficient-conversion-of-resultset-to-json

        //step 3: identity resolution

        //step 4: data fusion

        // convert data to JSON?

        //return results
        return "returnData";
    }

    /**
     Data Collection Process
     @param  d query DBpedia (boolean)
     @param w query Wikidata (boolean)
     @param y query YAGO (boolean)
     @param cat category to query
     @param fD fromDate (String)
     @param tD toDate (String)
     @return results from querying the public endpoints
     */
    public String dataCollection(boolean d, boolean w, boolean y, String cat, String fD, String tD) {
        System.out.println("start data collection");
        String result = "";
        QueryString qs = new QueryString();

        // create Wrapper for each selected source
        if (d) { // DBpedia
            String dbpedia = "http://dbpedia.org/sparql";
            System.out.println("Query " + dbpedia);
            String queryString = qs.getDBpediaQueryString(cat, fD, tD);

            createWrapper(dbpedia, queryString);
        }
        if (w) { //wikidata
            String wikidata = "https://query.wikidata.org/sparql";
            System.out.println("Query " + wikidata);
            String queryString = qs.getWikidataQueryString(cat, fD, tD);

            createWrapper(wikidata, queryString);
        }
        if (y) { //YAGO
            String yago = "https://linkeddata1.calcul.u-psud.fr/sparql";
            System.out.println("Query " + yago);
            String queryString = qs.getYagoQueryString(cat, fD, tD);

            createWrapper(yago, queryString);
        }

        return result;
    }
    /**
     Create a Wrapper for querying a KG
     @param service url to public KG endpoint
     @param queryString string to query the KG (String)
     */
    public void createWrapper(String service, String queryString) {
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(service, query);
        try {
            ResultSet results = qexec.execSelect();
            ResultSet output = ResultSetFactory.copyResults(results);
            System.out.println(ResultSetFormatter.asText(output));
            /*while (results.hasNext()) {
                QuerySolution sol = results.next();
                if (sol.get("x") == null) {
                    result = "null";
                } else if (sol.get("x").isLiteral()) {
                    result = sol.getLiteral("x").toString();
                } else {
                    result = sol.getResource("x").getURI();//.substring(28);
                }
                System.out.println(result);
            }
*/
        } catch (Exception e) {
            e.printStackTrace();
            //no db connection
            System.out.println(service + " connection failed");
        }
        qexec.close();
    }

}
