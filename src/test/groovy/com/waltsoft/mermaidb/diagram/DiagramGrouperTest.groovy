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
                |sector {
                |  int id
                |}
                |report {
                |  int id
                |}
            """.stripMargin().trim()

            def grouper = new DiagramGrouper(smallDiagram)
            Optional<Map<String, String>> result = grouper.groupByModule()

            assertTrue(result.isEmpty(), "Expected empty result because there is no group with 4+ tables")
        }

        @Test
        @DisplayName("Should return empty when there are less than 5 tables")
        void "Should return empty when there are less than 5 tables"() {

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
                |auth_key {
                |  int id
                |}
            """.stripMargin().trim()

            def grouper = new DiagramGrouper(smallDiagram)
            Optional<Map<String, String>> result = grouper.groupByModule()

            assertTrue(result.isEmpty(), "Expected empty result because there are less than 5 tables")
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
                |pay.a {
                |  int id
                |}
                |pay.b {
                |  int id
                |}
                |pay.c {
                |  int id
                |}
                |pay.d {
                |  int id
                |}
                |config {
                |  int id
                |}
                |auth_a ||--o{ auth_b : has
                |auth_a ||--o{ config : uses
                |auth_a ||--o{ pay.a : pays
            """.stripMargin().trim()

            def grouper = new DiagramGrouper(rawDiagram)
            Optional<Map<String, String>> resultOptional = grouper.groupByModule()

            assertTrue(resultOptional.isPresent(), "Expected diagrams to be generated")
            Map<String, String> diagrams = resultOptional.get()

            assertEquals(3, diagrams.size())
            assertTrue(diagrams.containsKey(MODULE_MAIN))
            assertTrue(diagrams.containsKey(MODULE_AUTH))
            assertTrue(diagrams.containsKey(MODULE_PAY))

            String mainDiagram = diagrams[MODULE_MAIN]

            assertTrue(mainDiagram.contains("config {"), "Main should contain shared kernel tables")
            assertTrue(mainDiagram.contains("\"MODULE: AUTH\" {"), "Main should contain abstract AUTH module block")
            assertTrue(mainDiagram.contains("table auth_a"), "Main abstract AUTH module should list its tables")
            assertFalse(mainDiagram.contains("\"MODULE: AUTH\" ||--o{ \"MODULE: AUTH\""), "Internal relationships should be hidden in main")
            assertTrue(mainDiagram.contains("\"MODULE: AUTH\" ||--o{ config : uses"), "Should map module to shared table")
            assertTrue(mainDiagram.contains("\"MODULE: AUTH\" ||--o{ \"MODULE: PAY\" : pays"), "Should map cross-module relationship")

            String authDiagram = diagrams[MODULE_AUTH]

            assertTrue(authDiagram.contains("auth_a {"), "Sub-diagram should contain its own tables")
            assertTrue(authDiagram.contains("config {"), "Sub-diagram should import used shared tables")
            assertTrue(authDiagram.contains("\"MODULE: PAY\" {"), "Sub-diagram should include external abstract modules")
            assertTrue(authDiagram.contains("auth_a ||--o{ auth_b : has"), "Internal relationships must be preserved in sub-diagram")
            assertTrue(authDiagram.contains("auth_a ||--o{ \"MODULE: PAY\" : pays"), "Cross relationships must point to abstract external modules")

            String payDiagram = diagrams[MODULE_PAY]

            assertTrue(payDiagram.contains("pay.a {"), "Sub-diagram PAY should contain its own tables")
            assertTrue(payDiagram.contains("\"MODULE: AUTH\" {"), "Sub-diagram PAY should include external abstract module AUTH")
            assertTrue(payDiagram.contains("table auth_a"), "Sub-diagram PAY abstract AUTH module should list its tables")
            assertTrue(payDiagram.contains("\"MODULE: AUTH\" ||--o{ pay.a : pays"), "Incoming cross relationships must point from abstract external module to internal table")
            assertFalse(payDiagram.contains("config {"), "Sub-diagram PAY should NOT import config table as it has no direct relationship")
        }
    }
}