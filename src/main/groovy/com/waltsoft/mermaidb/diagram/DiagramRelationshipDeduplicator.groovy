package com.waltsoft.mermaidb.diagram

class DiagramRelationshipDeduplicator {

    private final String rawDiagram

    DiagramRelationshipDeduplicator(String diagram) {
        this.rawDiagram = diagram
    }

    String deduplicate() {
        List<String> lines = rawDiagram.readLines()
        Set<String> seenRelationships = new HashSet<>()
        List<String> processedLines = []

        def relationshipRegex = ~/^\s*\w+\s+.*(--|\.\.).*/

        lines.each { line ->
            if (line =~ relationshipRegex) {
                if (seenRelationships.add(line.trim())) {
                    processedLines.add(line)
                }
            } else {
                processedLines.add(line)
            }
        }

        return processedLines.join("\n")
    }
}
