package com.waltsoft

class DatabaseDockerCommandFactory {

    public static final String CONTAINER_NAME = 'mermaid-temp-db'

    static List<String> buildRunCommand(MermaidbExtension extension) {
        def dbType = extension.dbType

        if (dbType == DatabaseType.SQLITE) {
            return ['echo', 'SQLite selected. Skipping Docker container.']
        }

        List<String> command = [
                'docker', 'run', '--name', 'mermaid-temp-db', '-d'
        ]

        command.addAll(['-p', "54332:${dbType.defaultPort}"])

        switch (dbType) {
            case DatabaseType.POSTGRESQL:
            case DatabaseType.ALLOYDB_OMNI: // Certifique-se de que o nome bate com o seu Enum
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
}