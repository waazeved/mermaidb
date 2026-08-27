package com.waltsoft.mermaidb.diagram

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

class DiagramGrouperTest {

    private static final String MODULE_MAIN = "main"
    private static final String MODULE_AUTH = "auth"
    private static final String MODULE_PAY = "pay"

    @Nested
    @DisplayName("Tests for DiagramModuleGrouper.groupByModule method")
    class MakeDiagramsTest {

        @Test
        @DisplayName("Should return empty Optional when diagram has no tables")
        void "should return empty when no tables exist"() {
            String emptyDiagram = "erDiagram\n%% Just a comment\n"

            def grouper = new DiagramGrouper(emptyDiagram)
            Optional<Map<String, String>> result = grouper.groupByModule()

            assertTrue(result.isEmpty(), "Expected empty result for diagram without tables")
        }

        @Test
        @DisplayName("Should return empty Optional when tables do not form valid groups (less than 4)")
        void "should return empty when no valid groups are found"() {

            String smallDiagram = """
                |erDiagram
                |auth_user {
                |  int id
                |}
                |auth_role {
                |  int id
                |}
                |auth_perm {
                |  int id
                |}
            """.stripMargin().trim()

            def grouper = new DiagramGrouper(smallDiagram)
            Optional<Map<String, String>> result = grouper.groupByModule()

            assertTrue(result.isEmpty(), "Expected empty result because there is no group with 4+ tables")
        }

        @Test
        @DisplayName("Should generate main and sub-diagrams correctly with abstract modules and shared kernel")
        void "should group tables and build diagrams correctly"() {

            String rawDiagram = """
                |erDiagram
                |auth_a {
                |  int id
                |}
                |auth_b {
                |  int id
                |}
                |auth_c {
                |  int id
                |}
                |auth_d {
                |  int id
                |}
                |pay_a {
                |  int id
                |}
                |pay_b {
                |  int id
                |}
                |pay_c {
                |  int id
                |}
                |pay_d {
                |  int id
                |}
                |config {
                |  int id
                |}
                |auth_a ||--o{ auth_b : has
                |auth_a ||--o{ config : uses
                |auth_a ||--o{ pay_a : pays
            """.stripMargin().trim()

            def grouper = new DiagramGrouper(rawDiagram)
            Optional<Map<String, String>> resultOptional = grouper.groupByModule()

            assertTrue(resultOptional.isPresent(), "Expected diagrams to be generated")
            Map<String, String> diagrams = resultOptional.get()

            assertEquals(3, diagrams.size())
            assertTrue(diagrams.containsKey(MODULE_MAIN))
            assertTrue(diagrams.containsKey(MODULE_AUTH))
            assertTrue(diagrams.containsKey(MODULE_PAY))

            String mainDiagram = diagrams[MODULE_MAIN].replaceAll("\\s+", "")

            assertTrue(mainDiagram.contains("config{intid}"), "Main should contain shared kernel tables")
            assertTrue(mainDiagram.contains("\"MODULE:AUTH\"{tableauth_a"), "Main should contain abstract AUTH module")
            assertFalse(mainDiagram.contains("\"MODULE:AUTH\"||--o{\"MODULE:AUTH\""), "Internal relationships should be hidden in main")
            assertTrue(mainDiagram.contains("\"MODULE:AUTH\"||--o{config:uses"), "Should map module to shared table")
            assertTrue(mainDiagram.contains("\"MODULE:AUTH\"||--o{\"MODULE:PAY\":pays"), "Should map cross-module relationship")

            String authDiagram = diagrams[MODULE_AUTH].replaceAll("\\s+", "")

            assertTrue(authDiagram.contains("auth_a{intid}"), "Sub-diagram should contain its own tables")
            assertTrue(authDiagram.contains("config{intid}"), "Sub-diagram should import used shared tables")
            assertTrue(authDiagram.contains("\"MODULE:PAY\"{"), "Sub-diagram should include external abstract modules")
            assertTrue(authDiagram.contains("auth_a||--o{auth_b:has"), "Internal relationships must be preserved in sub-diagram")
            assertTrue(authDiagram.contains("auth_a||--o{\"MODULE:PAY\":pays"), "Cross relationships must point to abstract external modules")
        }
    }
}