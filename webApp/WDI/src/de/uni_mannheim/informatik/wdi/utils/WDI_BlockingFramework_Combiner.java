package de.uni_mannheim.informatik.wdi.utils;

import BlockBuilding.AbstractBlockingMethod;
import BlockBuilding.MemoryBased.AttributeClusteringBlocking;
import BlockBuilding.MemoryBased.TokenBlocking;
import BlockProcessing.ComparisonRefinement.AbstractDuplicatePropagation;
import BlockProcessing.ComparisonRefinement.BilateralDuplicatePropagation;
import DataStructures.AbstractBlock;
import DataStructures.EntityProfile;
import DataStructures.IdDuplicates;
import Utilities.BlockStatistics;
import Utilities.RepresentationModel;
import Utilities.StatisticsUtilities;
import com.opencsv.CSVReader;
import de.uni_mannheim.informatik.wdi.model.DefaultSchemaElement;
import de.uni_mannheim.informatik.wdi.model.FusableDataSet;
import de.uni_mannheim.informatik.wdi.model.Pair;
import de.uni_mannheim.informatik.wdi.usecase.events.model.Event;
import de.uni_mannheim.informatik.wdi.usecase.events.model.Location;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

/**
 * Created by Daniel on 02/02/17.
 */
public class WDI_BlockingFramework_Combiner {
    private static HashMap<String, Integer> dIDs;
    private static HashMap<String, Integer> yIDs;

    private final static int NO_OF_BLOCKING_METHODS = 1;
    private final static int NO_OF_ITERATIONS = 1;

    private final static RepresentationModel ACL_DEFAULT_MODEL = RepresentationModel.TOKEN_UNIGRAM_GRAPHS;

    private static List<EntityProfile> convertFusableDataSetToEntityProfile(FusableDataSet<Event, DefaultSchemaElement> dataSet, HashMap<String, Integer> ids) {
        List<EntityProfile> list = new ArrayList<>(dataSet.getSize() + 10);

        int idCounter = 0;
        for (Event event : dataSet.getRecords()) {
            String eventURI = event.getIdentifier();
            EntityProfile entityProfile = new EntityProfile(eventURI);

            for (String label : event.getLabels()) {
                entityProfile.addAttribute("label", label);
            }
            for (LocalDate date : event.getDates()) {
                entityProfile.addAttribute("date", date.toString());
            }
            for (Pair<Double, Double> coordinatePair : event.getCoordinates()) {
                entityProfile.addAttribute("lat", coordinatePair.getFirst().toString());
                entityProfile.addAttribute("long", coordinatePair.getSecond().toString());
            }
            for (String same : event.getSames()) {
                entityProfile.addAttribute("same", same);
            }
            for (Location location : event.getLocations()) {
                for (String label : location.getLabels()) {
                    entityProfile.addAttribute("locationLabel", label);
                }
                for (Pair<Double, Double> coordinatePair : location.getCoordinates()) {
                    entityProfile.addAttribute("locationLat", coordinatePair.getFirst().toString());
                    entityProfile.addAttribute("locationLong", coordinatePair.getSecond().toString());
                }
                for (String same : location.getSames()) {
                    entityProfile.addAttribute("locationSame", same);
                }
            }

            list.add(entityProfile);
            //add to ID HashMap
            ids.put(eventURI, idCounter);
            idCounter++;

        }
        return list;

    }

    private static Set<IdDuplicates> convertGoldStandardToIdDuplicatesSet(String filePath) {
        Set<IdDuplicates> duplicatesSet = new HashSet<>();
        try {
            CSVReader reader = new CSVReader(new FileReader(new File(filePath)), '\t', '\"' , 1);

            String[] lineValues;
            //HashMap<instanceURI, HashSet<lineValues>>
            while ((lineValues = reader.readNext()) != null) {
                if (lineValues.length>2) {//check for complete line
                    if (lineValues[2].equals("true")) {//check for duplicate matches
                        String dURI = lineValues[0];
                        String yURI = lineValues[1];
                        if (dIDs.containsKey(dURI) && yIDs.containsKey(yURI)) {//check if URIs are contained in the data sets
                            IdDuplicates idDuplicates = new IdDuplicates(dIDs.get(dURI), yIDs.get(yURI));
                            duplicatesSet.add(idDuplicates);
                        }//done adding idDuplicates
                    }//"false"
                }//lineValues.length <=2
            }//processed all lines

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return duplicatesSet;
    }

    public static void runBlocking(FusableDataSet<Event, DefaultSchemaElement> dataSetD, FusableDataSet<Event, DefaultSchemaElement> dataSetY, String goldStandardFilePath) {
        //params

        List<Double>[] comparisons = new List[NO_OF_BLOCKING_METHODS];
        List<Double>[] pc = new List[NO_OF_BLOCKING_METHODS];
        List<Double>[] pq = new List[NO_OF_BLOCKING_METHODS];
        List<Double>[] blbuTimes = new List[NO_OF_BLOCKING_METHODS];
        List<Double>[] blclTimes = new List[NO_OF_BLOCKING_METHODS];
        List<Double>[] oTimes = new List[NO_OF_BLOCKING_METHODS];


        //init HashMaps with <URI, ID>
        dIDs = new HashMap<>(dataSetD.size()+10,1.0f);
        yIDs = new HashMap<> (dataSetY.size()+10, 1.0f);

        //convert datasets
        List<EntityProfile>[] profiles = new List[2];
        profiles[0] = WDI_BlockingFramework_Combiner.convertFusableDataSetToEntityProfile(dataSetD, dIDs);
        profiles[1] = WDI_BlockingFramework_Combiner.convertFusableDataSetToEntityProfile(dataSetY, yIDs);

        //gold standard
        Set<IdDuplicates> goldStandardMatches = WDI_BlockingFramework_Combiner.convertGoldStandardToIdDuplicatesSet(goldStandardFilePath);
        AbstractDuplicatePropagation abp = new BilateralDuplicatePropagation(goldStandardMatches);


        for (int methodId = 0; methodId < NO_OF_BLOCKING_METHODS; methodId++) {
            comparisons[methodId] = new ArrayList<>();
            pc[methodId] = new ArrayList<>();
            pq[methodId] = new ArrayList<>();
            oTimes[methodId] = new ArrayList<>();
            blbuTimes[methodId] = new ArrayList<>();
            blclTimes[methodId] = new ArrayList<>();
        }

        for (int iterations = 0; iterations < NO_OF_ITERATIONS; iterations++) {
            for (int methodId = 0; methodId < NO_OF_BLOCKING_METHODS; methodId++) {

                long time1 = System.currentTimeMillis();
                AbstractBlockingMethod method = null;
                switch (methodId) {
                    case 0:
                        method = new TokenBlocking(profiles);
                        break;
                    case 1:
                        method = new AttributeClusteringBlocking(ACL_DEFAULT_MODEL, profiles);
                        break;
                }
                List<AbstractBlock> blocks = method.buildBlocks();

                long time2 = System.currentTimeMillis();
                double overheadTime = time2 - time1;
                System.out.println("Overhead time\t:\t" + overheadTime);

                BlockStatistics bStats = new BlockStatistics(blocks, abp);
                double[] results = bStats.applyProcessing();

                comparisons[methodId].add(results[2]);
                pc[methodId].add(results[0]);
                pq[methodId].add(results[1]);
                oTimes[methodId].add(overheadTime);
            }
            for (int methodId = 0; methodId < NO_OF_BLOCKING_METHODS; methodId++) {
                System.out.println("\n\n\n\nCurrent method id\t:\t" + (methodId + 1));
                System.out.println("Average PC\t:\t" + StatisticsUtilities.getMeanValue(pc[methodId]));
                System.out.println("Average PQ\t:\t" + StatisticsUtilities.getMeanValue(pq[methodId]));
                System.out.println("Average Comparisons\t:\t" + StatisticsUtilities.getMeanValue(comparisons[methodId]));
                System.out.println("Average Overhead Time\t:\t" + StatisticsUtilities.getMeanValue(oTimes[methodId]));
            }
        }
    }
}
