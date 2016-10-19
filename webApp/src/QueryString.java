/**
 * Created by curtis on 18/10/16.
 */
public class QueryString {

    // TO DO: remove limit in queries



    public String getDBpediaQueryString(String cat, String fD, String tD) {

        String queryString = "PREFIX  dbo:  <http://dbpedia.org/ontology/>\n" +
                "PREFIX  dbp:  <http://dbpedia.org/property/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"+
                "SELECT  ?x ?location ?startDate ?endDate WHERE {\n"+
                "?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> dbo:Event ;\n"+
                "dbo:location ?location ;\n"+
                "dbo:startDate ?startDate ;\n"+
                "dbo:endDate           ?endDate .\n";

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

        public String getWikidataQueryString(String cat, String fD, String tD) {
            String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
                    "PREFIX wdt: <http://www.wikidata.org/prop/direct/>\n" +
                    "PREFIX wd: <http://www.wikidata.org/entity/>\n" +
                    "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"+
                    "SELECT ?event ?label ?category ?locLabel ?startTime ?endTime WHERE {\n" +
                    "  ?event wdt:P31 ?class.\n" +
                    "  ?event ?prop ?object.\n" +
                    "  ?event rdfs:label ?label.\n" +
                    "  ?event wdt:P276 ?location.\n" +
                    "  ?location rdfs:label ?locLabel.\n" +
                    "  ?event wdt:P580 ?startTime.\n" +
                    "  ?event wdt:P582 ?endTime.\n" +
                    "  ?event wdt:P373 ?category\n" +
                    "  FILTER(LANGMATCHES(LANG(?label), \'EN\'))\n" +
                    "  FILTER(LANGMATCHES(LANG(?locLabel), \'EN\'))\n" +
                    "  FILTER(?class = wd:Q1656682)\n";

            //check for filter values
            if (!fD.equals("") && !tD.equals("")) { //filter for startDate and endDate
                queryString = queryString + "FILTER(?startTime > \'" + fD + "T00:00:00Z\'^^xsd:dateTime)\n" +
                        "FILTER(?endTime < \'" + tD + "T00:00:00Z\'^^xsd:dateTime)\n";
            } else if (!fD.equals("") && tD.equals("")) { //filter for startDate only
                queryString = queryString + "FILTER(?startTime > \'" + fD + "T00:00:00Z\'^^xsd:dateTime)\n";
            } else if (fD.equals("") && !tD.equals("")) { //filter for endDate only
                queryString = queryString + "FILTER(?endTime < \'" + tD + "T00:00:00Z\'^^xsd:dateTime)\n";
                //} else { // no filter
            }
            queryString = queryString + "} LIMIT 10";

            return queryString;
        }

    public String getYagoQueryString(String cat, String fD, String tD) {
        String queryString = "PREFIX yago: <http://yago-knowledge.org/resource/>\n"+
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"+
                "SELECT ?event ?date ?location WHERE {\n" +
                "  ?class yago:hasWordnetDomain yago:wordnetDomain_factotum .\n" +
                "  ?class yago:hasSynsetId ?x .\n" +
                "  ?event <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?class .\n" +
                "  ?event yago:happenedOnDate ?date .\n" +
                "  ?event yago:isLocatedIn ?location\n" +
                "  FILTER (?x=\'100029378\')\n";

        //check for filter values
        if (!fD.equals("") && !tD.equals("")) { //filter for startDate and endDate
            queryString = queryString + "FILTER(?date > \'" + fD + "\'^^xsd:date)\n" +
                    "FILTER(?date < \'" + tD + "\'^^xsd:date)\n";
        } else if (!fD.equals("") && tD.equals("")) { //filter for startDate only
            queryString = queryString + "FILTER(?date > \'" + fD + "\'^^xsd:date)\n";
        } else if (fD.equals("") && !tD.equals("")) { //filter for endDate only
            queryString = queryString + "FILTER(?date < \'" + tD + "\'^^xsd:date)\n";
            //} else { // no filter
        }
        queryString = queryString + "} LIMIT 10";

        return queryString;
    }

/*    private static String removeQuotation(String quoted) {
        String unquoted;
        unquoted = quoted.replace("\"", "");
        return unquoted;
    }*/

}
