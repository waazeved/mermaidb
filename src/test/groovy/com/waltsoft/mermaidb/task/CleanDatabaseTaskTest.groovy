package com.waltsoft.mermaidb.task

import com.waltsoft.mermaidb.database.Database
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskContainer
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class CleanDatabaseTaskTest {

    private static final String TASK_NAME = "cleanDatabase"
    private static final List<String> REMOVE_COMMAND = ["docker", "rm", "-f", "test-container"]

    @Mock
    private Project project
    @Mock
    private Database database
    @Mock
    private TaskContainer taskContainer
    @Mock
    private Exec execTask
    @Captor
    private ArgumentCaptor<Action<Exec>> actionCaptor

    private CleanDatabaseTask cleanDatabaseTask

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this)
        Mockito.when(project.getTasks()).thenReturn(taskContainer)
        cleanDatabaseTask = new CleanDatabaseTask(project, database)
    }

    @Test
    void testRegister() {
        Mockito.when(database.buildRemoveCommand()).thenReturn(REMOVE_COMMAND)

        cleanDatabaseTask.register()

        Mockito.verify(taskContainer).register(
                Mockito.eq(TASK_NAME),
                Mockito.eq(Exec.class),
                actionCaptor.capture())

        Action<Exec> action = actionCaptor.getValue()
        action.execute(execTask)

        Mockito.verify(execTask).commandLine(REMOVE_COMMAND)
        Mockito.verify(execTask).ignoreExitValue = true
    }

    @Test
    void testGetName() {
        String taskName = cleanDatabaseTask.getName()
        Assertions.assertEquals(TASK_NAME, taskName)
    }
}
