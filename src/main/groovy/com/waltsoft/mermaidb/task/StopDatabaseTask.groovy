package com.waltsoft.mermaidb.task

import com.waltsoft.mermaidb.database.Database
import org.gradle.api.Project
import org.gradle.api.tasks.Exec

class StopDatabaseTask implements Task {

    public static final String TASK_NAME = "stopDatabase"

    private Project project
    private final Database database

    StopDatabaseTask(Project project, Database database) {
        this.database = database
        this.project = project
    }

    @Override
    void register() {
        project.tasks.register(TASK_NAME, Exec) {
            commandLine database.buildRemoveCommand()
            ignoreExitValue = true
        }
    }

    @Override
    String getName() {
        return TASK_NAME
    }
}
