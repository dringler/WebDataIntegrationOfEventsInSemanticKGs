import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import java.lang.reflect.Array;


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
        //if (d) {
            System.out.println("Query DBpedia true");
            Model model = ModelFactory.createDefaultModel();
            model.createTypedLiteral(fY);


            QueryString qs = new QueryString();
            String queryString = qs.getQueryString(cat, fY, tY);
            Query query = QueryFactory.create(queryString);

            String service = "http://dbpedia.org/sparql";


       // }

        QueryExecution qexec = QueryExecutionFactory.sparqlService(service, query);
        try {

            ResultSet results = qexec.execSelect();
            ResultSet output = ResultSetFactory.copyResults(qexec.execSelect());
            System.out.println(ResultSetFormatter.asText(output));

            while (results.hasNext()) {
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


        } catch (Exception e) {
            e.printStackTrace();
            //no db connection

        }
        qexec.close();

        return result;
    }

}
