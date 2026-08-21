package com.waltsoft.mermaidb

import com.waltsoft.mermaidb.database.Database
import com.waltsoft.mermaidb.extension.Extension
import com.waltsoft.mermaidb.extension.ExtensionValidator
import com.waltsoft.mermaidb.task.CleanDatabaseTask
import com.waltsoft.mermaidb.task.GenerateDatabaseDiagramTask
import com.waltsoft.mermaidb.task.MigrationTask
import com.waltsoft.mermaidb.task.StartDatabaseTask
import com.waltsoft.mermaidb.task.StopDatabaseTask
import com.waltsoft.mermaidb.task.Task
import org.gradle.api.Plugin
import org.gradle.api.Project

class MermaidbPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.logger.lifecycle("--- Mermaidb Plugin Loaded Successfully ---")

        def extension = project.extensions.create('mermaidb', Extension)

        project.afterEvaluate {

            new ExtensionValidator(extension).validate()

            println "🚀 Starting Mermaidb with database: ${extension.dbType}"

            def database = new Database(extension);
            def stopDatabaseTask = new StopDatabaseTask(project, database)
            def cleanDatabaseTask = new CleanDatabaseTask(project, database)
            def startDatabaseTask = new StartDatabaseTask(project,extension,database,cleanDatabaseTask)
            def migrationTask = new MigrationTask(project,extension,startDatabaseTask)
            def generateDatabaseDiagramTask = new GenerateDatabaseDiagramTask(project,extension,migrationTask,stopDatabaseTask)

            List<Task> tasks = [
                    stopDatabaseTask,
                    cleanDatabaseTask,
                    startDatabaseTask,
                    migrationTask,
                    generateDatabaseDiagramTask
            ]

            for (def task : tasks){
                task.register()
            }
        }
    }
}