import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

/**
 * Created by curtis on 18/10/16.
 */
public class QueryString {

    // TO DO: remove limit in queries

    //private String queryStringQ = ""; //query string that might have quotes
    private  String queryString = "PREFIX  dbo:  <http://dbpedia.org/ontology/>\n" +
            "PREFIX  dbp:  <http://dbpedia.org/property/>\n" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"+
            "SELECT  ?x ?location ?startDate ?endDate WHERE {\n"+
            "?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> dbo:Event ;\n"+
            "dbo:location ?location ;\n"+
            "dbo:startDate ?startDate ;\n"+
            "dbo:endDate           ?endDate .\n";

    public String getQueryString(String cat, String fD, String tD) {
        //check for filter values
        if (!fD.equals("") && !tD.equals("")) { //filter for startDate and endDate
            queryString = queryString + "FILTER ( ( ?startDate > \'" + fD +  "\'^^xsd:date ) && ( ?endDate < \'" + tD  + "\'^^xsd:date ) )\n";
        } else if (!fD.equals("") && tD.equals("")) { //filter for startDate only
            queryString = queryString + "FILTER ( ( ?startDate > \'" + fD +  "\'^^xsd:date ) )\n";
        } else if (fD.equals("") && !tD.equals("")) { //filter for endDate only
            queryString = queryString + "FILTER ( ( ?endDate < \'" + tD  + "\'^^xsd:date ) )\n";
        //} else { // no filter
        }
        queryString = queryString + "} LIMIT 10";

        return queryString;
        //return removeQuotation(queryStringQ);

        }

/*    private static String removeQuotation(String quoted) {
        String unquoted;
        unquoted = quoted.replace("\"", "");
        return unquoted;
    }*/

}
