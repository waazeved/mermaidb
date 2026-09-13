package io.github.waazeved.mermaidb.database

import io.github.waazeved.mermaidb.extension.Extension
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.Mockito

@DisplayName("Database Integration Tests")
@EnabledIfEnvironmentVariable(named = "RUN_DATABASE_INTEGRATION_TESTS", matches = "true")
class DatabaseIntegrationTest {

    private Extension extensionMock

    private static final Map<DatabaseType, String> DB_VERSIONS = [
            (DatabaseType.POSTGRESQL): "16",
            (DatabaseType.ALLOYDB): "15",
            (DatabaseType.MYSQL): "8.0",
            (DatabaseType.SQLSERVER): "2022-latest",
            (DatabaseType.MARIADB): "10.11",
            (DatabaseType.COCKROACHDB): "v23.1.0",
            (DatabaseType.TIDB): "v7.5.0"
    ]

    @BeforeEach
    void setUp() {
        extensionMock = Mockito.mock(Extension.class)
    }

    private void setupDatabaseType(DatabaseType dbType) {
        Mockito.when(extensionMock.getDbType()).thenReturn(dbType)
        Mockito.when(extensionMock.getDbVersion()).thenReturn(DB_VERSIONS[dbType] ?: "latest")
    }

    @ParameterizedTest
    @EnumSource(value = DatabaseType.class, names = "SQLITE", mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("Should start the database and allow connection")
    void shouldStartDatabaseAndAllowConnection(DatabaseType dbType) {
        setupDatabaseType(dbType)
        Database database = new Database(extensionMock)
        List<String> command = database.buildRunCommand()

        def process = command.execute()
        def stdout = new StringWriter()
        def stderr = new StringWriter()
        process.waitForProcessOutput(stdout, stderr)

        if (process.exitValue() != 0) {
            def errorMsg = "Failed to start Docker container for ${dbType}. Exit code: ${process.exitValue()}\n" +
                    "Stderr: ${stderr.toString()}"
            println errorMsg
            database.buildRemoveCommand().execute().waitFor()
            Assertions.fail(errorMsg)
        }

        println "Container for ${dbType} started successfully. Container ID: ${stdout.toString().trim()}"

        try {
            database.waitForConnection()
        } catch (Exception e) {
            Assertions.fail("Database connection failed for ${dbType}", e)
        }
        finally {
            database.buildRemoveCommand().execute().waitFor()
        }
    }
}
