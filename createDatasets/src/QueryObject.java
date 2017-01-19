import org.apache.jena.query.*;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Created by Daniel on 12/01/17.
 */
public class QueryObject {

    private String service;

    public String getService() {
        return this.service;
    }

    /**
     * QueryObject
     * @param service
     */
    public QueryObject(String service) {
        this.service = service;
    }


    /**
     * Query the SPARQL endpoint
     * @param queryString  query to send to the endpoint
     * @return ResultSet
     */
    public ResultSet queryEndpoint(String queryString) {
        while (true) {
            try {
                Query query = QueryFactory.create(queryString);
                QueryExecution qe = QueryExecutionFactory.sparqlService(this.service, query);

                try {
                    ResultSet results = qe.execSelect();
                    ResultSet resultsCopy = ResultSetFactory.copyResults(results);
                    qe.close();
                    if (resultsCopy != null)
                        return resultsCopy;
                } catch (QueryExceptionHTTP http) {
                    try {
                        System.out.println("error while executing query. waiting for 5 seconds. " + LocalDateTime.now());
                        System.out.println(queryString);
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    qe.close();
                }

            } catch (QueryParseException e) {
                System.out.println("Exception for : " + queryString);
                return null;
            }
        }
    }
    /**
     * Test the service connection
     * @return boolean (true if service is up)
     */
    public boolean testConnection() {
        boolean isUp = false;
        String queryTest = "ASK {}";

        QueryExecution qeTest = QueryExecutionFactory.sparqlService(this.service, queryTest);

        try {
            if(qeTest.execAsk()) {
                System.out.println(this.service + " is up");
                isUp = true;
            }
        } catch (QueryExceptionHTTP e) {
            System.out.println(this.service + " is down: " + e);
        } finally {
            qeTest.close();
        }
        return isUp;
    }

}
