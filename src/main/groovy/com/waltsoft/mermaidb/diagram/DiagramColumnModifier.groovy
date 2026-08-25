package com.waltsoft.mermaidb.diagram

class DiagramColumnModifier {

    private static final String COLUMN_LINE_REGEX = "^\\s{8}\\w+\\s+\\w+.*"
    private static final String WHITESPACE_REGEX = "\\s+"
    private static final String COLUMN_INDENTATION = "        "

    private final String diagram

    DiagramColumnModifier(String diagram) {
        this.diagram = diagram
    }

    String toUppercase() {
        List<String> lines = diagram.readLines()

        List<String> modifiedLines = lines.collect { line ->
            return columnOfLineToUppercase(line)
        }

        return modifiedLines.join("\n")
    }

    private String columnOfLineToUppercase(String line) {
        if (line.matches(COLUMN_LINE_REGEX)) {
            String[] parts = line.trim().split(WHITESPACE_REGEX, 3)

            if (parts.length >= 2) {
                String type = parts[0]
                String columnName = parts[1].toUpperCase()
                String rest = parts.length == 3 ? " " + parts[2] : ""

                return "${COLUMN_INDENTATION}${type} ${columnName}${rest}"
            }
        }

        return line
    }
}