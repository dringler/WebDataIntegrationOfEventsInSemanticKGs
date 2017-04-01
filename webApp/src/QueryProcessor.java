import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.uni_mannheim.informatik.wdi.model.*;
import de.uni_mannheim.informatik.wdi.usecase.events.dataanalysis.EventAnalyzer;
import de.uni_mannheim.informatik.wdi.usecase.events.model.Event;
import de.uni_mannheim.informatik.wdi.usecase.events.model.EventFactory;
import de.uni_mannheim.informatik.wdi.utils.WDI_BlockingFramework_Combiner;
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
     * Get user parameters from Web App and trigger the Data Integration Process
     * @param useLocalData use local data or query the SPARQL endpoints
     * @param includeOnlyFusedEvents
     * @param d query DBpedia (boolean)
     * @param y query YAGO (boolean)
     * @param keyword keyword search for the labels
     * @param fD fromDate (String)
     * @param tD toDate (String)
     * @return JSON to update the D3.JS chart
     */
    public String getUserData(boolean useLocalData, boolean includeOnlyFusedEvents, boolean d, boolean y, String keyword, String fD, String tD) throws Exception {
        String jsonString = null;
        List<Event> eventList = new ArrayList<>();

        boolean applyKeywordSearch = false;
        if (!keyword.equals("")) {
            applyKeywordSearch = true;
        }
        boolean filterFrom = checkDateFilter(fD);
        boolean filterTo = checkDateFilter(tD);


        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
        LocalDate fromDate = null;
        if (filterFrom) {
            fromDate = LocalDate.parse(fD, dateTimeFormatter);
        }
        LocalDate toDate = null;
        if (filterTo) {
            toDate = LocalDate.parse(tD, dateTimeFormatter);
        }
        char separator = '+';

        //user parameters received, start data integration process
        FusableDataSet<Event, DefaultSchemaElement> dataSetD = new FusableDataSet<>();
        FusableDataSet<Event, DefaultSchemaElement> dataSetY = new FusableDataSet<>();
        FusableDataSet<Event, DefaultSchemaElement> fusedDataSet = null;

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        //DATA ANALYSIS
        if (false) {
            EventAnalyzer eventAnalyzer = new EventAnalyzer();
            if (false) {
                //load first dataset
                EventFactory eventFactoryD = new EventFactory(dateTimeFormatter, false, fromDate, false, toDate, false, keyword);
                dataSetD.loadFromXML(new File("../data/dbpedia_events.xml"),
                        eventFactoryD, "events/event");
                eventFactoryD.printDateNotParsedCounter();
                eventAnalyzer.runAnalysis(dataSetD, "D");
            }
            if (true) {
                //load second dataset
                EventFactory eventFactoryY = new EventFactory(dateTimeFormatter, false, fromDate, false, toDate, false, keyword);
                dataSetY.loadFromXML(new File("../data/yago_events.xml"),
                        eventFactoryY, "events/event");
                eventFactoryY.printDateNotParsedCounter();
                eventAnalyzer.runAnalysis(dataSetY, "Y");
            }

        } else { //no data analysis

            //use dynamic data
            if (!useLocalData) {
                HashMap<String, HashSet<String[]>> instancesD;
                HashMap<String, HashSet<String[]>> instancesY;

                if (d) {
                    //step 1: data collection
                    instancesD = dataCollection(d, false, applyKeywordSearch, keyword, filterFrom, fD, filterTo, tD);
                    //step 2: data translation
                    dataSetD.loadFromInstancesHashMap(instancesD, new EventFactory(dateTimeFormatter, filterFrom, fromDate, filterTo, toDate, applyKeywordSearch, keyword), separator, dateTimeFormatter, filterFrom, fromDate, filterTo, toDate, applyKeywordSearch, keyword);
                }
                if (y) {
                    //step 1: data collection
                    instancesY = dataCollection(false, y, applyKeywordSearch, keyword, filterFrom, fD, filterTo, tD);
                    //step 2: data translation
                    dataSetY.loadFromInstancesHashMap(instancesY, new EventFactory(dateTimeFormatter, filterFrom, fromDate, filterTo, toDate, applyKeywordSearch, keyword), separator, dateTimeFormatter, filterFrom, fromDate, filterTo, toDate, applyKeywordSearch, keyword);
                }


            } else { //use local data
                //step 1+2: data collection and translation
                if (d) {
                    //dataSetD.loadFromTSV(new File("../data/dbpedia-1.tsv"),
                    //       new EventFactory(), "events/event", separator, dateTimeFormatter, filterFrom, fromDate, filterTo, toDate, applyKeywordSearch, keyword);
                    dataSetD.loadFromXML(new File("../data/dbpedia_events.xml"),
                            new EventFactory(dateTimeFormatter, filterFrom, fromDate, filterTo, toDate, applyKeywordSearch, keyword),
                            "events/event");//, dateTimeFormatter, filterFrom, fromDate, filterTo, toDate, applyKeywordSearch, keyword);
                }
                if (y) {
                    //dataSetY.loadFromTSV(new File("../data/yago-1.tsv"),
                    //       new EventFactory(), "events/event", separator, dateTimeFormatter, filterFrom, fromDate, filterTo, toDate, applyKeywordSearch, keyword);
                    dataSetY.loadFromXML(new File("../data/yago_events.xml"),
                            new EventFactory(dateTimeFormatter, filterFrom, fromDate, filterTo, toDate, applyKeywordSearch, keyword),
                            "events/event");
                }
            }

            //check size of data sets (could be zero due to keyword search or date filters)
            boolean dHasItems = checkDataSetSize(dataSetD);
            boolean yHasItems = checkDataSetSize(dataSetY);


            //step 3: identity resolution
            if (d && y && dHasItems && yHasItems) {

                //BLOCKING FRAMEWORK
                WDI_BlockingFramework_Combiner blockingFramework = new WDI_BlockingFramework_Combiner();
                blockingFramework.runBlocking(dataSetD, dataSetY, "../data/sameAs_combined_with_negative_len3.tsv");//dbpedia_2_yago_s.tsv


                //WDI FRAMEWORK
                de.uni_mannheim.informatik.wdi.model.ResultSet<Correspondence<Event, DefaultSchemaElement>> correspondences =
                        Events_IdentityResolution_Main.runIdentityResolution(dataSetD, dataSetY, separator);

                //step 4: data fusion
                if (correspondences.size() > 0) {
                    fusedDataSet = Events_DataFusion_Main.runDataFusion(dataSetD, dataSetY, correspondences, separator, dateTimeFormatter, filterFrom, fromDate, filterTo, toDate, applyKeywordSearch, keyword);
                    if (fusedDataSet != null) {
                        eventList.addAll(collectRecords(fusedDataSet));
                    }
                } else {
                    System.out.println("No correspondences found. Data fusion is not possible.");
                    // combine original data sets

                }
                if (!includeOnlyFusedEvents) {
                    //combine data sets
                    if (fusedDataSet != null) {
                        //get all IDs of fused records
                        Set<String> fusedRecordIDs = fusedDataSet.getOriginalIdsOfFusedRecords();
                        //add unfused records from DBpedia
                        eventList.addAll(collectNotFusedRecords(dataSetD, fusedRecordIDs));
                        // unfused records from YAGO
                        eventList.addAll(collectNotFusedRecords(dataSetY, fusedRecordIDs));
                    } else {
                        //just add original data sets
                        eventList.addAll(collectRecords(dataSetD));
                        eventList.addAll(collectRecords(dataSetY));
                    }
                }


            } else {
                if (!includeOnlyFusedEvents) {
                    //return single data set
                    if (d) {
                        System.out.println("dataSetD has " + dataSetD.size() + " records in total");
                        eventList.addAll(collectRecords(dataSetD));

                    }
                    if (y) {
                        System.out.println("dataSetY has " + dataSetY.size() + " records in total");
                        eventList.addAll(collectRecords(dataSetY));
                    }
                }
            }

            System.out.println(eventList.size() + " of the records in the eventList have valid coordinates");
            // convert data to JSON
            jsonString = mapper.writeValueAsString(eventList);
        }

        return jsonString;
    }

    private boolean checkDataSetSize(FusableDataSet<Event, DefaultSchemaElement> dataSet) {
        if (dataSet.getSize() == 0) {
            return false;
        }
        return true;
    }

    private List<Event> collectNotFusedRecords(FusableDataSet<Event, DefaultSchemaElement> fusableDataSet, Set<String> fusedRecordIDs) {
        return fusableDataSet.getRecords()
                .stream()
                .filter(event -> !fusedRecordIDs.contains(event.getIdentifier()) && event.hasValue(Event.COORDINATES))
                .collect(Collectors.toList());
    }



    /**
     * Collect all records of the given {@link FusableDataSet} that have valid coordinates
     * @param fusableDataSet
     * @return List<Event>
     */
    private List<Event> collectRecords(FusableDataSet<Event, DefaultSchemaElement> fusableDataSet) {
        return fusableDataSet.getRecords()
                .stream()
                .filter(event -> event.hasValue(Event.COORDINATES))
                .collect(Collectors.toList());
    }

    /**
     * Check dateString against empty values
     * @param dateString
     * @return boolean
     */
    private boolean checkDateFilter(String dateString) {
        if(!dateString.equals("")) {
            return true;
        }
        return false;
    }

    /**
     * Data Collection Process
     * @param d query DBpedia (boolean)
     * @param y query YAGO (boolean)
     * @param applyKeywordSearch
     * @param keyword keyword for the labels
     * @param filterFrom
     * @param fD fromDate (String)
     * @param filterTo
     * @param tD toDate (String)
     * @return results from querying the public endpoints
     */
    public HashMap<String, HashSet<String[]>>  dataCollection(boolean d, boolean y, boolean applyKeywordSearch, String keyword, boolean filterFrom, String fD, boolean filterTo, String tD) {
        System.out.println("start data collection");
        String result = "";
        QueryString qs = new QueryString();

        // create Wrapper for each selected source
        if (d) { // DBpedia
            String dbpedia = "http://dbpedia.org/sparql";
            System.out.println("Query " + dbpedia);
            String queryString = qs.getDBpediaQueryString(applyKeywordSearch, keyword, filterFrom, fD, filterTo, tD);

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
            String queryString = qs.getYagoQueryString(applyKeywordSearch, keyword, filterFrom, fD, filterTo, tD);

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

