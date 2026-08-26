package com.waltsoft.mermaidb.diagram

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import static org.junit.jupiter.api.Assertions.assertEquals

class DiagramTableOrdererTest {

    @Nested
    @DisplayName("Tests for DiagramTableOrderer.gravityOrder method")
    class GravityOrderTest {

        // 1. Success Scenarios (Hubs and Satellites ordering)
        // B and C are disconnected from each other, but both connect to A.
        // A is the "Hub" (Gravity = 2), so it should be moved to the top along with its satellites.
        private static final String BASIC_IN = "erDiagram\n%% comment\nC {\n  int id\n}\nB {\n  int id\n}\nA {\n  int id\n}\nA ||--o{ B : fk\nA ||--o{ C : fk"
        private static final String BASIC_OUT = "erDiagram\n%% comment\nA {\n  int id\n}\n\nB {\n  int id\n}\n\nC {\n  int id\n}\n\nA ||--o{ B : fk\nA ||--o{ C : fk"

        // 2. Regex Edge Case (Substring matching prevention)
        // 'user_role' and 'role' have gravity 1. 'user' has gravity 0.
        // The regex must prevent 'user' from falsely incrementing its gravity from 'user_role' relations.
        private static final String REGEX_IN = "erDiagram\nuser {\n}\nuser_role {\n}\nrole {\n}\nuser_role ||--o{ role : r"
        private static final String REGEX_OUT = "erDiagram\nuser_role {\n}\n\nrole {\n}\n\nuser {\n}\n\nuser_role ||--o{ role : r"

        // 3. Independence Edge Case (No relationships)
        // Should maintain the original order and append the visual spacing (\n).
        private static final String NO_REL_IN = "erDiagram\nA {\n}\nB {\n}"
        private static final String NO_REL_OUT = "erDiagram\nA {\n}\n\nB {\n}\n"

        // 4. Edge Cases
        private static final String EDGE_EMPTY_IN = ""
        private static final String EDGE_EMPTY_OUT = ""

        @ParameterizedTest(name = "[{index}] Should optimize diagram layout correctly")
        @MethodSource("provideTestCases")
        void "should order tables by gravity correctly"(String input, String expected) {
            def orderer = new DiagramTableOrderer(input)
            def result = orderer.gravityOrder()
            assertEquals(expected, result)
        }

        private static Stream<Arguments> provideTestCases() {
            return Stream.of(
                    Arguments.of(BASIC_IN, BASIC_OUT),
                    Arguments.of(REGEX_IN, REGEX_OUT),
                    Arguments.of(NO_REL_IN, NO_REL_OUT),
                    Arguments.of(EDGE_EMPTY_IN, EDGE_EMPTY_OUT)
            )
        }
    }
}