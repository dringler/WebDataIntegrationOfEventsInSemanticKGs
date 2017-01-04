package de.uni_mannheim.informatik.wdi.datafusion.conflictresolution.date;

import de.uni_mannheim.informatik.wdi.datafusion.conflictresolution.ConflictResolutionFunction;
import de.uni_mannheim.informatik.wdi.model.Fusable;
import de.uni_mannheim.informatik.wdi.model.FusableValue;
import de.uni_mannheim.informatik.wdi.model.FusedValue;
import de.uni_mannheim.informatik.wdi.model.Matchable;

import java.util.Collection;

/**
 * Random {@link ConflictResolutionFunction}: Returns the first date value.
 * @author Daniel Ringler
 *
 * @param <ValueType>
 * @param <RecordType>
 */
public class FirstDate<ValueType, RecordType extends Matchable & Fusable<SchemaElementType>, SchemaElementType> extends ConflictResolutionFunction<ValueType, RecordType, SchemaElementType> {

    @Override
    public FusedValue<ValueType, RecordType, SchemaElementType> resolveConflict(
            Collection<FusableValue<ValueType, RecordType, SchemaElementType>> values) {

        if (values.size()>0) {
            for(FusableValue<ValueType, RecordType, SchemaElementType> value : values) {
                return new FusedValue<>(value);
            }
        }
        FusableValue<ValueType, RecordType, SchemaElementType> none = null;
        return new FusedValue<>(none);

    }

}
