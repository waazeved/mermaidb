package com.waltsoft.mermaidb.database

import com.waltsoft.mermaidb.extension.Extension

class Database {

    public static final String DOCKER_CONTAINER_NAME = 'mermaidb-temp-db'

    private final Extension extension

    Database(Extension extension) {
        this.extension = extension
    }

    List<String> buildRunCommand() {
        def dbType = extension.dbType

        if (dbType == DatabaseType.SQLITE) {
            return ['echo', 'SQLite selected. Skipping Docker container.']
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
}