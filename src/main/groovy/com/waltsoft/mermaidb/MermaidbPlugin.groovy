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

        List tasks = buildTasks(project, extension)
        registerTasks(project, tasks, extension)

        project.afterEvaluate {
            new ExtensionValidator(extension).validate()
            println "🚀 Starting Mermaidb with database: ${extension.dbType}"
        }
    }

    void registerTasks(Project project, List<Task> tasks, Extension extension) {
        List<String> pluginTaskNames = []

        for (def task : tasks) {
            task.register()
            pluginTaskNames.add(task.getName())
        }

        configureOnlyIf(project, pluginTaskNames, extension)
    }

    void configureOnlyIf(Project project, List<String> taskNames, Extension extension) {
        taskNames.each { taskName ->
            project.tasks.named(taskName).configure { t ->
                t.onlyIf {
                    return shouldRunPipeline(project, extension)
                }
            }
        }
    }

    List<Task> buildTasks(Project project, Extension extension) {
        def database = new Database(extension)
        return [
                createStopDatabaseTask(project, database),
                createCleanDatabaseTask(project, database),
                createStartDatabaseTask(project, extension, database),
                createMigrationTask(project, extension),
                createGenerateDiagramTask(project, extension)
        ]
    }

    Task createStopDatabaseTask(Project project, Database database) {
        return new StopDatabaseTask(project, database)
    }

    Task createCleanDatabaseTask(Project project, Database database) {
        return new CleanDatabaseTask(project, database)
    }

    Task createStartDatabaseTask(Project project, Extension extension, Database database) {
        return new StartDatabaseTask(project, extension, database)
    }

    Task createMigrationTask(Project project, Extension extension) {
        return new MigrationTask(project, extension)
    }

    Task createGenerateDiagramTask(Project project, Extension extension) {
        return new GenerateDiagramTask(project, extension)
    }

    @Memoized
    boolean shouldRunPipeline(Project project, Extension extension){
        def git = new Git(project, extension)
        shouldRunPipelineByGit(project, git);
    }

    boolean shouldRunPipelineByGit(Project project, Git git) {
        boolean forceGenerate = project.hasProperty("forceGenerate")
                ? project.property("forceGenerate").toString().toBoolean() : false

        if (forceGenerate) {
            return true
        }

        return git.checkIfMigrationsChanged()
    }
}
