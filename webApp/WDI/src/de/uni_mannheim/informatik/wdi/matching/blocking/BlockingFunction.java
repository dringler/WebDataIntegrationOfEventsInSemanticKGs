package de.uni_mannheim.informatik.wdi.matching.blocking;

import de.uni_mannheim.informatik.wdi.usecase.events.model.Event;

import java.util.HashSet;

/**
 * Created by Daniel Ringler on 21/03/17.
 */
public class BlockingFunction {
    public static MultiBlockingKeyGenerator<Event> getStandardBlockingFunctionAllAttributes() {

        MultiBlockingKeyGenerator<Event> standardBlocking = new MultiBlockingKeyGenerator<Event>() {
            @Override
            public HashSet<String> getMultiBlockingKey(Event event) {

                HashSet<String> keys = new HashSet<>();
                for (String value : event.getAllAttributeValues(' ')) {
                    String[] tokens = value.split("\\s+");
                    for (String token : tokens) {
                        keys.add(token);
                    }
                }
                return keys;
            }
        };
        return standardBlocking;
    }

    public static BlockingKeyGenerator<Event> getFirstLabel() {
        BlockingKeyGenerator<Event> firstLabel = new BlockingKeyGenerator<Event>() {
            @Override
            public String getBlockingKey(Event event) {
                for (String label : event.getLabels()) {
                    return label;
                }
                return null;
            }
        };
        return firstLabel;
    }
}
