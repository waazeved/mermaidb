package io.github.waazeved.mermaidb.migration

import io.github.waazeved.mermaidb.database.Database
import io.github.waazeved.mermaidb.database.DatabaseType
import io.github.waazeved.mermaidb.docker.Docker
import io.github.waazeved.mermaidb.extension.Extension
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.JavaExec
import org.gradle.internal.impldep.com.google.common.annotations.VisibleForTesting

class LiquibaseMigration implements Migration {

    private static final String PICOCLI_DEPENDENCY = "info.picocli:picocli:4.7.7"
    private static final String LIQUIBASE_DEPENDENCY = "org.liquibase:liquibase-core:4.33.0"
    private static final String LIQUIBASE_COMMAND_LINE_CLASS = 'liquibase.integration.commandline.LiquibaseCommandLine'
    private static final String RESOURCES_DIR_PATH = 'src/main/resources'

    private final Docker docker

    LiquibaseMigration() {
        this.docker = new Docker()
    }

    @VisibleForTesting
    LiquibaseMigration(Docker docker) {
        this.docker = docker
    }

    @Override
    void applyDependencies(Project project, Configuration configuration, Extension extension) {
        project.dependencies.add(configuration.name, PICOCLI_DEPENDENCY)
        project.dependencies.add(configuration.name, LIQUIBASE_DEPENDENCY)
        
        project.afterEvaluate {
            if (extension.dbType != null) {
                project.dependencies.add(configuration.name, extension.dbType.jdbcDriverDependency)
            }
        }
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
                databaseExternalPort = docker.getDynamicPort(
                        Database.DOCKER_CONTAINER_NAME, extension.dbType.defaultPort.toString())
            }
            task.args = buildLiquibaseArgs(extension, databaseExternalPort)
        }
    }

    @VisibleForTesting
    List<String> buildLiquibaseArgs(Extension extension, Integer databaseExternalPort) {
        String jdbcUrl = String.format(
                extension.dbType.jdbcUrlFormat,
                databaseExternalPort,
                extension.dbType.defaultDbName
        )

        def args = [
                "--url=${jdbcUrl}",
                '--searchPath=src/main/resources',
                "--changeLogFile=${extension.changeLogFilePath}",
                'update'
        ]

        if (extension.dbType != DatabaseType.SQLITE) {
            args.add("--username=${extension.dbType.defaultUser}")
            args.add("--password=${extension.dbType.defaultPassword}")
        }

        return args
    }
}
