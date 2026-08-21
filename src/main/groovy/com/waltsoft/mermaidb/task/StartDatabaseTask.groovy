package com.waltsoft.mermaidb.task

import com.waltsoft.mermaidb.database.Database
import com.waltsoft.mermaidb.database.DatabaseType
import com.waltsoft.mermaidb.extension.Extension
import org.gradle.api.Project
import org.gradle.api.tasks.Exec

class StartDatabaseTask implements  Task {

    private static final String TASK_NAME = 'startDatabase'

    private final Extension extension;
    private final Project project;
    private final Database database;
    private final CleanDatabaseTask cleanDatabaseTask;

    StartDatabaseTask(Project project, Extension extension, Database database, CleanDatabaseTask cleanDatabaseTask) {
        this.extension = extension
        this.project = project
        this.database = database
        this.cleanDatabaseTask = cleanDatabaseTask
    }

    @Override
    void register() {
        project.tasks.register(TASK_NAME, Exec) {
            dependsOn cleanDatabaseTask.name()

            commandLine database.buildRunCommand(extension)

            doLast {
                if (extension.dbType != DatabaseType.SQLITE) {
                    println "Waiting for ${extension.dbType} database to start..."
                    sleep(6000)
                }
            }
        }
    }

    @Override
    String name() {
        return TASK_NAME
    }
}
