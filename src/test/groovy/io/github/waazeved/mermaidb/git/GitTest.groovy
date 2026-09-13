package io.github.waazeved.mermaidb.git

import io.github.waazeved.mermaidb.extension.Extension
import org.gradle.api.Project
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito

@DisplayName("Tests for Git class")
class GitTest {

    private static final String MIGRATION_FILE_PATH = "db/changelog/db.changelog-master.xml"
    private static final String OUTPUT_DIR_PATH = "build/mermaid"

    private Git git
    private Project projectMock
    private Extension extensionMock

    @BeforeEach
    void setUp() {
        projectMock = Mockito.mock(Project.class)
        extensionMock = Mockito.mock(Extension.class)
        Mockito.when(extensionMock.getChangeLogFilePath()).thenReturn(MIGRATION_FILE_PATH)
        Mockito.when(extensionMock.getOutputDirPath()).thenReturn(OUTPUT_DIR_PATH)
        git = new Git(projectMock, extensionMock)
    }

    @Nested
    @DisplayName("Tests for add command")
    class AddCommandTest {

        @Test
        @DisplayName("buildAddCommand should construct the correct git add command")
        void buildAddCommandShouldConstructCorrectCommand() {
            final String expectedCommand = "git add " + OUTPUT_DIR_PATH
            String actualCommand = git.buildAddCommand()
            Assertions.assertEquals(expectedCommand, actualCommand, "The git add command was not built correctly.")
        }

        @Test
        @DisplayName("add method should wait for the process to complete")
        void addShouldWaitForProcess() {
            Process processMock = Mockito.mock(Process.class)
            git.add(processMock)
            Mockito.verify(processMock, Mockito.times(1)).waitFor()
        }
    }

    @Nested
    @DisplayName("Tests for checkIfMigrationsChanged method")
    class CheckIfMigrationsChangedTest {

        @Test
        @DisplayName("buildDiffCommand should construct the correct git diff command")
        void buildDiffCommandShouldConstructCorrectCommand() {
            final String expectedCommand = "git diff --name-only --cached"
            String actualCommand = git.buildDiffCommand()
            Assertions.assertEquals(expectedCommand, actualCommand, "The git diff command was not built correctly.")
        }

        @Test
        @DisplayName("getMigrationDir should return the correct directory from the changelog file path")
        void getMigrationDirShouldReturnCorrectDirectory() {
            final String expectedDir = "src/main/resources/db/changelog"
            String actualDir = git.getMigrationDir()
            Assertions.assertEquals(expectedDir, actualDir, "The migration directory was not extracted correctly.")
        }

        @Test
        @DisplayName("should return true when migration files are staged")
        void shouldReturnTrueWhenMigrationsAreStaged() {
            final String gitDiffOutput = "src/main/resources/db/changelog/new-migration.xml"
            Process processMock = createProcessMock(0, gitDiffOutput)
            boolean result = git.checkIfMigrationsChanged(processMock)
            Assertions.assertTrue(result, "Should return true when migration files are staged.")
        }

        @Test
        @DisplayName("should return false when no migration files are staged")
        void shouldReturnFalseWhenNoMigrationsAreStaged() {
            final String gitDiffOutput = "src/main/java/com/waltsoft/SomeClass.java"
            Process processMock = createProcessMock(0, gitDiffOutput)
            boolean result = git.checkIfMigrationsChanged(processMock)
            Assertions.assertFalse(result, "Should return false when no migration files are staged.")
        }

        @Test
        @DisplayName("should return false when there are no staged files")
        void shouldReturnFalseForNoStagedFiles() {
            final String gitDiffOutput = ""
            Process processMock = createProcessMock(0, gitDiffOutput)
            boolean result = git.checkIfMigrationsChanged(processMock)
            Assertions.assertFalse(result, "Should return false for no staged files.")
        }

        @Test
        @DisplayName("should return true if git diff fails")
        void shouldReturnTrueIfGitDiffFails() {
            final int exitValue = 1
            final String gitDiffOutput = "error: not a git repository"
            Process processMock = createProcessMock(exitValue, gitDiffOutput)
            boolean result = git.checkIfMigrationsChanged(processMock)
            Assertions.assertTrue(result, "Should return true if git diff command fails.")
        }

        private Process createProcessMock(int exitValue, String output) {
            Process processMock = Mockito.mock(Process.class)
            Mockito.when(processMock.exitValue()).thenReturn(exitValue)
            
            InputStream inputStream = new ByteArrayInputStream(output.getBytes())
            InputStream errorStream = new ByteArrayInputStream(new byte[0])
            OutputStream outputStream = new ByteArrayOutputStream()

            Mockito.when(processMock.getInputStream()).thenReturn(inputStream)
            Mockito.when(processMock.getErrorStream()).thenReturn(errorStream)
            Mockito.when(processMock.getOutputStream()).thenReturn(outputStream)

            return processMock
        }
    }
}
