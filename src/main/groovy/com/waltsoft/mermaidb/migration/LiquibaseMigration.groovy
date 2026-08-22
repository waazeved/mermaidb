package com.waltsoft.mermaidb.migration

import com.waltsoft.mermaidb.database.Database
import com.waltsoft.mermaidb.extension.Extension
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.JavaExec

class LiquibaseMigration implements Migration {

    @Override
    void applyDependencies(Project project, Configuration configuration, Extension extension) {
        project.getDependencies().add(configuration.getName(), "info.picocli:picocli:4.7.6")
    }

    @Override
    void configure(JavaExec task, Project project, Extension extension, Configuration migrationRuntime) {

        task.mainClass.set('liquibase.integration.commandline.LiquibaseCommandLine')
        def projectClasspath = project.configurations.named('runtimeClasspath')

        task.classpath = project.files(
                projectClasspath,
                migrationRuntime,
                'src/main/resources'
        )

        task.doFirst {
            Integer databaseExternalPort = getDynamicPort(Database.DOCKER_CONTAINER_NAME, extension.dbType.defaultPort.toString())

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

    private Integer getDynamicPort(String containerName, String internalPort) {
        // Roda o comando 'docker port'
        def process = new ProcessBuilder('docker', 'port', containerName, internalPort).start()
        process.waitFor()

        // Captura o resultado (Ex: "0.0.0.0:32768")
        def output = process.inputStream.text.trim()

        if (output.isEmpty()) {
            throw new RuntimeException("Mermaidb: Failed to retrieve dynamic port for container ${containerName}")
        }

        // Divide a string pelo ':' e pega a porta
        return output.split(':').last().trim().toInteger()
    }
}
