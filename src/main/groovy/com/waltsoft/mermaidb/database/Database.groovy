package com.waltsoft.mermaidb.database

import com.waltsoft.mermaidb.docker.Docker
import com.waltsoft.mermaidb.extension.Extension

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class Database {

    public static final String DOCKER_CONTAINER_NAME = 'mermaidb-temp-db'

    private final Extension extension

    Database(Extension extension) {
        this.extension = extension
    }

    List<String> buildRunCommand() {
        def dbType = extension.dbType

        if (dbType == DatabaseType.SQLITE) {
            return []
        }

        List<String> command = [
                'docker', 'run', '--name', DOCKER_CONTAINER_NAME, '-d'
        ]

        command.addAll(['-p', "${dbType.defaultPort}"])

        switch (dbType) {
            case DatabaseType.POSTGRESQL:
            case DatabaseType.ALLOYDB:
            case DatabaseType.COCKROACHDB:
                command.addAll([
                        '-e', "POSTGRES_USER=${dbType.defaultUser}",
                        '-e', "POSTGRES_PASSWORD=${dbType.defaultPassword}",
                        '-e', "POSTGRES_DB=${dbType.defaultDbName}"
                ])
                break
            case DatabaseType.MYSQL:
            case DatabaseType.MARIADB:
            case DatabaseType.TIDB:
                command.addAll([
                        '-e', "MYSQL_ROOT_PASSWORD=${dbType.defaultPassword}",
                        '-e', "MYSQL_DATABASE=${dbType.defaultDbName}"
                ])
                break
            case DatabaseType.SQLSERVER:
                command.addAll([
                        '-e', 'ACCEPT_EULA=Y',
                        '-e', "SA_PASSWORD=${dbType.defaultPassword}"
                ])
                break
        }

        def formattedImage = String.format(dbType.dockerImageFormat, extension.dbVersion)
        command.add(extension.dbCustomDockerImage ?: formattedImage)
        return command
    }

    List<String> buildRemoveCommand() {
        return ['docker', 'rm', '-f', Database.DOCKER_CONTAINER_NAME]
    }

    void waitForConnection() throws SQLException {
        def dbType = extension.dbType

        if (dbType == DatabaseType.SQLITE) {
            return
        }

        int maxRetries = 10
        long retryDelay = 2000
        Exception lastException = null

        def databaseExternalPort = new Docker().getDynamicPort(
                DOCKER_CONTAINER_NAME, dbType.defaultPort as String)

        def url = String.format(
                dbType.jdbcUrlFormat,
                databaseExternalPort,
                dbType.defaultDbName)

        println "Attempting to connect to ${url}..."

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try (Connection connection = DriverManager.getConnection(url, dbType.defaultUser, dbType.defaultPassword)) {
                println "Connection to ${dbType} successful on attempt #${attempt}."
                return
            } catch (Exception e) {
                lastException = e
                if (attempt < maxRetries) {
                    println "Connection attempt #${attempt} failed. Error: ${e.message}"
                    println "Retrying in ${retryDelay / 1000} seconds..."
                    Thread.sleep(retryDelay)
                }
            }
        }

        def logsProcess = "docker logs ${DOCKER_CONTAINER_NAME}".execute()
        logsProcess.waitFor()
        def containerLogs = logsProcess.in.text
        def failureMsg = "Failed to connect to the database: ${dbType} after ${maxRetries} attempts.\n" +
                "Container logs:\n${containerLogs}"
        println failureMsg

        throw new SQLException(failureMsg, lastException)
    }
}
