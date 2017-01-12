import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import java.io.*;
import java.util.HashSet;

/**
 * Created by Daniel on 12/01/17.
 */
public class getD2YsameAs {
    public static void main(String[] args) {

        QueryObject dQ = new QueryObject( "http://dbpedia.org/sparql");
        if(dQ.testConnection()){
            HashSet<String> eventInstances = readEventInstancesFromCSV();
            System.out.println(eventInstances.size() + " events read from CSV");

            HashSet<String> resultSet = new HashSet<>(50000);

            int eventCounter = 0;
            for (String eventInstance : eventInstances) {
                String queryString = getQueryStringForEventInstance(eventInstance);
                ResultSet results = dQ.queryEndpoint(queryString);

                while (results.hasNext()) {
                    QuerySolution qs = results.next();
                    resultSet.add(eventInstance+ "\t" + qs.get("same").toString());
                }
                eventCounter++;
                if (eventCounter % 5000 == 0) {
                    System.out.println(eventCounter + " events processed");
                }
            }//end for loop for eventInstances

            System.out.println(resultSet.size() + " lines written to resultSet");

            writeTSVtoDisk(resultSet);


        }//end testConnection

    }

    private static String getQueryStringForEventInstance(String eventInstance) {
        return "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                "SELECT ?same  WHERE {\n" +
                "<"+ eventInstance +"> owl:sameAs ?same . \n" +
                "FILTER ( ( regex(str(?same), \'yago\', \'i\') ) )\n"+
                "}";
    }

    private static HashSet<String> readEventInstancesFromCSV() {
        String fileName = "EventInstanceURIs.csv";
        HashSet<String> eventInstances = new HashSet<>(14500);
        try {

            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = reader.readLine()) != null) {
                eventInstances.add(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            System.out.println(fileName + " not found");
        }
        return eventInstances;
    }

    private static void writeTSVtoDisk(HashSet<String> resultSet) {
        String fileName = "d2y_sameAs.tsv";
        int resultCounter = 0;
        try {
            PrintWriter writer = new PrintWriter(fileName, "UTF-8");
            for (String result : resultSet) {
                writer.println(result);
                resultCounter++;
            }
            writer.close();
            System.out.println(resultCounter + " lines written to " + fileName);
        } catch (IOException e) {
            System.out.println("error while writing to file " + fileName);
        }
    }


}
