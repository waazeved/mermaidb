package io.github.waazeved.mermaidb.migration

import io.github.waazeved.mermaidb.database.DatabaseType
import io.github.waazeved.mermaidb.docker.Docker
import io.github.waazeved.mermaidb.extension.Extension
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito

@DisplayName("Tests for LiquibaseMigration class")
class LiquibaseMigrationTest {

    private static final String CONFIGURATION_NAME = "migrationRuntime"
    private static final String PICOCLI_DEPENDENCY = "info.picocli:picocli:4.7.7"
    private static final String LIQUIBASE_DEPENDENCY = "org.liquibase:liquibase-core:4.33.0"
    private static final String CHANGELOG_FILE_PATH = "db/changelog/db.changelog-master.xml"
    private static final int DYNAMIC_PORT = 32768

    private LiquibaseMigration liquibaseMigration
    private Docker dockerMock

    @BeforeEach
    void setUp() {
        dockerMock = Mockito.mock(Docker.class)
        liquibaseMigration = new LiquibaseMigration(dockerMock)
    }

    @Nested
    @DisplayName("Tests for applyDependencies method")
    class ApplyDependenciesTest {

        @Test
        @DisplayName("Should add all required dependencies for a given database type")
        void shouldAddAllRequiredDependencies() {

            Project project = ProjectBuilder.builder().build()
            DependencyHandler dependencyHandlerMock = Mockito.mock(DependencyHandler.class)
            project.metaClass.getDependencies = { -> dependencyHandlerMock }
            Configuration configuration = project.getConfigurations().create(CONFIGURATION_NAME)
            Extension extension = new Extension()
            extension.dbType = DatabaseType.POSTGRESQL

            liquibaseMigration.applyDependencies(project, configuration, extension)

            ((ProjectInternal) project).evaluate()

            Mockito.verify(dependencyHandlerMock).add(CONFIGURATION_NAME, PICOCLI_DEPENDENCY)
            Mockito.verify(dependencyHandlerMock).add(CONFIGURATION_NAME, LIQUIBASE_DEPENDENCY)
            Mockito.verify(dependencyHandlerMock).add(CONFIGURATION_NAME, extension.dbType.jdbcDriverDependency)
        }
    }

    @Nested
    @DisplayName("Tests for buildLiquibaseArgs method")
    class BuildLiquibaseArgsTest {

        private Extension extension

        @BeforeEach
        void setUp() {
            extension = new Extension()
            extension.changeLogFilePath = CHANGELOG_FILE_PATH
        }

        @Test
        @DisplayName("Should build correct arguments for a database requiring a dynamic port")
        void shouldBuildCorrectArgsForPostgres() {
            extension.dbType = DatabaseType.POSTGRESQL
            String expectedJdbcUrl = String.format(extension.dbType.jdbcUrlFormat, DYNAMIC_PORT, extension.dbType.defaultDbName)

            List<String> args = liquibaseMigration.buildLiquibaseArgs(extension, DYNAMIC_PORT)

            Assertions.assertTrue(args.contains("--url=${expectedJdbcUrl}"))
            Assertions.assertTrue(args.contains("--username=${extension.dbType.defaultUser}"))
            Assertions.assertTrue(args.contains("--password=${extension.dbType.defaultPassword}"))
            Assertions.assertTrue(args.contains("--changeLogFile=${CHANGELOG_FILE_PATH}"))
            Assertions.assertTrue(args.contains("update"))
        }

        @Test
        @DisplayName("Should build correct arguments for a database not requiring a dynamic port")
        void shouldBuildCorrectArgsForSQLite() {
            extension.dbType = DatabaseType.SQLITE
            final int sqlitePort = 0
            String expectedJdbcUrl = String.format(extension.dbType.jdbcUrlFormat, sqlitePort, extension.dbType.defaultDbName)

            List<String> args = liquibaseMigration.buildLiquibaseArgs(extension, sqlitePort)

            Assertions.assertTrue(args.contains("--url=${expectedJdbcUrl}"))
            Assertions.assertTrue(args.contains("--changeLogFile=${CHANGELOG_FILE_PATH}"))
            Assertions.assertTrue(args.contains("update"))
            Assertions.assertFalse(args.any { it.startsWith("--username") })
            Assertions.assertFalse(args.any { it.startsWith("--password") })
        }
    }
}
