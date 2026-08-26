package com.waltsoft.mermaidb.task

import com.waltsoft.mermaidb.diagram.DiagramColumnModifier
import com.waltsoft.mermaidb.diagram.DiagramGenerator
import com.waltsoft.mermaidb.diagram.DiagramModuleGrouper
import com.waltsoft.mermaidb.diagram.DiagramRelationshipDeduplicator
import com.waltsoft.mermaidb.diagram.DiagramTableOrderer
import com.waltsoft.mermaidb.extension.Extension
import com.waltsoft.mermaidb.git.Git
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.Exec

class GenerateDiagramTask implements Task {

    public static final String TASK_NAME = 'generateDatabaseDiagram'
    public static final String DIAGRAM_FILE_NAME = "database-diagram.mmd"

    private final Extension extension
    private final Project project

    GenerateDiagramTask(Project project,
                        Extension extension) {
        this.extension = extension
        this.project = project
    }

    @Override
    void register() {
        project.tasks.register(TASK_NAME, Exec) {

            group = 'mermaidb'
            description = 'Generates Mermaid diagrams from database schema'
            dependsOn MigrationTask.TASK_NAME

            doFirst {
                prepareOutputDir()
            }

            String diagramFilePath = "${extension.outputDirPath}/${DIAGRAM_FILE_NAME}"

            commandLine new DiagramGenerator(project, extension).buildCommand(diagramFilePath)

            finalizedBy StopDatabaseTask.TASK_NAME

            doLast {
                processGeneratedDiagram(diagramFilePath)
            }
        }
    }

    private void prepareOutputDir() {

        def outputDir = project.file(extension.outputDirPath)

        if (outputDir.exists() && outputDir.isDirectory()) {
            println "🧹 Cleaning up old diagram files in '${extension.outputDirPath}'..."
            project.delete(project.fileTree(dir: extension.outputDirPath))
        }

        outputDir.mkdirs()
    }

    private void processGeneratedDiagram(String diagramFilePath) {

        def diagramFile = project.file(diagramFilePath)

        if (!diagramFile.exists()) {
            throw new GradleException(
                    "Mermaidb Error: The diagram file was not generated at '${diagramFile.absolutePath}'. " +
                            "This usually indicates that the Mermerd CLI or Docker container failed during execution. " +
                            "Please verify your database connection settings and check the logs above. " +
                            "Run the task with --info or --debug for more details."
            )
        }

        def diagramText = diagramFile.text

        diagramText = new DiagramRelationshipDeduplicator(diagramText).deduplicate();

        if (extension.uppercaseColumns) {
            diagramText = new DiagramColumnModifier(diagramText).toUppercase()
        }

        Optional<Map<String, String>> diagramsOptional = new DiagramModuleGrouper(diagramText).makeDiagramsMappedByModuleName()

        if (diagramsOptional.isEmpty()) {

            diagramText = new DiagramTableOrderer(diagramText).gravityOrder()
            diagramFile.delete()
            diagramFile.text = diagramText
            diagramFile.setReadOnly()

        } else {

            diagramFile.delete()
            String moduleDirPath = "${extension.outputDirPath}/modules/"
            File moduleDir = project.file(moduleDirPath)
            moduleDir.mkdirs()

            diagramsOptional.get().each { groupName, groupDiagramText ->

                String newFilePath = (groupName == "main")
                        ? "${extension.outputDirPath}/${DIAGRAM_FILE_NAME}"
                        : "${moduleDirPath}/${groupName}_${DIAGRAM_FILE_NAME}"

                File newDiagramFile = project.file(newFilePath)
                String orderedText = new DiagramTableOrderer(groupDiagramText).gravityOrder()
                newDiagramFile.text = orderedText
                newDiagramFile.setReadOnly()
            }
        }

        if (extension.autoGitAdd) {
            def git = new Git(project, extension)
            git.add()
        }

        println "✅ Diagrams generated successfully at '${extension.outputDirPath}'!"
    }

    @Override
    String getName() {
        return TASK_NAME
    }
}
