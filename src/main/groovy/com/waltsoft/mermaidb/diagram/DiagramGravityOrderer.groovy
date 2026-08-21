package com.waltsoft.mermaidb.diagram

class DiagramGravityOrderer {

    private final String diagram;

    DiagramGravityOrderer(String diagram) {
        this.diagram = diagram
    }

    String order() {
        println "✨ Executing DiagramGravityOrderer (Hub & Spoke Affinity)..."

        def lines = diagram.readLines()
        def header = []
        def relationships = []
        Map<String, List<String>> tables = [:]

        String currentTable = null

        // 1. Parser (Splits the file into blocks: Header, Tables, and Relationships)
        for (String line : lines) {
            def trimmed = line.trim()
            if (trimmed.startsWith("erDiagram") || trimmed.startsWith("%%")) {
                header.add(line)
            } else if (trimmed.contains("{") && !trimmed.contains("--")) {
                currentTable = trimmed.split("\\s+")[0]
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

        // 2. Ranking (Counts connections for each table to determine gravity/weight)
        Map<String, Integer> relCount = [:]
        tables.keySet().each { relCount[it] = 0 }

        relationships.each { rel ->
            tables.keySet().each { tableName ->
                // The \b guarantees an exact word match (prevents 'user' from matching 'user_role')
                if (rel.matches(".*\\b${tableName}\\b.*")) {
                    relCount[tableName] = relCount[tableName] + 1
                }
            }
        }

        // Sorts from the largest Hub to the smallest
        def sortedHubs = tables.keySet().sort { a, b -> relCount[b] <=> relCount[a] }

        // 3. Final Assembly (Purely structural Hub & Spoke grouping algorithm)
        def optimizedLines = []

        // Keeps the original header intact, without injecting visual rules here
        optimizedLines.addAll(header)

        Set<String> processedTables = []

        // Iterates over the sorted Hubs
        sortedHubs.each { hubName ->
            if (processedTables.contains(hubName)) return // Already drawn as a satellite of another hub

            // Writes the Hub ("Parent")
            optimizedLines.addAll(tables[hubName])
            optimizedLines.add("") // Blank line for visual clarity in the file
            processedTables.add(hubName)

            // Finds the Satellites ("Children")
            def children = []
            relationships.each { rel ->
                if (rel.matches(".*\\b${hubName}\\b.*")) {
                    tables.keySet().each { childName ->
                        if (childName != hubName && !processedTables.contains(childName) && rel.matches(".*\\b${childName}\\b.*")) {
                            children.add(childName)
                        }
                    }
                }
            }

            // Adds the satellites attached to their Hub
            children.unique().each { childName ->
                optimizedLines.addAll(tables[childName])
                optimizedLines.add("") // Blank line separating child tables
                processedTables.add(childName)
            }
        }

        // Places relationships at the end of the file
        optimizedLines.addAll(relationships)

        return optimizedLines.join("\n")
    }
}