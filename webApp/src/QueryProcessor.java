import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * Created by curtis on 13/10/16.
 */
public class QueryProcessor {

    public class DataArray {
        public boolean d;
        public boolean w;
        public boolean y;
        public String cat;
        public int fY;
        public int tY;
    }

    public String getUserData(boolean d, boolean w, boolean y, String cat, String fY, String tY) {
        System.out.println("DATA RECEIVED");
        System.out.println(cat);
        //data received, start data integration process
        //step 1: data collection
        String dataCollectionResult = dataCollection(d, w, y, cat, fY, tY);


        //step 2: data translation

        //step 3: identity resolution

        //step 4: data fusion

        // convert data to JSON?

        //return results
        return "returnData";
    }

    public String dataCollection(boolean d, boolean w, boolean y, String cat, String fY, String tY) {
        System.out.println("start data collection");
        String result = "";
        QueryString qs = new QueryString();
        // dbpedia
        if (d) {
            String dbpedia = "http://dbpedia.org/sparql";
            System.out.println("Query " + dbpedia);
            String queryString = qs.getDBpediaQueryString(cat, fY, tY);

            queryTheKB(dbpedia, queryString);
        }
        //wikidata
        if (w) {
            String wikidata = "https://query.wikidata.org/sparql";
            System.out.println("Query " + wikidata);
            String queryString = qs.getWikidataQueryString(cat, fY, tY);

            queryTheKB(wikidata, queryString);
        }

        if (y) {
            String yago = "https://linkeddata1.calcul.u-psud.fr/sparql";
            System.out.println("Query " + yago);
            String queryString = qs.getYagoQueryString(cat, fY, tY);

            queryTheKB(yago, queryString);
        }

        return result;
    }
    public void queryTheKB(String service, String queryString) {
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
