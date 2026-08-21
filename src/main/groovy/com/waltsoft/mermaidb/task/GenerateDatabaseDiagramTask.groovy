package com.waltsoft.mermaidb.task

import com.waltsoft.mermaidb.diagram.DiagramColumnModifier
import com.waltsoft.mermaidb.diagram.DiagramGenerator
import com.waltsoft.mermaidb.diagram.DiagramGravityOrderer
import com.waltsoft.mermaidb.extension.Extension
import com.waltsoft.mermaidb.git.Git
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.Exec

class GenerateDatabaseDiagramTask implements Task{

    private static final String TASK_NAME = 'generateDatabaseDiagram'
    private static final String FORCE_GENERATE_PROPERTY = "forceGenerate"

    private final Extension extension;
    private final Project project;
    private final MigrationTask migrationTask
    private final StopDatabaseTask stopDatabaseTask;

    GenerateDatabaseDiagramTask(Project project,
                                Extension extension,
                                MigrationTask migrationTask,
                                StopDatabaseTask stopDatabaseTask) {
        this.extension = extension
        this.project = project
        this.migrationTask = migrationTask
        this.stopDatabaseTask = stopDatabaseTask
    }

    @Override
    void register() {
        project.tasks.register(TASK_NAME, Exec) {

            group = 'documentation'
            description = 'Generates Mermaid diagrams from database schema'
            dependsOn migrationTask.name()

            def git = new Git(project, extension)

            onlyIf {

                boolean forceGenerate = false
                if (project.hasProperty(FORCE_GENERATE_PROPERTY)) {
                    forceGenerate = project.property(FORCE_GENERATE_PROPERTY).toString().toBoolean()
                }

                boolean migrationsChanged = git.checkIfMigrationsChanged(project, extension.changeLogFilePath)

                if (!migrationsChanged && !forceGenerate) {
                    println "✅ No changes found in the migrations file. Skipping diagram generation to save time."
                    println "💡 (Tip: run with -PforceGenerate=true to force the generation)"
                    return false
                }

                println "🔄 Starting Mermaid diagram generation..."
                return true
            }

            doFirst {
                def outputDir = project.file(extension.outputDirPath)

                if (outputDir.exists() && outputDir.isDirectory()) {
                    println "🧹 Cleaning up old diagram files in '${extension.outputDirPath}'..."
                    project.delete(project.fileTree(dir: extension.outputDirPath))
                }
            }

            commandLine new DiagramGenerator(project, extension).buildCommand()

            finalizedBy stopDatabaseTask.name()

            doLast {

                def diagramFile = project.file(extension.outputDirPath + "/" + extension.outputFileName)

                if (!diagramFile.exists()) {
                    throw new GradleException(
                            "Mermaidb Error: The diagram file was not generated at '${diagramFile.absolutePath}'. " +
                                    "This usually indicates that the Mermerd CLI or Docker container failed during execution. " +
                                    "Please verify your database connection settings and check the logs above. " +
                                    "Run the task with --info or --debug for more details."
                    )
                }

                def diagramText = diagramFile.text

                if (extension.uppercaseColumns) {
                    diagramText = new DiagramColumnModifier(diagramText).toUppercase()
                }

                diagramText = new DiagramGravityOrderer(diagramText).order()

                diagramFile.delete()
                diagramFile.text = diagramText
                diagramFile.setReadOnly()

                if(extension.autoGitAdd) {
                    git.add()
                }

                println "✅ Diagrams generated successfully at '${extension.outputDirPath}'!"
            }
        }
    }

    @Override
    String name() {
        return TASK_NAME
    }
}
