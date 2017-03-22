import de.uni_mannheim.informatik.wdi.model.DefaultSchemaElement;
import de.uni_mannheim.informatik.wdi.model.FusableDataSet;
import de.uni_mannheim.informatik.wdi.usecase.events.model.Event;
import de.uni_mannheim.informatik.wdi.usecase.events.model.EventFactory;
import de.uni_mannheim.informatik.wdi.utils.WDI_BlockingFramework_Combiner;

import java.io.File;

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
        boolean gsFiles = ui.getGsUserInput();
        boolean gsNegativeFiles = ui.getGsWithNegativeUserInput();
        //int s = ui.getSampleSizeUserInput();
        boolean getBestParameter = ui.getBestParameter();
        int blockingStep = ui.getBlockingStep();
        int blockingMethod = ui.getBlockingMethod();

        //get file paths based on user input
        FileLoader fl = new FileLoader();
        String[] paths = fl.getPaths(testing, gsFiles, gsNegativeFiles);

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

        /*if (s>0) {
            dataSetD.sampleRecords(s);
            dataSetY.sampleRecords(s);
        }*/

        //run blocking
        WDI_BlockingFramework_Combiner blockingFramework = new WDI_BlockingFramework_Combiner();

        //blockingFramework.runBlocking(dataSetD, dataSetY, paths[2]);

        switch(blockingStep) {
            case 0: //BlBu
                if (blockingMethod==0) {
                    if (getBestParameter) {
                        System.out.println("Block Building with Token Blocking is parameter-free.");
                    } else {
                        System.out.println("Measure runtime for Block Building using Standard Blocking.");
                        blockingFramework.analyzeRuntime_BlBu(blockingMethod, dataSetD, dataSetY, paths[2]); //parameter-free
                    }
                } else if (blockingMethod==1) {
                    if (getBestParameter) {
                        System.out.println("Get the best parameters for Attribute Clustering.");
                        blockingFramework.getBestParameterForACl(dataSetD, dataSetY, paths[2]);
                    } else {
                        System.out.println("Measure runtime for Block Building using Attribute Clustering.");
                        blockingFramework.analyzeRuntime_BlBu(blockingMethod, dataSetD, dataSetY, paths[2]);
                    }
                }
                break;
            case 1: //BlFi
                if (getBestParameter) {
                    System.out.println("Get the best parameters for Block Filtering with Blocking Method "+ blockingMethod + ".");
                    blockingFramework.getBestParameterForBlFi(blockingMethod, dataSetD, dataSetY, paths[2]);
                } else {
                    System.out.println("Measure runtime for Block Filtering with Blocking Method "+ blockingMethod + ".");
                    blockingFramework.analyzeRuntimeForBlFi(blockingMethod, dataSetD, dataSetY, paths[2]);
                }
                break;
            case 2: //MeBl
                if (getBestParameter) {
                    System.out.println("Get the best parameters for Meta Blocking with Blocking Method " + blockingMethod + " and Block Filtering.");
                    blockingFramework.getBestParameterForMeBl_bestBlFi(blockingMethod, dataSetD, dataSetY, paths[2]);
                } else {
                    System.out.println("Measure runtime for Meta Blocking with Blocking Method " + blockingMethod + " and Block Filtering.");
                    blockingFramework.analyzeRuntimeForMeBl_preSteps_StBl_bestBlFi(blockingMethod,dataSetD, dataSetY, paths[2]);
                }
                break;



        }

    }
}
