package com.waltsoft

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.JavaExec

class MermaidbPlugin implements Plugin<Project> {

    private static final String MERMERD_VERSION = "v0.13.0"

    void apply(Project project) {
        project.logger.lifecycle("--- Mermaidb Plugin Loaded Successfully ---")

        def extension = project.extensions.create('mermaidb', MermaidbExtension)

        project.afterEvaluate {
            validateExtension(extension)

            def liquibaseRuntime = project.configurations.maybeCreate('mermaidbLiquibaseRuntime')
            project.dependencies.add(liquibaseRuntime.name, "info.picocli:picocli:4.7.6")

            println "🚀 Starting Mermaidb with database: ${extension.dbType}"

            registerCleanTempDbTask(project)
            registerStopTempDbTask(project)
            registerStartTempDbTask(project, extension)
            registerRunLiquibaseTask(project, extension, liquibaseRuntime)
            registerGenerateMermaidTask(project, extension)
        }
    }

    private void validateExtension(MermaidbExtension extension) {
        if (extension.dbType == null) {
            throw new GradleException("Error (Mermaidb): The 'dbType' field is required.")
        }

        if (extension.dbType != DatabaseType.SQLITE && (extension.dbVersion == null || extension.dbVersion.trim().isEmpty())) {
            throw new GradleException("Error (Mermaidb): The 'dbVersion' field is required for Docker-based databases (e.g., '15', '8.0', 'latest').")
        }

        if (extension.changeLogFilePath == null || extension.changeLogFilePath.trim().isEmpty()) {
            throw new GradleException("Error (Mermaidb): The 'changeLogFilePath' field is required.")
        }
    }

    private List<String> buildRemoveCommand() {
        return ['docker', 'rm', '-f', DatabaseDockerCommandFactory.CONTAINER_NAME]
    }

    private String convertDiagramColumnsToUppercase(String originalText) {
        println "✨ Converting columns to UPPERCASE..."
        def lines = originalText.readLines()
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

    private void registerCleanTempDbTask(Project project) {
        project.tasks.register('cleanTempDb', Exec) {
            commandLine buildRemoveCommand()
            ignoreExitValue = true
        }
    }

    private void registerStopTempDbTask(Project project) {
        project.tasks.register('stopTempDb', Exec) {
            commandLine buildRemoveCommand()
            ignoreExitValue = true
        }
    }

    private void registerStartTempDbTask(Project project, MermaidbExtension extension) {
        project.tasks.register('startTempDb', Exec) {
            dependsOn 'cleanTempDb'

            commandLine DatabaseDockerCommandFactory.buildRunCommand(extension)

            doLast {
                if (extension.dbType != DatabaseType.SQLITE) {
                    println "Waiting for ${extension.dbType} database to start..."
                    sleep(6000)
                }
            }
        }
    }

    private void registerRunLiquibaseTask(Project project, MermaidbExtension extension, def liquibaseRuntime) {
        project.tasks.register('runLiquibase', JavaExec) {
            dependsOn 'startTempDb'
            mainClass = 'liquibase.integration.commandline.LiquibaseCommandLine'

            def projectClasspath = project.configurations.findByName('runtimeClasspath')
            classpath = projectClasspath + liquibaseRuntime + project.files('src/main/resources')

            def dynamicJdbcUrl = String.format(
                    extension.dbType.jdbcUrlFormat,
                    54332,
                    extension.dbType.defaultDbName
            )

            args = [
                    "--url=${dynamicJdbcUrl}",
                    "--username=${extension.dbType.defaultUser}",
                    "--password=${extension.dbType.defaultPassword}",
                    '--searchPath=src/main/resources',
                    "--changeLogFile=${extension.changeLogFilePath}",
                    'update'
            ]
        }
    }

    private void registerGenerateMermaidTask(Project project, MermaidbExtension extension) {
        project.tasks.register('generateMermaid', Exec) {
            group = 'documentation'
            description = 'Generates Mermaid diagram from database schema'
            dependsOn 'runLiquibase'

            def rootDir = project.layout.projectDirectory.asFile.absolutePath
            def diagramFile = project.file(extension.outputFilePath)

            def dynamicMermerdUrl = String.format(
                    extension.dbType.mermerdUrlFormat,
                    extension.dbType.defaultUser,
                    extension.dbType.defaultPassword,
                    extension.dbType.defaultPort,
                    extension.dbType.defaultDbName
            )

            def mermerdCommand = "apk add --no-cache git && " +
                    "go install github.com/KarnerTh/mermerd@${MERMERD_VERSION} && " +
                    "/go/bin/mermerd " +
                    "-c \"${dynamicMermerdUrl}\" " +
                    "--schema public " +
                    "--useAllTables " +
                    "--outputFileName /workspace/${extension.outputFilePath}"

            doFirst {
                if (diagramFile.exists()) {
                    diagramFile.setWritable(true)
                    diagramFile.delete()
                }
            }

            commandLine 'docker', 'run', '--rm',
                    '--link', "${DatabaseDockerCommandFactory.CONTAINER_NAME}:db",
                    '-v', "${rootDir}:/workspace",
                    'golang:alpine',
                    'sh', '-c', mermerdCommand

            finalizedBy 'stopTempDb'

            doLast {

                if (diagramFile.exists()) {
                    def diagramText = diagramFile.text

                    if (extension.uppercaseColumns) {
                        diagramText = convertDiagramColumnsToUppercase(diagramText)
                    }

                    diagramFile.delete()
                    diagramFile.text = diagramText
                    diagramFile.setReadOnly()
                }

                try {
                    "git add ${extension.outputFilePath}".execute()
                } catch (Exception e) {
                    println "⚠️ Git repo not found. Skipping 'git add' for the diagram file."
                }

                println "✅ Diagram generated successfully at '${extension.outputFilePath}'!"
            }
        }
    }
}