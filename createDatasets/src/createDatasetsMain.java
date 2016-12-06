import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

/**
 * Created by curtis on 06/12/16.
 */
public class createDatasetsMain {
    public static void main(String[] args) {
        // PARAMETERS
        boolean dbpedia = true;
        boolean yago = true;
        boolean wikidata = true;

        //configure log4j
        org.apache.log4j.BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel(Level.OFF); //set console logger off

        String service = "";
        boolean dbIsUp;

        if (dbpedia) {
            service = "http://dbpedia.org/sparql";
            dbIsUp = testConnection(service);

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
