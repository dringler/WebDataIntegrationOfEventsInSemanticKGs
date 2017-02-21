/** 
 *
 * Copyright (C) 2015 Data and Web Science Group, University of Mannheim, Germany (code@dwslab.de)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.uni_mannheim.informatik.wdi.model;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.opencsv.CSVWriter;
import de.uni_mannheim.informatik.wdi.usecase.events.model.Event;
import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * {@link DefaultDataSet} class extended by functionalities for data fusion
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 * @param <RecordType>
 */
public class FusableDataSet<RecordType extends Matchable & Fusable<SchemaElementType>, SchemaElementType> extends
		DefaultDataSet<RecordType, SchemaElementType> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double score;
	private DateTime date;

	/**
	 * Get the set of all original IDs of the fused records
	 * @return
	 */
	public Set<String> getOriginalIdsOfFusedRecords() {
		return originalIdIndex.keySet();
	}

	private Map<String, RecordType> originalIdIndex = new HashMap<>();

	/**
	 * Add an original ID to a fused record (can be called multiple times)
	 * 
	 * @param record
	 * @param id
	 */
	public void addOriginalId(RecordType record, String id) {
		originalIdIndex.put(id, record);
	}

	@Override
	public RecordType getRecord(String identifier) {
		RecordType record = super.getRecord(identifier);

		if (record == null) {
			record = originalIdIndex.get(identifier);
		}

		return record;
	}

	/**
	 * Returns the score of this dataset
	 * 
	 * @return
	 */
	public double getScore() {
		return score;
	}

	/**
	 * Sets the score of this dataset
	 * 
	 * @param score
	 */
	public void setScore(double score) {
		this.score = score;
	}

	/**
	 * Returns the date of this dataset
	 * 
	 * @return
	 */
	public DateTime getDate() {
		return date;
	}

	/**
	 * Sets the date of this dataset
	 * 
	 * @param date
	 */
	public void setDate(DateTime date) {
		this.date = date;
	}

	/**
	 * Calculates the overall density of this dataset
	 * 
	 * @return
	 */
	public double getDensity() {
		int values = 0;
		int attributes = 0;

		for (RecordType record : getRecords()) {
			values += getNumberOfValues(record);
			attributes += getNumberOfAttributes(record);
		}

		return (double) values / (double) attributes;
	}


	/**
	 * Returns the number of attributes that have a value for the given record
	 * 
	 * @param record
	 * @return
	 */
	protected int getNumberOfValues(RecordType record) {
		int cnt = 0;
		//for (SchemaElementType att : getAttributes()) {
		for (DefaultSchemaElement att : getDefaultAttributes()) {
			cnt += record.hasValue((SchemaElementType) att) ? 1 : 0;
		}
		return cnt;
	}

	/**
	 * Returns the number of attributes for the given record
	 * 
	 * @param record
	 * @return
	 */
	protected int getNumberOfAttributes(RecordType record) {
		//return getAttributes().size();
		return getDefaultAttributes().size();
	}

	/**
	 * Calculates the density for all attributes of the records in this dataset
	 * 
	 * @return
	 */
	public Map<SchemaElementType, Double> getAttributeDensities() {
		// counts how often the attribute exists (should be equal to the number
		// of records
		Map<SchemaElementType, Integer> sizes = new HashMap<>();
		// counts how often the attribute has a value
		Map<SchemaElementType, Integer> values = new HashMap<>();

		for (RecordType record : getRecords()) {

			//for (SchemaElementType att : getAttributes()) {
			for (DefaultSchemaElement defAtt : getDefaultAttributes()) {
				SchemaElementType att = (SchemaElementType) defAtt;

				Integer size = sizes.get(att);
				if (size == null) {
					size = 0;
				}
				sizes.put(att, size + 1);

				if (record.hasValue(att)) {
					Integer value = values.get(att);
					if (value == null) {
						value = 0;
					}
					values.put(att, value + 1);
				}
			}

		}

		Map<SchemaElementType, Double> result = new HashMap<>();

		for (SchemaElementType att : sizes.keySet()) {
			Integer valueCount = values.get(att);
			if (valueCount == null) {
				valueCount = 0;
			}
			double density = (double) valueCount / (double) sizes.get(att);
			result.put(att, density);
		}

		return result;
	}

	private Map<SchemaElementType,Map<Integer, Integer>> getAttributeValueDistributions() {
        Map<SchemaElementType, Map<Integer, Integer>> result = new HashMap<>();

        boolean isFirst = true;

        for (RecordType record : getRecords()) {

            //for (SchemaElementType att : getAttributes()) {
            for (DefaultSchemaElement defAtt : getDefaultAttributes()) {
                SchemaElementType att = (SchemaElementType) defAtt;

                if (isFirst) {
                    //init HashMap
                    Map<Integer,Integer> initCountMap = new HashMap<>();
                    for (int i = 0; i<50; i++) {
                        initCountMap.put(i, 0);
                        result.put(att, initCountMap);
                    }
                }

                Integer numOfValues = 0;
                if (record.hasValue(att)) {
                    numOfValues = record.getNumberOfValues(att);
                }

                    Map<Integer, Integer> countMap = result.get(att);
                    if (countMap == null) {
                        //init countMap
                        countMap = new HashMap<>();
                        countMap.put(numOfValues, 0);
                        result.put(att, countMap);
                    }
                    Integer count = countMap.get(numOfValues);
                    if (count == null) {
                        //init count
                        count = 0;
                    }
                    countMap.put(numOfValues, count + 1);

                    /*Integer value = values.get(att);
                    if (value == null) {
                        value = 0;
                    }
                    values.put(att, value + 1);
                    */
            }//end for each defAtt
            isFirst = false;
        }//end for each record

        return result;
	}

	/**
	 * Calculates the density for all attributes of the records in this dataset
	 * and prints the result to the console
	 */
	public void printDataSetDensityReport() {
		System.out
				.println(String.format("DataSet density: %.2f", getDensity()));
		System.out.println("Attributes densities:");
		Map<SchemaElementType, Double> densities = getAttributeDensities();
		for (SchemaElementType att : densities.keySet()) {
			System.out.println(String.format("\t%s: %.2f", att.toString(),
					densities.get(att)));
		}
	}

	public void printDataSetDensityDistributionReport(boolean writeToFile, String fileName) {

		System.out.println("Attribute/Value distribution:");
		Map<SchemaElementType, Map<Integer, Integer>> densities = getAttributeValueDistributions();
		for (SchemaElementType att : densities.keySet()) {
            Map<Integer, Integer> countMap = densities.get(att);
		    String countMapString = getCountMapString(countMap);

			System.out.println(//String.format("\t%s: %v", att.toString(),
                    att.toString() + " " +
					countMapString
                    //densities.get(att).toString()
            //)
            );
		}
		if (writeToFile) {
            writeAttributeValueDistributionToCSV(fileName, densities, getAttributeDensities());
        }

	}

    private String getCountMapString(Map<Integer, Integer> countMap) {
        String countMapString = "";
        //tree for sorting the keys
        Map<Integer, Integer> countTreeMap = new TreeMap<>(countMap);

        for (Integer numOfValues : countTreeMap.keySet()) {
            Integer recordCount = countTreeMap.get(numOfValues);
            countMapString += numOfValues + ": " + recordCount + ", ";
        }
        if (countMapString.length()>0) {
            countMapString = countMapString.substring(0, countMapString.length()-2);
        }
        return countMapString;
    }

    private void writeAttributeValueDistributionToCSV(String fileName, Map<SchemaElementType, Map<Integer, Integer>> densities, Map<SchemaElementType, Double> attDensities) {
        try {
            PrintWriter writer = new PrintWriter("/Users/curtis/git/MasterThesis/webApp/out/"+fileName, "UTF-8");
            int counter = 0;
            writer.println("DataSet density, " +  getDensity());
            counter++;
            writer.println("Attributes densities:");
            counter++;
            for (SchemaElementType att : attDensities.keySet()) {
                writer.println(att.toString() +", " + attDensities.get(att));
                counter++;
            }


            for (SchemaElementType att : densities.keySet()) {
                writer.println(att);
                counter++;
                writer.println("bucket, recordCount for " + att);
                counter++;
                Map<Integer, Integer> countTreeMap = new TreeMap<>(densities.get(att));
                for(Map.Entry<Integer, Integer> entry : countTreeMap.entrySet()) {
                    writer.println(entry.getKey() + ", " + entry.getValue());
                    counter++;
                }
                //String countMapString = getCountMapString(countMap);
                //writer.println(countMapString);
            }
            writer.close();
            System.out.println(counter + " lines written to " + fileName);
        } catch (IOException e) {
            System.out.println("error while writing to file " + fileName);
        }
	}

}
