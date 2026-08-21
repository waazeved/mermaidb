package com.waltsoft.mermaidb.diagram

class DiagramColumnModifier {

    private final String diagram;

    DiagramColumnModifier(String diagram) {
        this.diagram = diagram
    }

    String toUppercase() {
        println "✨ Converting columns to UPPERCASE..."
        def lines = diagram.readLines()
        def modifiedLines = lines.collect { line ->
            if (line.matches("^\\s{8}\\w+\\s+\\w+.*")) {
                def parts = line.trim().split("\\s+", 3)
                if (parts.size() >= 2) {
                    def type = parts[0]
                    def columnName = parts[1].toUpperCase()
                    def rest = parts.size() == 3 ? " " + parts[2] : ""
                    return "        ${type} ${columnName}${rest}"
                }
            }
            return line
        }
        return modifiedLines.join("\n")
    }

}
