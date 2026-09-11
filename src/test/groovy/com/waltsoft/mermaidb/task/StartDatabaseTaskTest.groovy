package com.waltsoft.mermaidb.task

import com.waltsoft.mermaidb.database.Database
import com.waltsoft.mermaidb.database.DatabaseType
import com.waltsoft.mermaidb.extension.Extension
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskContainer
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class StartDatabaseTaskTest {

    private static final String TASK_NAME = "startDatabase"
    private static final List<String> RUN_COMMAND = ["docker", "run", "test-db"]

    @Mock
    private Project project
    @Mock
    private Extension extension
    @Mock
    private Database database
    @Mock
    private TaskContainer taskContainer
    @Mock
    private Exec execTask
    @Captor
    private ArgumentCaptor<Action<Exec>> actionCaptor
    @Captor
    private ArgumentCaptor<Closure> onlyIfClosureCaptor
    @Captor
    private ArgumentCaptor<Closure> doLastClosureCaptor

    private StartDatabaseTask startDatabaseTask

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this)
        Mockito.when(project.getTasks()).thenReturn(taskContainer)
        startDatabaseTask = new StartDatabaseTask(project, extension, database)
    }

    @Nested
    @DisplayName("Task Registration")
    class Register {
        @Test
        void testRegister_wiresUpMethodsCorrectly() {
            Project testProject = ProjectBuilder.builder().build()
            StartDatabaseTask taskSpy = Mockito.spy(new StartDatabaseTask(testProject, extension, database))

            Mockito.when(database.buildRunCommand()).thenReturn(RUN_COMMAND)
            Mockito.doReturn(true).when(taskSpy).shouldRun()
            Mockito.doNothing().when(taskSpy).onComplete()

            taskSpy.register()

            Exec createdTask = (Exec) testProject.tasks.getByName(TASK_NAME)

            Assertions.assertTrue(createdTask.dependsOn.contains(CleanDatabaseTask.TASK_NAME))
            Assertions.assertEquals(RUN_COMMAND, createdTask.commandLine)

            createdTask.getOnlyIf().isSatisfiedBy(createdTask)
            Mockito.verify(taskSpy).shouldRun()

            def allActions = createdTask.getActions()
            def ourDoLastAction = allActions.get(allActions.size() - 1)
            ourDoLastAction.execute(createdTask)

            Mockito.verify(taskSpy).onComplete()
        }
    }

    @Nested
    @DisplayName("Execution Condition (shouldRun)")
    class ShouldRun {
        @Test
        void testShouldRun_returnsFalse_forSQLite() {
            Mockito.when(extension.getDbType()).thenReturn(DatabaseType.SQLITE)
            boolean result = startDatabaseTask.shouldRun()
            Assertions.assertFalse(result)
        }

        @Test
        void testShouldRun_returnsTrue_forPostgres() {
            Mockito.when(extension.getDbType()).thenReturn(DatabaseType.POSTGRESQL)
            boolean result = startDatabaseTask.shouldRun()
            Assertions.assertTrue(result)
        }
    }

    @Nested
    @DisplayName("Completion Action (onComplete)")
    class OnComplete {
        @Test
        void testOnComplete_waitsForConnection() {
            startDatabaseTask.onComplete()
            Mockito.verify(database).waitForConnection()
        }

        @Test
        void testOnComplete_throwsRuntimeException_onSqlException() {
            Mockito.doThrow(new java.sql.SQLException("Connection failed")).when(database).waitForConnection()

            Assertions.assertThrows(RuntimeException.class, {
                startDatabaseTask.onComplete()
            })
        }
    }

    @Test
    void testGetName() {
        Assertions.assertEquals(TASK_NAME, startDatabaseTask.getName())
    }
}
