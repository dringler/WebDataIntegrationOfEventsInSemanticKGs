import de.uni_mannheim.informatik.wdi.model.DefaultSchemaElement;
import de.uni_mannheim.informatik.wdi.model.FusableDataSet;
import de.uni_mannheim.informatik.wdi.usecase.events.model.Event;
import de.uni_mannheim.informatik.wdi.usecase.events.model.EventFactory;
import de.uni_mannheim.informatik.wdi.utils.WDI_BlockingFramework_Combiner;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * Created by Daniel on 07/02/17.
 */
public class BlockingAnalysis {
    public static void main(String[] args) throws Exception {
        System.out.println("Blocking Analysis");
        //System.out.println("Working Directory = " + System.getProperty("user.dir"));

        //get user input: use sample or full data files?
        UserInput ui = new UserInput();
        boolean testing = ui.getDatasetUserInput();
        int s = ui.getSampleSizeUserInput();

        //get file paths based on user input
        FileLoader fl = new FileLoader();
        String[] paths = fl.getPaths(testing);

        //create FusableDataSet objects
        FusableDataSet<Event, DefaultSchemaElement> dataSetD = new FusableDataSet<>();
        FusableDataSet<Event, DefaultSchemaElement> dataSetY = new FusableDataSet<>();

        //load XML data sets
        dataSetD.loadFromXML(new File(paths[0]),
                new EventFactory(null, false, null, false, null, false, ""),
                "events/event");

        dataSetY.loadFromXML(new File(paths[1]),
                new EventFactory(null, false, null, false, null, false, ""),
                "events/event");

        if (s>0) {
            dataSetD.sampleRecords(s);
            dataSetY.sampleRecords(s);
        }

        //run blocking
        WDI_BlockingFramework_Combiner blockingFramework = new WDI_BlockingFramework_Combiner();

        //blockingFramework.runBlocking(dataSetD, dataSetY, paths[2]);

        //blockingFramework.analyzeStBl_BlFi(dataSetD, dataSetY, paths[2]);

        blockingFramework.analyzeStBl_bestBlFi_MeBl(dataSetD, dataSetY, paths[2]);
    }
}
