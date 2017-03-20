package de.uni_mannheim.informatik.wdi.matching.blocking;

import de.uni_mannheim.informatik.wdi.model.ResultSet;
import de.uni_mannheim.informatik.wdi.processing.Group;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Daniel Ringler on 20/03/17.
 * Based on BlockFiltering by http://l3s.de/~papadakis/erFramework.html
 */
public class BlockFiltering  {

    public static <RecordType, KeyType> List<Map<KeyType, List<RecordType>>> runBlockFiltering(double r, Map<KeyType, List<RecordType>> joinKeys1, Map<KeyType, List<RecordType>> joinKeys2) {
        List<Map<KeyType, List<RecordType>>> resultList = new ArrayList<>();

        HashMap<KeyType, Long> blockCardinalities = new HashMap<>();
        HashMap<RecordType, List<KeyType>> entityBlockAssignments1 =  new HashMap<>();
        HashMap<RecordType, List<KeyType>> entityBlockAssignments2 = new HashMap<>();



        /*
        countEntities(blocks);
        sortBlocks(blocks);
        getLimits(blocks);
        initializeCounters();
        restructureBlocks(blocks);
    */

        //get block cardinalities(=number of comparisons in block)
        getBlockCardinalities(joinKeys1, joinKeys2, blockCardinalities);

        //sort blocks in desc order of cardinality
       /* HashMap<String, Long> sortedBlockCardinalities = blockCardinalities.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1,e2)->e1,
                        LinkedHashMap::new));
        */

        // get entities with all block assignments
        getEntityBlockAssignment(joinKeys1, joinKeys2, entityBlockAssignments1, entityBlockAssignments2);


        //delete entities from largest blocks
        removeEntitiesFromBlocks(r, blockCardinalities, entityBlockAssignments1, joinKeys1);
        removeEntitiesFromBlocks(r, blockCardinalities, entityBlockAssignments2, joinKeys2);

        //getBlockCardinalities(joinKeys1, joinKeys2, blockCardinalities);

        resultList.add(joinKeys1);
        resultList.add(joinKeys2);

        return resultList;
    }

    private static <KeyType, RecordType> void getEntityBlockAssignment(Map<KeyType, List<RecordType>> joinKeys1,
                                                                       Map<KeyType, List<RecordType>> joinKeys2,
                                                                       HashMap<RecordType, List<KeyType>> entityBlockAssignments1,
                                                                       HashMap<RecordType, List<KeyType>> entityBlockAssignments2) {
        for (KeyType key1 : joinKeys1.keySet()) {
            List<RecordType> block1 = joinKeys1.get(key1);
            List<RecordType> block2 = joinKeys2.get(key1);
            if (block2 != null) {
                //cast objects
                ResultSet<RecordType> rs1 = (ResultSet) ((Group) block1.get(0)).getRecords();
                ResultSet<RecordType> rs2 = (ResultSet) ((Group) block2.get(0)).getRecords();
                // get block assignments for the entities
                addRecordsToBlockAssignement(key1, rs1, entityBlockAssignments1);
                addRecordsToBlockAssignement(key1, rs2, entityBlockAssignments2);
            } else {
                addEntitiesFromSingleKG(key1, block1, entityBlockAssignments1);

            }
        }
        //add single blocks from joinKeys2
        for (KeyType key2 : joinKeys2.keySet()) {
            List<RecordType> block1 = joinKeys1.get(key2);
            List<RecordType> block2 = joinKeys2.get(key2);
            if (block1 == null) {
                addEntitiesFromSingleKG(key2, block2, entityBlockAssignments2);
            }
        }
    }

    private static <KeyType, RecordType> void getBlockCardinalities(Map<KeyType, List<RecordType>> joinKeys1, Map<KeyType, List<RecordType>> joinKeys2, HashMap<KeyType, Long> blockCardinalities) {
        long totalCardinality = 0;
        for (KeyType key1 : joinKeys1.keySet()) {
            List<RecordType> block1 = joinKeys1.get(key1);
            List<RecordType> block2 = joinKeys2.get(key1);
            if (block2 != null) {
                //cast objects
                ResultSet<RecordType> rs1 = (ResultSet) ((Group) block1.get(0)).getRecords();
                ResultSet<RecordType> rs2 = (ResultSet) ((Group) block2.get(0)).getRecords();
                // get block cardinalities
                long blockCardinality = rs1.size() * rs2.size();
                blockCardinalities.put(key1, blockCardinality);
                totalCardinality = totalCardinality + blockCardinality;
            } else {
                blockCardinalities.put(key1, (long) 0);
            }
        }
        //add single blocks from joinKeys2
        for (KeyType key2 : joinKeys2.keySet()) {
            List<RecordType> block1 = joinKeys1.get(key2);
            List<RecordType> block2 = joinKeys2.get(key2);
            if (block1 == null) {
                blockCardinalities.put(key2, (long) 0);
            }
        }
        System.out.println(String.format("Total block cardinality: %,d", totalCardinality));
    }

    private static <KeyType, RecordType> void removeEntitiesFromBlocks(double r,
                                                                       HashMap<KeyType, Long> blockCardinalities,
                                                                       HashMap<RecordType, List<KeyType>> entityBlockAssignments,
                                                                       Map<KeyType, List<RecordType>> joinKeys) {
        for (Map.Entry<RecordType, List<KeyType>> entry : entityBlockAssignments.entrySet()) {
            int numberOfBlocks = entry.getValue().size();
            int limitOfBlocks = (int) Math.floor(numberOfBlocks * r);

            // get block cardinalities of entity
            HashMap<KeyType, Long> entityBlocks = new HashMap<>();
            for (KeyType block : entry.getValue()) {
                entityBlocks.put(block, blockCardinalities.get(block));
            }

            //sort blocks on cardinality
            HashMap<KeyType, Long> sortedEntityBlocks = entityBlocks.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .collect(Collectors.toMap(Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1,e2)->e1,
                            LinkedHashMap::new));


            //remove entity from largest blocks
            int processedBlocks = 0;
            for (KeyType block : sortedEntityBlocks.keySet()) {
                if (processedBlocks >= limitOfBlocks) {
                    //delete entity from blocks
                    //System.out.println((int) ((ResultSet) ((Group) joinKeys.get(block).get(0)).getRecords()).size());
                    ((Group) joinKeys.get(block).get(0)).getRecords().remove(entry.getKey());
                    //System.out.println((int) ((ResultSet) ((Group) joinKeys.get(block).get(0)).getRecords()).size());
                }
                processedBlocks++;
            }

        }
    }


    private static <KeyType, RecordType> void addEntitiesFromSingleKG(KeyType key, List<RecordType> block, HashMap<RecordType, List<KeyType>> entityBlockAssignments) {
        Group<KeyType, RecordType> b = (Group) block.get(0);
        ResultSet<RecordType> rs = (ResultSet) ((Group) block.get(0)).getRecords();
        //sortedBlockCardinalities.put(blockCardinality, key.toString());
        addRecordsToBlockAssignement(key, rs, entityBlockAssignments);
    }

    private static <KeyType, RecordType> void addRecordsToBlockAssignement(KeyType blockKey, ResultSet<RecordType> rs, HashMap<RecordType, List<KeyType>> entityBlockAssignments) {
        for (RecordType r : rs.get()) {
            //Record r  = (Record) rt;
            //String eventIdentifier = e.getIdentifier();
            addRecordToBlockAssignement(r, blockKey, entityBlockAssignments);
        }
    }

    private static <KeyType, RecordType> void addRecordToBlockAssignement(RecordType r, KeyType blockKey, HashMap<RecordType, List<KeyType>> entityBlockAssignments) {
        if (entityBlockAssignments.containsKey(r)) {
            List<KeyType> blockKeys = entityBlockAssignments.get(r);
            blockKeys.add(blockKey);
        } else {
            // create new entry
            ArrayList<KeyType> blockKeys = new ArrayList<>();
            blockKeys.add(blockKey);
            entityBlockAssignments.put(r, blockKeys);
        }
    }

}
