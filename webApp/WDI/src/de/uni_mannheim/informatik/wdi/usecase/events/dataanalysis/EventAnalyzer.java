package de.uni_mannheim.informatik.wdi.usecase.events.dataanalysis;

import de.uni_mannheim.informatik.wdi.model.DefaultSchemaElement;
import de.uni_mannheim.informatik.wdi.model.FusableDataSet;
import de.uni_mannheim.informatik.wdi.usecase.events.model.Event;

/**
 * Analyse the event XML data
 * Created on 23/01/17.
 * @author Daniel Ringler
 */
public class EventAnalyzer {

    public EventAnalyzer() {}

    public void runAnalysis(FusableDataSet<Event, DefaultSchemaElement> dataSet, String kg) {
        if (dataSet != null && dataSet.size() > 0) {

            System.out.println("attribute densities of dataset " + kg);
            dataSet.printDataSetDensityReport();
            dataSet.printDataSetDensityDistributionReport(true, "dataSetDensityDistributionReport_"+kg+".csv");


        } else {
            System.out.println("Analysis not possible (no records or dataset is null).");
        }
    }
}
