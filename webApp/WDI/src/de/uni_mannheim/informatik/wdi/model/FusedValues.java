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

import java.util.*;

/**
 * Represents a list of fused values in the data fusion process
 *
 * @author Daniel Ringler
 *
 * @param <ValueType>
 * @param <RecordType>
 */
public class FusedValues<ValueType, RecordType extends Matchable & Fusable<SchemaElementType>, SchemaElementType> {

    private List<ValueType> values;
    private Map<RecordType, FusableDataSet<RecordType, SchemaElementType>> originalRecords = new HashMap<>();

    /**
     * Creates a fused value without any provenance information
     *
     * @param values
     */
    public FusedValues(List<ValueType> values) {
        this.values = values;
    }

    /**
     * Creates a fused value with the original record and dataset as provenance
     * information
     *
     * @param values
     */
    public FusedValues(FusableValues<ValueType, RecordType, SchemaElementType> values) {
        if (values != null) {
            this.values = values.getValues();
            addOriginalRecord(values.getRecord(), values.getDataset());
        }
    }

    /**
     * Returns the fused value
     *
     * @return
     */
    public List<ValueType> getValues() {
        return values;
    }

    /**
     * Returns a map record -> dataset containing all original records that are
     * represented by this fused value
     *
     * @return
     */
    public Map<RecordType, FusableDataSet<RecordType, SchemaElementType>> getOriginalRecords() {
        return originalRecords;
    }

    /**
     * Returns the IDs of all original records that are represented by this
     * fused value
     *
     * @return
     */
    public Collection<String> getOriginalIds() {
        Collection<String> result = new LinkedList<>();
        for (RecordType record : getOriginalRecords().keySet()) {
            result.add(record.getIdentifier());
        }
        return result;
    }

    /**
     * Adds an original record as provenance information
     *
     * @param record
     * @param dataset
     */
    public void addOriginalRecord(RecordType record,
                                  FusableDataSet<RecordType, SchemaElementType> dataset) {
        originalRecords.put(record, dataset);
    }

    /**
     * Adds an original record as provenance information
     *
     * @param value
     */
    public void addOriginalRecord(FusableValue<ValueType, RecordType, SchemaElementType> value) {
        originalRecords.put(value.getRecord(), value.getDataset());
    }
}
