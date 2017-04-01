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
package de.uni_mannheim.informatik.wdi.matching;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.opencsv.CSVWriter;
import de.uni_mannheim.informatik.wdi.matching.blocking.BlockedMatchable;
import de.uni_mannheim.informatik.wdi.model.*;
import de.uni_mannheim.informatik.wdi.usecase.events.model.Event;

/**
 * Evaluates a set of {@link Correspondence}s against a
 * {@link MatchingGoldStandard}.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 * @param <RecordType>
 */
public class MatchingEvaluator<RecordType extends Matchable, SchemaElementType> {

	private boolean verbose = false;

	public MatchingEvaluator() {
	}

	public MatchingEvaluator(boolean isVerbose) {
		verbose = isVerbose;
	}

	/**
	 * Evaluates the given correspondences against the gold standard
	 * 
	 * @param correspondences
	 *            the correspondences to evaluate
	 * @param goldStandard
	 *            the gold standard
	 * @return the result of the evaluation
	 */
	public Performance evaluateMatching(
			Collection<Correspondence<RecordType, SchemaElementType>> correspondences,
			MatchingGoldStandard goldStandard,
            boolean printResults) {
		int correct = 0;
		int matched = 0;
		int correct_max = goldStandard.getPositiveExamples().size();
		HashMap<String, Integer> matchingOutdegreesFirst = new HashMap<>(correspondences.size() + 1, 1.0f);
		HashMap<String, Integer> matchingOutdegreesSecond = new HashMap<>(correspondences.size() + 1, 1.0f);


		// keep a list of all unmatched positives for later output
		List<Pair<String, String>> positives = new ArrayList<>(
				goldStandard.getPositiveExamples());

		for (Correspondence<RecordType, SchemaElementType> correspondence : correspondences) {
			if (goldStandard.containsPositive(correspondence.getFirstRecord(),
					correspondence.getSecondRecord())) {
				correct++;
				matched++;

				addMatchingOutdegree(matchingOutdegreesFirst, correspondence.getFirstRecord());
				addMatchingOutdegree(matchingOutdegreesSecond, correspondence.getSecondRecord());

				if (verbose) {
				    if(printResults) {
                        System.out.println(String
                                .format("[correct] %s,%s,%s", correspondence
                                                .getFirstRecord().getIdentifier(),
                                        correspondence.getSecondRecord()
                                                .getIdentifier(), Double
                                                .toString(correspondence
                                                        .getSimilarityScore())));
                    }

					// remove pair from positives
					Iterator<Pair<String, String>> it = positives.iterator();
					while (it.hasNext()) {
						Pair<String, String> p = it.next();
						String id1 = correspondence.getFirstRecord()
								.getIdentifier();
						String id2 = correspondence.getSecondRecord()
								.getIdentifier();

						if (p.getFirst().equals(id1)
								&& p.getSecond().equals(id2)
								|| p.getFirst().equals(id2)
								&& p.getSecond().equals(id1)) {
							it.remove();
						}
					}
				}
			} else if (goldStandard.isComplete() || goldStandard.containsNegative(
					correspondence.getFirstRecord(),
					correspondence.getSecondRecord())) {
				matched++;

				if (verbose) {
                    if (printResults) {
                        System.out.println(String
                                .format("[wrong] %s,%s,%s", correspondence
                                                .getFirstRecord().getIdentifier(),
                                        correspondence.getSecondRecord()
                                                .getIdentifier(), Double
                                                .toString(correspondence
                                                        .getSimilarityScore())));
                    }
                }
			}
		}

		if (verbose) {
			// print all missing positive examples
            if(printResults) {
                for (Pair<String, String> p : positives) {
                    System.out.println(String.format("[missing] %s,%s",
                            p.getFirst(), p.getSecond()));
                }
            }
		}

		//System.out.println("Found links/URI first: " + getSortedHashMap(matchingOutdegreesFirst).toString());
		//System.out.println("Found links/URI second: " + getSortedHashMap(matchingOutdegreesSecond).toString());

		//saveElementOutdegreesToDisk("dbpedia", matchingOutdegreesFirst);
        //saveElementOutdegreesToDisk("yago", matchingOutdegreesSecond);

		return new Performance(correct, matched, correct_max);
	}

    private void saveElementOutdegreesToDisk(String filename, HashMap<String, Integer> matchingOutdegrees) {
	    try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("./out/" + filename + "_elementOutdegree.tsv"));
            HashMap<String, Integer> sortedSatchingOutdegrees = getSortedHashMap(matchingOutdegrees);
            for (String k : sortedSatchingOutdegrees.keySet()) {
                writer.write(k + "\t" + matchingOutdegrees.get(k) + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private HashMap<String, Integer>  getSortedHashMap(HashMap<String, Integer> matchingOutdegreesFirst) {
		HashMap<String, Integer> sortedMap =
				matchingOutdegreesFirst.entrySet().stream()
						.sorted(Map.Entry.comparingByValue())
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
								(e1, e2) -> e1, LinkedHashMap::new));
		return sortedMap;

	}


	private void addMatchingOutdegree(HashMap<String, Integer> matchingOutdegrees, RecordType record) {
		String uri = record.getIdentifier();
		//check if uri is already contained in hashmap
		if (matchingOutdegrees.containsKey(uri)) {
			//increment outdegree count
			int newCount = matchingOutdegrees.get(uri) + 1;
			matchingOutdegrees.put(uri, newCount);
		} else {
			//create new key with count=1
			matchingOutdegrees.put(uri, 1);
		}
	}
}
