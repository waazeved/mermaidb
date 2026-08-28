package com.waltsoft.mermaidb.diagram

import com.waltsoft.mermaidb.database.Database
import com.waltsoft.mermaidb.database.DatabaseType
import com.waltsoft.mermaidb.extension.Extension
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.ProjectLayout
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito

@DisplayName("Tests for DiagramGenerator")
class DiagramGeneratorTest {

    private static final String DIAGRAM_FILE_PATH = "build/diagram.md"
    private static final String LINUX_PROJECT_PATH = "/home/user/project"
    private static final String WINDOWS_PROJECT_PATH = "C:\\Users\\user\\project"
    private static final String MERMERD_VERSION = "v0.13.0"

    private Extension extensionMock
    private Project projectMock
    private Directory directoryMock

    @BeforeEach
    void setUp() {
        extensionMock = Mockito.mock(Extension.class)
        projectMock = Mockito.mock(Project.class)
        ProjectLayout layoutMock = Mockito.mock(ProjectLayout.class)
        directoryMock = Mockito.mock(Directory.class)

        Mockito.when(projectMock.getLayout()).thenReturn(layoutMock)
        Mockito.when(layoutMock.getProjectDirectory()).thenReturn(directoryMock)
    }

    private void setupProjectDirectory(String path) {
        File fileMock = Mockito.mock(File.class)
        Mockito.when(fileMock.getAbsolutePath()).thenReturn(path)
        Mockito.when(directoryMock.getAsFile()).thenReturn(fileMock)
    }

    private void setupDatabaseType(DatabaseType dbType) {
        Mockito.when(extensionMock.getDbType()).thenReturn(dbType)
    }

    private List<String> buildExpectedCommand(String projectPath, String diagramPath, DatabaseType dbType) {
        def dbUrl = String.format(
                dbType.mermerdUrlFormat,
                dbType.defaultUser,
                dbType.defaultPassword,
                dbType.defaultPort,
                dbType.defaultDbName
        )

        def mermerdCommand = "echo '⏳ Compiling Mermerd (This may take a few minutes)...' && " +
                "apk add --no-cache git && " +
                "go install github.com/KarnerTh/mermerd@${MERMERD_VERSION} && " +
                "/go/bin/mermerd " +
                "-c \"${dbUrl}\" " +
                "--schema public " +
                "--useAllTables " +
                "--outputFileName /workspace/${diagramPath}"

        return ['docker', 'run', '--rm',
                '--link', "${Database.DOCKER_CONTAINER_NAME}:db",
                '-v', "${projectPath}:${"/workspace"}",
                'golang:alpine',
                'sh', '-c', mermerdCommand]
    }

    @Nested
    @DisplayName("Tests for buildCommand method")
    class BuildCommandTest {

        @Test
        @DisplayName("Should generate command correctly")
        void shouldGenerateCommandForLinuxPath() {
            setupProjectDirectory(LINUX_PROJECT_PATH)
            def dbType = DatabaseType.POSTGRESQL
            setupDatabaseType(dbType)

            DiagramGenerator generator = new DiagramGenerator(projectMock, extensionMock)
            List<String> command = generator.buildCommand(DIAGRAM_FILE_PATH)
            def expectedCommand = buildExpectedCommand(LINUX_PROJECT_PATH, DIAGRAM_FILE_PATH, dbType)

            Assertions.assertEquals(expectedCommand, command, "The generated command is incorrect")
        }

        @Test
        @DisplayName("Should sanitize and generate command correctly for a Windows path")
        void shouldGenerateCommandForWindowsPath() {
            setupProjectDirectory(WINDOWS_PROJECT_PATH)
            def dbType = DatabaseType.POSTGRESQL
            setupDatabaseType(dbType)

            DiagramGenerator generator = new DiagramGenerator(projectMock, extensionMock)
            List<String> command = generator.buildCommand(DIAGRAM_FILE_PATH)

            String sanitizedPath = WINDOWS_PROJECT_PATH.replace("\\", "/")
            def expectedCommand = buildExpectedCommand(sanitizedPath, DIAGRAM_FILE_PATH, dbType)

            Assertions.assertEquals(expectedCommand, command, "The Windows path was not correctly sanitized")
        }

        @Test
        @DisplayName("Should format the database URL correctly for MySQL")
        void shouldFormatDatabaseUrlForMySql() {
            setupProjectDirectory(LINUX_PROJECT_PATH)
            def dbType = DatabaseType.MYSQL
            setupDatabaseType(dbType)

            DiagramGenerator generator = new DiagramGenerator(projectMock, extensionMock)
            List<String> command = generator.buildCommand(DIAGRAM_FILE_PATH)
            def expectedCommand = buildExpectedCommand(LINUX_PROJECT_PATH, DIAGRAM_FILE_PATH, dbType)

            Assertions.assertEquals(expectedCommand, command, "The MySQL connection URL is incorrect")
        }

        @Test
        @DisplayName("Should format the database URL correctly for SQL Server")
        void shouldFormatDatabaseUrlForSqlServer() {
            setupProjectDirectory(LINUX_PROJECT_PATH)
            def dbType = DatabaseType.SQLSERVER
            setupDatabaseType(dbType)

            DiagramGenerator generator = new DiagramGenerator(projectMock, extensionMock)
            List<String> command = generator.buildCommand(DIAGRAM_FILE_PATH)
            def expectedCommand = buildExpectedCommand(LINUX_PROJECT_PATH, DIAGRAM_FILE_PATH, dbType)

            Assertions.assertEquals(expectedCommand, command, "The SQL Server connection URL is incorrect")
        }

        @Test
        @DisplayName("Should handle an empty diagram file path")
        void shouldHandleEmptyDiagramPath() {
            setupProjectDirectory(LINUX_PROJECT_PATH)
            def dbType = DatabaseType.POSTGRESQL
            setupDatabaseType(dbType)

            DiagramGenerator generator = new DiagramGenerator(projectMock, extensionMock)
            List<String> command = generator.buildCommand("")
            def expectedCommand = buildExpectedCommand(LINUX_PROJECT_PATH, "", dbType)

            Assertions.assertEquals(expectedCommand, command, "The command for an empty path is incorrect")
        }
    }
}
