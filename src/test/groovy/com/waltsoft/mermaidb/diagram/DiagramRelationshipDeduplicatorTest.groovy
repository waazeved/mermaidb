package com.waltsoft.mermaidb.diagram

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import static org.junit.jupiter.api.Assertions.assertEquals

class DiagramRelationshipDeduplicatorTest {

    @Nested
    @DisplayName("Tests for deduplicate method")
    class DeduplicateTest {

        private static final String DUPLICATED_RELATION_IN  = "A }o--|| B : \"id\"\nA }o--|| B : \"id\""
        private static final String DUPLICATED_RELATION_OUT = "A }o--|| B : \"id\""

        private static final String MIXED_DIAGRAM_IN        = "table {\n  int id\n}\ntable }o--|| other : \"fk\"\ntable }o--|| other : \"fk\""
        private static final String MIXED_DIAGRAM_OUT       = "table {\n  int id\n}\ntable }o--|| other : \"fk\""

        private static final String NO_DUPLICATES           = "A }o--|| B : \"id\"\nA }o--|| C : \"id\""
        private static final String NO_RELATIONS            = "table {\n  int id\n}"

        private static final String EDGE_EMPTY              = ""

        @ParameterizedTest(name = "[{index}] Input: ''{0}'' => Expected: ''{1}''")
        @MethodSource("provideTestCases")
        void "should deduplicate diagram relationships correctly"(String input, String expected) {
            def deduplicator = new DiagramRelationshipDeduplicator(input)
            def result = deduplicator.deduplicate()
            assertEquals(expected, result)
        }

        private static Stream<Arguments> provideTestCases() {
            return Stream.of(

                    Arguments.of(DUPLICATED_RELATION_IN, DUPLICATED_RELATION_OUT),
                    Arguments.of(MIXED_DIAGRAM_IN,       MIXED_DIAGRAM_OUT),

                    Arguments.of(NO_DUPLICATES, NO_DUPLICATES),
                    Arguments.of(NO_RELATIONS,  NO_RELATIONS),

                    Arguments.of(EDGE_EMPTY, EDGE_EMPTY)
            )
        }
    }
}