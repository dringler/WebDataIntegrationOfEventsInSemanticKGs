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
package de.uni_mannheim.informatik.wdi.datafusion.conflictresolution;

import java.util.Collection;

import de.uni_mannheim.informatik.wdi.model.Fusable;
import de.uni_mannheim.informatik.wdi.model.FusableValue;
import de.uni_mannheim.informatik.wdi.model.FusedValue;
import de.uni_mannheim.informatik.wdi.model.Matchable;

/**
 * The abstract super class for all conflict resolution functions
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 * @param <ValueType>
 * @param <RecordType>
 */
public abstract class ConflictResolutionFunction<ValueType, RecordType extends Matchable & Fusable<SchemaElementType>, SchemaElementType> {

	/**
	 * Fuses a collection of fusable values by applying the conflict resolution
	 * @param values
	 * @return
	 */
	public abstract FusedValue<ValueType, RecordType, SchemaElementType> resolveConflict(Collection<FusableValue<ValueType, RecordType, SchemaElementType>> values);
	
}
