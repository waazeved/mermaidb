package com.waltsoft.mermaidb.task

import com.waltsoft.mermaidb.extension.Extension
import com.waltsoft.mermaidb.migration.LiquibaseMigration
import com.waltsoft.mermaidb.migration.Migration
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.JavaExec

class MigrationTask implements Task {

    private static final String MIGRATION_RUNTIME = "mermaidbMigrationRuntime"
    public static final String TASK_NAME = 'runMigration'

    private final Extension extension
    private final Project project

    MigrationTask(Project project, Extension extension) {
        this.extension = extension
        this.project = project
    }

    @Override
    void register() {

        Migration migration = new LiquibaseMigration()
        Configuration migrationRuntime = project.configurations.maybeCreate(MIGRATION_RUNTIME)
        migration.applyDependencies(project, migrationRuntime, extension)

        project.tasks.register(TASK_NAME, JavaExec) { task ->
            task.dependsOn StartDatabaseTask.TASK_NAME
            println "⚙️ Configuring migration task using: ${migration.getClass().getSimpleName()}"
            migration.configure(task, project, extension, migrationRuntime)
        }
    }

    @Override
    String getName() {
        return TASK_NAME
    }
}
