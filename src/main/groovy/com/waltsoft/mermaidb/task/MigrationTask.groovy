package com.waltsoft.mermaidb.task

import com.waltsoft.mermaidb.extension.Extension
import com.waltsoft.mermaidb.migration.LiquibaseMigration
import com.waltsoft.mermaidb.migration.Migration
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.JavaExec

class MigrationTask implements Task {

    private static final String MIGRATION_RUNTIME = "mermaidbMigrationRuntime"
    private static final String TASK_NAME = 'runMigration'

    private final Extension extension
    private final Project project
    private final StartDatabaseTask startDatabaseTask

    MigrationTask(Project project, Extension extension, StartDatabaseTask startDatabaseTask) {
        this.extension = extension
        this.project = project
        this.startDatabaseTask = startDatabaseTask
    }

    @Override
    void register() {

        Migration migration = new LiquibaseMigration()
        Configuration migrationRuntime = project.configurations.maybeCreate(MIGRATION_RUNTIME)
        migration.applyDependencies(project, migrationRuntime, extension)

        project.tasks.register(TASK_NAME, JavaExec) { task ->
            task.dependsOn startDatabaseTask.name()
            println "⚙️ Configuring migration task using: ${migration.getClass().getSimpleName()}"
            migration.configure(task, project, extension, migrationRuntime)
        }
    }

    @Override
    String name() {
        return TASK_NAME
    }
}
