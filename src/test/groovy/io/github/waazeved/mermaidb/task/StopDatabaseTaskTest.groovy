package io.github.waazeved.mermaidb.task

import io.github.waazeved.mermaidb.database.Database
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class StopDatabaseTaskTest {

    private static final String TASK_NAME = "stopDatabase"
    private static final List<String> REMOVE_COMMAND = ["docker", "rm", "-f", "test-container"]

    @Mock
    private Database database

    private Project testProject
    private StopDatabaseTask stopDatabaseTask

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this)
        testProject = ProjectBuilder.builder().build()
        stopDatabaseTask = new StopDatabaseTask(testProject, database)
    }

    @Nested
    @DisplayName("Task Registration")
    class Register {
        @Test
        void testRegister_configuresTaskCorrectly() {
            Mockito.when(database.buildRemoveCommand()).thenReturn(REMOVE_COMMAND)

            stopDatabaseTask.register()

            Exec createdTask = (Exec) testProject.tasks.getByName(TASK_NAME)

            Assertions.assertEquals(REMOVE_COMMAND, createdTask.getCommandLine())
            Assertions.assertTrue(createdTask.isIgnoreExitValue())
        }
    }

    @Test
    void testGetName() {
        Assertions.assertEquals(TASK_NAME, stopDatabaseTask.getName())
    }
}
