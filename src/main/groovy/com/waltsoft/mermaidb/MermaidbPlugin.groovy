package com.waltsoft.mermaidb

import com.waltsoft.mermaidb.database.Database
import com.waltsoft.mermaidb.extension.Extension
import com.waltsoft.mermaidb.extension.ExtensionValidator
import com.waltsoft.mermaidb.git.Git
import com.waltsoft.mermaidb.task.*
import groovy.transform.Memoized
import org.gradle.api.Plugin
import org.gradle.api.Project

class MermaidbPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.logger.lifecycle("--- Mermaidb Plugin Loaded Successfully ---")

        def extension = project.extensions.create('mermaidb', Extension)

        project.afterEvaluate {
            new ExtensionValidator(extension).validate()
            println "🚀 Starting Mermaidb with database: ${extension.dbType}"
            List tasks = buildTasks(project, extension)
            registerTasks(project, extension, tasks)
        }
    }

    private void registerTasks(Project project, Extension extension, List<Task> tasks) {
        List<String> pluginTaskNames = []

        for (def task : tasks) {
            task.register()
            pluginTaskNames.add(task.getName())
        }

        pluginTaskNames.each { taskName ->
            project.tasks.named(taskName).configure { t ->
                t.onlyIf {
                    return shouldRunPipeline(project, extension)
                }
            }
        }
    }

    private List<Task> buildTasks(Project project, Extension extension) {
        def database = new Database(extension)

        def stopDatabaseTask = new StopDatabaseTask(project, database)
        def cleanDatabaseTask = new CleanDatabaseTask(project, database)
        def startDatabaseTask = new StartDatabaseTask(project, extension, database)
        def migrationTask = new MigrationTask(project, extension)
        def generateDatabaseDiagramTask = new GenerateDiagramTask(project, extension)

        return [
                stopDatabaseTask,
                cleanDatabaseTask,
                startDatabaseTask,
                migrationTask,
                generateDatabaseDiagramTask
        ]
    }

    @Memoized
    private boolean shouldRunPipeline(Project project, Extension extension) {

        boolean forceGenerate = project.hasProperty("forceGenerate")
                ? project.property("forceGenerate").toString().toBoolean() : false

        if (forceGenerate) {
            println "🔄 Force generate is ON. Starting Mermaid diagram generation..."
            return true
        }

        def git = new Git(project, extension)
        boolean changed = git.checkIfMigrationsChanged()

        if (!changed) {
            println "✅ No changes found in the migrations file. Skipping Mermaidb tasks to save time."
            println "💡 (Tip: run with -PforceGenerate=true to force the generation)"
        } else {
            println "🔄 Migrations detected! Starting Mermaid diagram generation..."
        }

        return changed
    }

    /**
     * TODO Colocar no readme que isso pode ser incluido no githook
     * TODO Criar testes unitarios
     * TODO Publicar versão 1.0.0
     */
}