package com.waltsoft.mermaidb.task

import com.waltsoft.mermaidb.database.Database
import com.waltsoft.mermaidb.database.DatabaseType
import com.waltsoft.mermaidb.extension.Extension
import org.gradle.api.Project
import org.gradle.api.tasks.Exec

class StartDatabaseTask implements Task {

    public static final String TASK_NAME = 'startDatabase'

    private final Extension extension
    private final Project project
    private final Database database

    StartDatabaseTask(Project project, Extension extension, Database database) {
        this.extension = extension
        this.project = project
        this.database = database
    }

    @Override
    void register() {
        project.tasks.register(TASK_NAME, Exec) {
            dependsOn CleanDatabaseTask.TASK_NAME

            onlyIf {
                if (extension.dbType == DatabaseType.SQLITE) {
                    println "SQLite selected. Skipping Docker container setup."
                    return false
                }
                return true
            }

            commandLine database.buildRunCommand()

            doLast {
                if (extension.dbType != DatabaseType.SQLITE) {
                    println "Waiting for ${extension.dbType} database to start..."
                    sleep(6000)
                }
            }
        }
    }

    @Override
    String getName() {
        return TASK_NAME
    }
}
