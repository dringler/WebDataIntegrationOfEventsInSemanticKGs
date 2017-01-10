import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.uni_mannheim.informatik.wdi.model.*;
import de.uni_mannheim.informatik.wdi.usecase.events.model.Event;
import de.uni_mannheim.informatik.wdi.usecase.events.model.EventFactory;
import org.apache.jena.query.*;

import de.uni_mannheim.informatik.wdi.usecase.events.Events_IdentityResolution_Main;
import de.uni_mannheim.informatik.wdi.usecase.events.Events_DataFusion_Main;
import org.apache.jena.query.ResultSet;
import org.apache.log4j.Logger;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Daniel Ringler
 * Created on 13/10/16.
 */
public class QueryProcessor {

    final static Logger logger = Logger.getLogger(QueryProcessor.class);
    /**
     Get user parameters from Web App and trigger the Data Integration Process
     @param useLocalData
     @param d query DBpedia (boolean)
     @param y query YAGO (boolean)
     @param keyword keyword search for the labels
     @param fD fromDate (String)
     @param tD toDate (String)
     @return JSON to update the D3.JS chart
     */
    public String getUserData(boolean useLocalData, boolean d, boolean y, String keyword, String fD, String tD) throws Exception {

        boolean applyKeywordSearch = false;
        if (!keyword.equals("")) {
            applyKeywordSearch = true;
        }

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
        LocalDate fromDate = LocalDate.parse(fD, dateTimeFormatter);
        LocalDate toDate = LocalDate.parse(tD, dateTimeFormatter);
        char separator = '+';

        //user parameters received, start data integration process
        FusableDataSet<Event, DefaultSchemaElement> dataSetD = new FusableDataSet<>();
        FusableDataSet<Event, DefaultSchemaElement> dataSetY = new FusableDataSet<>();
        FusableDataSet<Event, DefaultSchemaElement> fusedDataSet = null;

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        //use dynamic data
        if (!useLocalData) {
            HashMap<String, HashSet<String[]>> instancesD;
            HashMap<String, HashSet<String[]>> instancesY;

            if (d) {
                //step 1: data collection
                instancesD = dataCollection(d, false, applyKeywordSearch, keyword, fD, tD);
                //step 2: data translation
                dataSetD.loadFromInstancesHashMap(instancesD, new EventFactory(), separator, true, dateTimeFormatter, fromDate, toDate, false, null);
            }
            if (y) {
                //step 1: data collection
                instancesY = dataCollection(false, y, applyKeywordSearch, keyword, fD, tD);
                //step 2: data translation
                dataSetY.loadFromInstancesHashMap(instancesY, new EventFactory(), separator, true, dateTimeFormatter, fromDate, toDate, false, null);
            }



        } else { //use local sample data
            //step 1+2: data collection and translation
            if (d) {
                dataSetD.loadFromTSV(new File("../data/dbpedia-1.tsv"),
                        new EventFactory(), "events/event", separator, true, dateTimeFormatter, fromDate, toDate, applyKeywordSearch, keyword);
            }
            if (y) {
                dataSetY.loadFromTSV(new File("../data/yago-1.tsv"),
                        new EventFactory(), "events/event", separator, true, dateTimeFormatter, fromDate, toDate, applyKeywordSearch, keyword);
            }
        }



        //step 3: identity resolution
        if (d && y) {

            de.uni_mannheim.informatik.wdi.model.ResultSet<Correspondence<Event, DefaultSchemaElement>> correspondences =
                    Events_IdentityResolution_Main.runIdentityResolution(dataSetD, dataSetY, separator);

        //step 4: data fusion
            if (correspondences.size() > 0) {
                fusedDataSet = Events_DataFusion_Main.runDataFusion(dataSetD, dataSetY, correspondences, separator, dateTimeFormatter, fromDate, toDate, keyword);
            } else {
                System.out.println("no correspondences found");
            }

            //combine data sets


        } else {
            //return single data set
            if (d) {
                String jsonInString = mapper.writeValueAsString(dataSetD);
                System.out.println(jsonInString);
            }
            if (y) {
                String jsonInString = mapper.writeValueAsString(dataSetY);
                System.out.println(jsonInString);
            }
        }

        // convert data to JSON

        /*List<Event> sampleEventList= Stream.of(
                fusedDataSet.getRandomRecord(),
                fusedDataSet.getRandomRecord(),
                fusedDataSet.getRandomRecord(),
                fusedDataSet.getRandomRecord()
        ).collect(Collectors.toList());
        */
        List<Event> eventList = fusedDataSet.getRecords()
                .stream()
                .collect(Collectors.toList());

        String jsonInString = mapper.writeValueAsString(eventList);

        return jsonInString;
    }

    /**
     Data Collection Process
     @return results from querying the public endpoints
      * @param d query DBpedia (boolean)
     * @param y query YAGO (boolean)
     * @param applyKeywordSearch
     * @param keyword keyword for the labels
     * @param fD fromDate (String)
     * @param tD toDate (String)
     */
    public HashMap<String, HashSet<String[]>>  dataCollection(boolean d, boolean y, boolean applyKeywordSearch, String keyword, String fD, String tD) {
        System.out.println("start data collection");
        String result = "";
        QueryString qs = new QueryString();

        // create Wrapper for each selected source
        if (d) { // DBpedia
            String dbpedia = "http://dbpedia.org/sparql";
            System.out.println("Query " + dbpedia);
            String queryString = qs.getDBpediaQueryString(applyKeywordSearch, keyword, fD, tD);

            HashMap<String, HashSet<String[]>> instancesD = createWrapper(dbpedia, queryString);
            return instancesD;
        }
        /*if (w) { //wikidata
            String wikidata = "https://query.wikidata.org/sparql";
            System.out.println("Query " + wikidata);
            String queryString = qs.getWikidataQueryString(cat, fD, tD);

            createWrapper(wikidata, queryString);
        }*/
        if (y) { //YAGO
            String yago = "https://linkeddata1.calcul.u-psud.fr/sparql";
            System.out.println("Query " + yago);
            String queryString = qs.getYagoQueryString(applyKeywordSearch, keyword, fD, tD);

            HashMap<String, HashSet<String[]>> instancesY = createWrapper(yago, queryString);
            return instancesY;
        }

        return null;
    }
    /**
     Create a Wrapper for querying a KG
     @param service url to public KG endpoint
     @param queryString string to query the KG (String)
     @return ResultSet
     */
    public HashMap<String, HashSet<String[]>> createWrapper(String service, String queryString) {
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(service, query);
        //ResultSet output = null;
        HashMap<String, HashSet<String[]>> instances = new HashMap<>();
        try {
            ResultSet results = qexec.execSelect();
            //ResultSet output = ResultSetFactory.copyResults(results);
            //System.out.println(ResultSetFormatter.asText(output));
            while (results.hasNext()) {
                QuerySolution sol = results.next();
                /*if (sol.get("x") == null) {
                    result = "null";
                } else if (sol.get("x").isLiteral()) {
                    result = sol.getLiteral("x").toString();
                } else {
                    result = sol.getResource("x").getURI();//.substring(28);
                }
                System.out.println(result);
                */
                String[] lineValues = new String[7];
                lineValues[0] = sol.getResource("uri").getURI();//.substring(28);
                lineValues[1] = sol.getLiteral("label").toString();
                lineValues[2] = sol.getLiteral("date").toString();
                lineValues[3] = sol.getLiteral("lat").toString();
                lineValues[4] = sol.getLiteral("long").toString();
                lineValues[5] = "same";
                lineValues[6] = "place";

                if (instances.containsKey(lineValues[0])) {
                    instances.get(lineValues[0]).add(lineValues);
                } else {
                    HashSet<String[]> lineValuesSet = new HashSet<>();
                    lineValuesSet.add(lineValues);
                    instances.put(lineValues[0], lineValuesSet);
                }

            }


        } catch (Exception e) {
            e.printStackTrace();
            //no db connection
            System.out.println(service + " connection failed");
        }
        qexec.close();
        return instances;
    }

}

