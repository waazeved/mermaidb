package com.waltsoft.mermaidb.migration

import com.waltsoft.mermaidb.database.Database
import com.waltsoft.mermaidb.database.DatabaseType
import com.waltsoft.mermaidb.docker.Docker
import com.waltsoft.mermaidb.extension.Extension
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.JavaExec

class LiquibaseMigration implements Migration {

    private static final String PICOCLI_DEPENDENCY = "info.picocli:picocli:4.7.7"
    private static final String LIQUIBASE_DEPENDENCY = "org.liquibase:liquibase-core:4.33.0"
    private static final String LIQUIBASE_COMMAND_LINE_CLASS = 'liquibase.integration.commandline.LiquibaseCommandLine'
    private static final String RESOURCES_DIR_PATH = 'src/main/resources'

    @Override
    void applyDependencies(Project project, Configuration configuration, Extension extension) {
        project.dependencies.add(configuration.name, PICOCLI_DEPENDENCY)
        project.dependencies.add(configuration.name, LIQUIBASE_DEPENDENCY)
        project.dependencies.add(configuration.name, extension.dbType.jdbcDriverDependency)
    }

    @Override
    void configure(JavaExec task, Project project, Extension extension, Configuration migrationRuntime) {

        task.mainClass.set(LIQUIBASE_COMMAND_LINE_CLASS)

        task.classpath = project.files(
                migrationRuntime,
                RESOURCES_DIR_PATH
        )

        task.doFirst {

            Integer databaseExternalPort = 0

            if (extension.dbType != DatabaseType.SQLITE) {
                databaseExternalPort = new Docker().getDynamicPort(
                        Database.DOCKER_CONTAINER_NAME, extension.dbType.defaultPort.toString())
            }

            def JdbcUrl = String.format(
                    extension.dbType.jdbcUrlFormat,
                    databaseExternalPort,
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
}
