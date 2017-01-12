/**
 * @author Daniel Ringler
 * Created on 18/10/16.
 */
public class QueryString {

    // TO DO: remove limit in queries

    /**
     * Get a string to query the DBpedia endpoint
     * @param applyKeywordSearch
     * @param  keyword
     * @param filterFrom
     * @param fD fromDate range
     * @param filterTo
     * @param tD toDate range
     * @return String to query the public DBpedia endpoint at http://dbpedia.org/sparql
     */
    public String getDBpediaQueryString(boolean applyKeywordSearch, String keyword, boolean filterFrom, String fD, boolean filterTo, String tD) {

        /*String queryString = "PREFIX  dbo:  <http://dbpedia.org/ontology/>\n" +
                "PREFIX  dbp:  <http://dbpedia.org/property/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"+
                "SELECT  ?uri ?location ?startDate ?endDate WHERE {\n"+
                "?uri <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> dbo:Event ;\n"+
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
        */

        String queryString =
                "PREFIX  dbo:  <http://dbpedia.org/ontology/>\n" +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"+
                        "PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\n" +
                "SELECT ?uri ?label ?date ?lat ?long  WHERE {\n" + //?same ?place
                "?uri a dbo:Event ;\n" +
                " rdfs:label ?label ;\n" +
                " dbo:date ?date ;\n" +
                " geo:lat ?lat ;\n" +
                " geo:long ?long .\n"+
               // " owl:sameAs ?same ;\n" +
               // " dbo:place ?place .\n";
                "FILTER langMatches( lang(?label), 'EN' )\n";

        //date filter
        if (filterFrom || filterTo) {
            queryString = queryString + addDateFilter(filterFrom, fD, filterTo, tD);
        }
        if (applyKeywordSearch) {
            queryString = queryString + addKeywordFilter(keyword);
        }


        queryString = queryString + "}";// LIMIT 1000";

        return queryString;
        //return removeQuotation(queryStringQ);
    }

    private String addKeywordFilter(String keyword) {
        return "FILTER ( ( regex(str(?label), \'" +  keyword + "\', \'i\') ) )";
    }

    private String addDateFilter(boolean filterFrom, String fD, boolean filterTo, String tD) {

        if (filterFrom && filterTo) {
            return "FILTER ( ( ?date > \'" + fD +  "\'^^xsd:date ) && ( ?date < \'" + tD  + "\'^^xsd:date ) )\n";
        } else if (filterFrom && !filterTo) {
            return "FILTER ( ( ?date > \'" + fD +  "\'^^xsd:date ) )\n";
        } else if (!filterFrom && filterTo) {
            return "FILTER ( ( ?date < \'" + tD +  "\'^^xsd:date ) )\n";
        }
        return null;
    }

    /**
     * Get a string to query the YAGO endpoint
     * @param applyKeywordSearch
     * @param  keyword
     * @param filterFrom
     * @param fD fromDate range
     * @param filterTo
     * @param tD toDate range
     * @return String to query the public YAGO endpoint at https://linkeddata1.calcul.u-psud.fr/sparql
     */
    public String getYagoQueryString(boolean applyKeywordSearch, String keyword, boolean filterFrom, String fD, boolean filterTo, String tD) {
        /*String queryString = "PREFIX yago: <http://yago-knowledge.org/resource/>\n"+
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"+
                "SELECT ?uri ?date ?location WHERE {\n" +
                "  ?class yago:hasWordnetDomain yago:wordnetDomain_factotum .\n" +
                "  ?class yago:hasSynsetId ?x .\n" +
                "  ?uri <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?class .\n" +
                "  ?uri yago:happenedOnDate ?date .\n" +
                "  ?uri yago:isLocatedIn ?location\n" +
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
        }*/
        String queryString = "PREFIX yago: <http://yago-knowledge.org/resource/>\n"+
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"+
                "SELECT ?uri ?label ?date ?lat ?long  WHERE {\n" + //?same ?place
                "?uri a yago:wordnet_event_100029378 ;\n" +
                " rdfs:label ?label ;\n" +
                " yago:happenedOnDate ?date ;\n" +
                " yago:hasLatitude ?lat ;\n" +
                " yago:hasLongitude ?long .\n"+
                // " owl:sameAs ?same ;\n" +
                // " dbo:place ?place .\n";
                "FILTER langMatches( lang(?label), 'ENG' )\n";

        //date filter
        if (filterFrom || filterTo) {
            queryString = queryString + addDateFilter(filterFrom, fD, filterTo, tD);
        }
        if (applyKeywordSearch) {
            queryString = queryString + addKeywordFilter(keyword);
        }

        queryString = queryString + "}";// " +
                //"OFFSET 4730 " +
                //"LIMIT 1000";

        return queryString;
    }
        /**
         Get a string to query the Wikidata endpoint
         @param  cat category
         @param fD fromDate range
         @param tD toDate range
         @return String to query the public Wikidata endpoint at https://query.wikidata.org/sparql
         */
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


/*    private static String removeQuotation(String quoted) {
        String unquoted;
        unquoted = quoted.replace("\"", "");
        return unquoted;
    }*/

}
