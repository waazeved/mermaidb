package com.waltsoft.mermaidb.diagram

import java.util.regex.Pattern

class DiagramTableOrderer {

    private static final String ER_DIAGRAM_PREFIX = "erDiagram"
    private static final String COMMENT_PREFIX = "%%"

    private final String rawDiagram

    private final List<String> header = []
    private final List<String> relationships = []
    private final Map<String, List<String>> tables = [:]

    DiagramTableOrderer(String diagram) {
        this.rawDiagram = diagram
    }

    String gravityOrder() {
        parseDiagram()

        Map<String, Integer> tableWeights = calculateTableGravity()
        List<String> sortedHubs = sortHubsByWeight(tableWeights)

        return buildOptimizedDiagram(sortedHubs)
    }

    private void parseDiagram() {
        String currentTable = null

        rawDiagram.readLines().each { line ->
            String trimmed = line.trim()

            if (trimmed.startsWith(ER_DIAGRAM_PREFIX) || trimmed.startsWith(COMMENT_PREFIX)) {
                header.add(line)
            } else if (trimmed.contains("{") && !trimmed.contains("--")) {
                currentTable = trimmed.substring(0, trimmed.indexOf("{")).trim()
                tables[currentTable] = [line]
            } else if (trimmed == "}") {
                if (currentTable) {
                    tables[currentTable].add(line)
                    currentTable = null
                }
            } else if (currentTable) {
                tables[currentTable].add(line)
            } else if (trimmed.contains("--") || trimmed.contains("..")) {
                relationships.add(line)
            }
        }
    }

    private Map<String, Integer> calculateTableGravity() {
        Map<String, Integer> relCount = [:]
        tables.keySet().each { relCount[it] = 0 }

        relationships.each { rel ->
            tables.keySet().each { tableName ->
                if (hasTable(rel, tableName)) {
                    relCount[tableName]++
                }
            }
        }
        return relCount
    }

    private List<String> sortHubsByWeight(Map<String, Integer> relCount) {
        return tables.keySet().sort { a, b -> relCount[b] <=> relCount[a] }
    }

    private String buildOptimizedDiagram(List<String> sortedHubs) {
        List<String> optimizedLines = []
        Set<String> processedTables = []

        optimizedLines.addAll(header)

        sortedHubs.each { hubName ->
            if (processedTables.contains(hubName)) {
                return
            }

            optimizedLines.addAll(tables[hubName])
            optimizedLines.add("") // Linha em branco para clareza visual
            processedTables.add(hubName)

            List<String> satellites = findSatellites(hubName, processedTables)

            satellites.each { childName ->
                optimizedLines.addAll(tables[childName])
                optimizedLines.add("") // Linha em branco
                processedTables.add(childName)
            }
        }

        optimizedLines.addAll(relationships)
        return optimizedLines.join("\n")
    }

    private List<String> findSatellites(String hubName, Set<String> processedTables) {
        List<String> children = []

        relationships.each { rel ->
            if (hasTable(rel, hubName)) {
                tables.keySet().each { childName ->
                    if (childName != hubName && !processedTables.contains(childName) && hasTable(rel, childName)) {
                        children.add(childName)
                    }
                }
            }
        }

        return children.unique()
    }

    private boolean hasTable(String relLine, String tableName) {
        def pattern = Pattern.compile("(?<![a-zA-Z0-9_])" + Pattern.quote(tableName) + "(?![a-zA-Z0-9_])")
        return pattern.matcher(relLine).find()
    }
}