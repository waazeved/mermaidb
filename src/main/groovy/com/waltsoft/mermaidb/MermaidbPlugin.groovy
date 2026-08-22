package com.waltsoft.mermaidb

import com.waltsoft.mermaidb.database.Database
import com.waltsoft.mermaidb.extension.Extension
import com.waltsoft.mermaidb.extension.ExtensionValidator
import com.waltsoft.mermaidb.task.*
import org.gradle.api.Plugin
import org.gradle.api.Project

class MermaidbPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.logger.lifecycle("--- Mermaidb Plugin Loaded Successfully ---")

        def extension = project.extensions.create('mermaidb', Extension)

        project.afterEvaluate {

            new ExtensionValidator(extension).validate()

            println "🚀 Starting Mermaidb with database: ${extension.dbType}"

            /**
             * // 1. Centralizamos o Provider aqui no topo da cadeia
             def shouldRunProvider = project.provider {
             boolean force = project.hasProperty(FORCE_GENERATE_PROPERTY) &&
             project.property(FORCE_GENERATE_PROPERTY).toString().toBoolean()
             return force || new Git(project, extension).checkIfMigrationsChanged()
             }

             // 2. Injetamos o Provider no construtor de cada task
             def startDbTask = new StartDatabaseTask(project, extension, shouldRunProvider)
             startDbTask.register()

             def migrationTask = new MigrationTask(project, extension, startDbTask, shouldRunProvider)
             migrationTask.register()
             */

            /**
             * class StartDatabaseTask implements Task {

             private final Project project
             private final Extension extension
             private final Provider<Boolean> shouldRunProvider // Injetado!

             StartDatabaseTask(Project project, Extension extension, Provider<Boolean> shouldRunProvider) {
             this.project = project
             this.extension = extension
             this.shouldRunProvider = shouldRunProvider
             }

             @Override
              void register() {
              project.tasks.register('startDatabase', Exec) {

              // A task consome a regra injetada de forma elegantíssima:
              onlyIf { shouldRunProvider.get() }

              // ... resto do seu código Docker ...
              }
              }}
             */

            def database = new Database(extension)
            def stopDatabaseTask = new StopDatabaseTask(project, database)
            def cleanDatabaseTask = new CleanDatabaseTask(project, database)
            def startDatabaseTask = new StartDatabaseTask(project, extension, database, cleanDatabaseTask)
            def migrationTask = new MigrationTask(project, extension, startDatabaseTask)
            def generateDatabaseDiagramTask = new GenerateDatabaseDiagramTask(project, extension, migrationTask, stopDatabaseTask)

            List<Task> tasks = [
                    stopDatabaseTask,
                    cleanDatabaseTask,
                    startDatabaseTask,
                    migrationTask,
                    generateDatabaseDiagramTask
            ]

            for (def task : tasks) {
                task.register()
            }
        }
    }
}