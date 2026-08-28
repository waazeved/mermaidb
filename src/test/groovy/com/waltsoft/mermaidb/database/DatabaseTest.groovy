package com.waltsoft.mermaidb.database

import com.waltsoft.mermaidb.extension.Extension
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito

@DisplayName("Tests for Database class")
class DatabaseTest {

    private Extension extensionMock

    @BeforeEach
    void setUp() {
        extensionMock = Mockito.mock(Extension.class)
        Mockito.when(extensionMock.getDbVersion()).thenReturn("latest")
    }

    private void setupDatabaseType(DatabaseType dbType) {
        Mockito.when(extensionMock.getDbType()).thenReturn(dbType)
    }

    @Nested
    @DisplayName("Tests for buildRunCommand method")
    class BuildRunCommandTest {

        @Test
        @DisplayName("Should return the correct command for PostgreSQL")
        void shouldReturnCorrectCommandForPostgres() {
            def dbType = DatabaseType.POSTGRESQL
            setupDatabaseType(dbType)
            Mockito.when(extensionMock.getDbVersion()).thenReturn("16")

            Database database = new Database(extensionMock)
            List<String> command = database.buildRunCommand()

            def expectedImage = String.format(dbType.dockerImageFormat, "16")
            def expectedCommand = [
                    'docker', 'run', '--name', Database.DOCKER_CONTAINER_NAME, '-d',
                    '-p', "${dbType.defaultPort}",
                    '-e', "POSTGRES_USER=${dbType.defaultUser}",
                    '-e', "POSTGRES_PASSWORD=${dbType.defaultPassword}",
                    '-e', "POSTGRES_DB=${dbType.defaultDbName}",
                    expectedImage
            ]

            Assertions.assertEquals(expectedCommand, command)
        }

        @Test
        @DisplayName("Should return the correct command for MySQL")
        void shouldReturnCorrectCommandForMySql() {
            def dbType = DatabaseType.MYSQL
            setupDatabaseType(dbType)

            Database database = new Database(extensionMock)
            List<String> command = database.buildRunCommand()

            def expectedImage = String.format(dbType.dockerImageFormat, "latest")
            def expectedCommand = [
                    'docker', 'run', '--name', Database.DOCKER_CONTAINER_NAME, '-d',
                    '-p', "${dbType.defaultPort}",
                    '-e', "MYSQL_ROOT_PASSWORD=${dbType.defaultPassword}",
                    '-e', "MYSQL_DATABASE=${dbType.defaultDbName}",
                    expectedImage
            ]

            Assertions.assertEquals(expectedCommand, command)
        }

        @Test
        @DisplayName("Should return the correct command for SQL Server")
        void shouldReturnCorrectCommandForSqlServer() {
            def dbType = DatabaseType.SQLSERVER
            setupDatabaseType(dbType)

            Database database = new Database(extensionMock)
            List<String> command = database.buildRunCommand()

            def expectedImage = String.format(dbType.dockerImageFormat, "latest")
            def expectedCommand = [
                    'docker', 'run', '--name', Database.DOCKER_CONTAINER_NAME, '-d',
                    '-p', "${dbType.defaultPort}",
                    '-e', 'ACCEPT_EULA=Y',
                    '-e', "SA_PASSWORD=${dbType.defaultPassword}",
                    expectedImage
            ]

            Assertions.assertEquals(expectedCommand, command)
        }

        @Test
        @DisplayName("Should return an empty list for SQLite")
        void shouldReturnEmptyListForSqlite() {
            setupDatabaseType(DatabaseType.SQLITE)

            Database database = new Database(extensionMock)
            List<String> command = database.buildRunCommand()

            Assertions.assertTrue(command.isEmpty(), "Command list should be empty for SQLite")
        }

        @Test
        @DisplayName("Should use custom Docker image when provided")
        void shouldUseCustomDockerImage() {
            def customImage = "my-registry/my-postgres:15-custom"
            setupDatabaseType(DatabaseType.POSTGRESQL)
            Mockito.when(extensionMock.getDbCustomDockerImage()).thenReturn(customImage)

            Database database = new Database(extensionMock)
            List<String> command = database.buildRunCommand()

            Assertions.assertTrue(command.contains(customImage), "Command should use the custom Docker image")
            Assertions.assertFalse(command.contains(String.format(DatabaseType.POSTGRESQL.dockerImageFormat, "latest")),
                    "Command should not use the default Docker image")
        }

        @Test
        @DisplayName("Should return the correct command for MariaDB")
        void shouldReturnCorrectCommandForMariaDb() {
            def dbType = DatabaseType.MARIADB
            setupDatabaseType(dbType)

            Database database = new Database(extensionMock)
            List<String> command = database.buildRunCommand()

            def expectedImage = String.format(dbType.dockerImageFormat, "latest")
            def expectedCommand = [
                    'docker', 'run', '--name', Database.DOCKER_CONTAINER_NAME, '-d',
                    '-p', "${dbType.defaultPort}",
                    '-e', "MYSQL_ROOT_PASSWORD=${dbType.defaultPassword}",
                    '-e', "MYSQL_DATABASE=${dbType.defaultDbName}",
                    expectedImage
            ]

            Assertions.assertEquals(expectedCommand, command)
        }
    }

    @Nested
    @DisplayName("Tests for buildRemoveCommand method")
    class BuildRemoveCommandTest {

        @Test
        @DisplayName("Should return the correct remove command")
        void shouldReturnCorrectRemoveCommand() {
            Database database = new Database(extensionMock)
            List<String> command = database.buildRemoveCommand()

            def expectedCommand = ['docker', 'rm', '-f', Database.DOCKER_CONTAINER_NAME]

            Assertions.assertEquals(expectedCommand, command, "The remove command is incorrect")
        }
    }
}
