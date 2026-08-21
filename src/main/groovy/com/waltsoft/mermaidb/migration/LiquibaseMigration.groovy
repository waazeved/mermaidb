package com.waltsoft.mermaidb.migration

import com.waltsoft.mermaidb.extension.Extension
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec

import java.lang.module.Configuration

class LiquibaseMigration implements  Migration{

    @Override
    void applyDependencies(Project project, Configuration configuration, Extension extension) {
        project.getDependencies().add(configuration.getName(), "info.picocli:picocli:4.7.6")
    }

    @Override
    void configure(JavaExec task, Project project, Extension extension, Configuration migrationRuntime) {

        task.mainClass.set('liquibase.integration.commandline.LiquibaseCommandLine')
        def projectClasspath = project.configurations.findByName('runtimeClasspath')
        task.classpath = projectClasspath + migrationRuntime + project.files('src/main/resources')

        def JdbcUrl = String.format(
                extension.dbType.jdbcUrlFormat,
                getDatabasePort(),
                extension.dbType.defaultDbName
        )

        task.args = [
                "--url=${JdbcUrl}",
                "--username=${extension.dbType.defaultUser}",
                "--password=${extension.dbType.defaultPassword}",
                '--searchPath=src/main/resources',
                "--changeLogFile=${extension.changeLogFilePath}",
                'update'
        ]
    }
}
