package com.waltsoft.mermaidb.diagram

import java.util.regex.Pattern

class DiagramModuleGrouper {

    private static final String SHARED_KERNEL = "SHARED_KERNEL"
    private static final String MAIN_GROUP = "MAIN"
    private static final String MAIN_DIAGRAM_KEY = "main"
    private static final String ER_DIAGRAM_DECLARATION = "erDiagram"
    private static final String ER_DIAGRAM_HEADER = "erDiagram\n"
    private static final String COMMENT_PREFIX = "%%"
    private static final String MODULE_PREFIX_FORMAT = "    \"MODULE: %s\" {\n"
    private static final String TABLE_PREFIX_FORMAT = "        table %s\n"
    private static final String MODULE_CLOSURE = "    }\n"
    private static final String RELATION_DASH = "--"
    private static final String RELATION_DOT = ".."

    private static final String REGEX_SEPARATORS = "[_\\-.]"
    private static final String REGEX_RAW_RELATION = "^\\s*\"?([a-zA-Z0-9_.-]+)\"?\\s+\\S+\\s+\"?([a-zA-Z0-9_.-]+)\"?"
    private static final String REGEX_MODULE = "\"MODULE: ([A-Z0-9_]+)\""
    private static final String REGEX_FULL_RELATION = "^(\\s*)\"?([a-zA-Z0-9_.-]+)\"?(\\s+\\S+\\s+)\"?([a-zA-Z0-9_.-]+)\"?(\\s*:.*)\$"

    private final String rawDiagram
    private Map<String, String> tableBodies = [:]
    private List<String> relationships = []

    DiagramModuleGrouper(String diagram) {
        this.rawDiagram = diagram
    }

    Optional<Map<String, String>> makeDiagramsMappedByModuleName() {
        parseMermaid()

        List<String> allTableNames = new ArrayList<>(tableBodies.keySet())
        if (allTableNames.isEmpty()) {
            return Optional.empty()
        }

        List<String> validGroups = calculateValidGroups(allTableNames)
        if (validGroups.isEmpty()) {
            return Optional.empty()
        }

        Map<String, List<String>> categorizedTables = assignTablesToGroups(allTableNames, validGroups)
        Map<String, String> abstractModulesText = buildAbstractModules(categorizedTables)
        Map<String, String> finalDiagrams = [:]

        String mainDiagram = buildMainDiagram(categorizedTables, abstractModulesText)
        finalDiagrams.put(MAIN_DIAGRAM_KEY, mainDiagram)

        categorizedTables.keySet().each { groupName ->
            if (groupName != SHARED_KERNEL) {
                String subDiagram = buildSubDiagram(groupName, categorizedTables, abstractModulesText)
                finalDiagrams.put(groupName, subDiagram)
            }
        }

        return Optional.of(finalDiagrams)
    }

    private void parseMermaid() {
        boolean insideTable = false
        String currentTableName = ""
        StringBuilder currentBlock = new StringBuilder()

        rawDiagram.eachLine { line ->
            String trimmed = line.trim()

            if (trimmed.isEmpty() || trimmed.startsWith(COMMENT_PREFIX) || trimmed == ER_DIAGRAM_DECLARATION) {
                return
            }

            if (trimmed.contains("{")) {
                insideTable = true
                currentTableName = trimmed.substring(0, trimmed.indexOf("{")).replace("\"", "").trim()
                currentBlock = new StringBuilder(line).append("\n")
            } else if (insideTable) {
                currentBlock.append(line).append("\n")
                if (trimmed.contains("}")) {
                    insideTable = false
                    tableBodies.put(currentTableName, currentBlock.toString())
                }
            } else if (trimmed.contains(RELATION_DASH) || trimmed.contains(RELATION_DOT)) {
                relationships.add(line)
            }
        }
    }

    private List<String> calculateValidGroups(List<String> tables) {
        Map<String, Integer> prefixCounts = [:]

        tables.each { tableName ->
            String[] parts = tableName.split(REGEX_SEPARATORS)
            String currentPrefix = ""

            parts.eachWithIndex { part, index ->
                currentPrefix += (index == 0 ? part : "_" + part)
                prefixCounts[currentPrefix] = prefixCounts.getOrDefault(currentPrefix, 0) + 1
            }
        }

        Map<String, Integer> potentialGroups = prefixCounts.findAll { it.value >= 4 }

        if (potentialGroups.isEmpty()) {
            return []
        }

        double average = potentialGroups.values().sum() / (double) potentialGroups.size()
        double threshold = average * 0.5

        List<String> validGroups = potentialGroups.findAll { entry ->
            entry.value >= threshold
        }.collect { it.key }

        return validGroups.sort { -it.length() }
    }

    private Map<String, List<String>> assignTablesToGroups(List<String> tables, List<String> validGroups) {
        Map<String, List<String>> categorized = [:]
        categorized[SHARED_KERNEL] = []

        tables.each { tableName ->
            String matchedGroup = validGroups.find { group ->
                tableName == group || tableName.startsWith(group + "_") || tableName.startsWith(group + "-")
            }

            if (matchedGroup) {
                categorized.putIfAbsent(matchedGroup, [])
                categorized[matchedGroup].add(tableName)
            } else {
                categorized[SHARED_KERNEL].add(tableName)
            }
        }

        List<String> groupsToDissolve = []

        categorized.each { groupName, groupTables ->
            if (groupName != SHARED_KERNEL && groupTables.size() <= 1) {
                categorized[SHARED_KERNEL].addAll(groupTables)
                groupsToDissolve.add(groupName)
            }
        }

        groupsToDissolve.each { groupName ->
            categorized.remove(groupName)
        }

        return categorized
    }

    private Map<String, String> buildAbstractModules(Map<String, List<String>> categorizedTables) {
        Map<String, String> abstractModules = [:]

        categorizedTables.each { groupName, tables ->
            if (groupName == SHARED_KERNEL) {
                return
            }

            StringBuilder builder = new StringBuilder()
            builder.append(String.format(MODULE_PREFIX_FORMAT, groupName.toUpperCase()))

            tables.sort().each { tableName ->
                builder.append(String.format(TABLE_PREFIX_FORMAT, tableName))
            }
            builder.append(MODULE_CLOSURE)

            abstractModules[groupName] = builder.toString()
        }

        return abstractModules
    }

    private Map<String, String> createTableToGroupMap(Map<String, List<String>> categorizedTables) {
        Map<String, String> map = [:]
        categorizedTables.each { group, tables ->
            tables.each { table ->
                map[table] = group
            }
        }
        return map
    }

    private String buildMainDiagram(Map<String, List<String>> categorizedTables, Map<String, String> abstractModules) {
        StringBuilder builder = new StringBuilder(ER_DIAGRAM_HEADER)
        Map<String, String> tableToGroup = createTableToGroupMap(categorizedTables)

        categorizedTables[SHARED_KERNEL].each { tableName ->
            builder.append(tableBodies[tableName])
        }

        abstractModules.values().each { moduleText ->
            builder.append(moduleText)
        }

        relationships.each { rel ->
            String remapped = rewriteRelationshipLine(rel, tableToGroup, MAIN_GROUP)
            if (remapped != null) {
                builder.append(remapped).append("\n")
            }
        }

        return builder.toString()
    }

    private String buildSubDiagram(String currentGroup, Map<String, List<String>> categorizedTables, Map<String, String> abstractModules) {
        StringBuilder builder = new StringBuilder(ER_DIAGRAM_HEADER)
        Map<String, String> tableToGroup = createTableToGroupMap(categorizedTables)

        Set<String> sharedTablesToInclude = []
        List<String> relationshipsToInclude = []

        relationships.each { rel ->
            def rawMatcher = rel =~ REGEX_RAW_RELATION
            String remapped = rewriteRelationshipLine(rel, tableToGroup, currentGroup)

            if (remapped != null) {
                relationshipsToInclude.add(remapped)

                if (rawMatcher.find()) {
                    String leftRaw = rawMatcher.group(1)
                    String rightRaw = rawMatcher.group(2)

                    if (tableToGroup[leftRaw] == SHARED_KERNEL) {
                        sharedTablesToInclude.add(leftRaw)
                    }
                    if (tableToGroup[rightRaw] == SHARED_KERNEL) {
                        sharedTablesToInclude.add(rightRaw)
                    }
                }
            }
        }

        categorizedTables[currentGroup].each { tableName ->
            builder.append(tableBodies[tableName])
        }

        sharedTablesToInclude.each { tableName ->
            builder.append(tableBodies[tableName])
        }

        Set<String> externalModulesNeeded = []
        relationshipsToInclude.each { rel ->
            def matcher = rel =~ REGEX_MODULE
            while (matcher.find()) {
                externalModulesNeeded.add(matcher.group(1).toLowerCase())
            }
        }

        externalModulesNeeded.each { moduleName ->
            if (abstractModules.containsKey(moduleName)) {
                builder.append(abstractModules[moduleName])
            }
        }

        relationshipsToInclude.each { rel ->
            builder.append(rel).append("\n")
        }

        return builder.toString()
    }

    private String rewriteRelationshipLine(String rawLine, Map<String, String> tableToGroup, String focusGroup) {
        def matcher = rawLine =~ REGEX_FULL_RELATION

        if (!matcher.matches()) {
            return rawLine
        }

        String leftTable = matcher.group(2)
        String rightTable = matcher.group(4)

        String leftGroup = tableToGroup[leftTable] ?: SHARED_KERNEL
        String rightGroup = tableToGroup[rightTable] ?: SHARED_KERNEL

        if (focusGroup == MAIN_GROUP && leftGroup != SHARED_KERNEL && leftGroup == rightGroup) {
            return null
        }

        if (focusGroup != MAIN_GROUP && leftGroup != focusGroup && rightGroup != focusGroup) {
            return null
        }

        String mappedLeft = (leftGroup == SHARED_KERNEL || leftGroup == focusGroup) ? leftTable : "\"MODULE: ${leftGroup.toUpperCase()}\""
        String mappedRight = (rightGroup == SHARED_KERNEL || rightGroup == focusGroup) ? rightTable : "\"MODULE: ${rightGroup.toUpperCase()}\""

        return "${matcher.group(1)}${mappedLeft}${matcher.group(3)}${mappedRight}${matcher.group(5)}"
    }
}