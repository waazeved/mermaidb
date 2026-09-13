package io.github.waazeved.mermaidb.task

import io.github.waazeved.mermaidb.database.DatabaseType
import io.github.waazeved.mermaidb.extension.Extension
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.DependencySet
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskContainer
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class MigrationTaskTest {

    private static final String TASK_NAME = "runMigration"
    private static final String MIGRATION_RUNTIME = "mermaidbMigrationRuntime"

    @Mock
    private Project project
    private Extension extension
    @Mock
    private TaskContainer taskContainer
    @Mock
    private ConfigurationContainer configurationContainer
    @Mock
    private Configuration configuration
    @Mock
    private JavaExec javaExecTask
    @Mock
    private DependencyHandler dependencyHandler
    @Mock
    private DependencySet dependencySet
    @Mock
    private Dependency dependency
    @Mock
    private Property<String> mainClassProperty
    @Mock
    private ConfigurableFileCollection fileCollection
    @Captor
    private ArgumentCaptor<Action<JavaExec>> actionCaptor

    private MigrationTask migrationTask

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this)

        extension = new Extension()
        extension.dbType = DatabaseType.POSTGRESQL

        Mockito.when(project.getTasks()).thenReturn(taskContainer)
        Mockito.when(project.getConfigurations()).thenReturn(configurationContainer)
        Mockito.when(configurationContainer.maybeCreate(MIGRATION_RUNTIME)).thenReturn(configuration)
        Mockito.when(project.getDependencies()).thenReturn(dependencyHandler)
        Mockito.when(dependencyHandler.create(Mockito.anyString())).thenReturn(dependency)
        Mockito.when(configuration.getDependencies()).thenReturn(dependencySet)
        Mockito.when(javaExecTask.getMainClass()).thenReturn(mainClassProperty)

        Mockito.when(project.files(Mockito.any(Object[].class))).thenReturn(fileCollection)

        migrationTask = new MigrationTask(project, extension)
    }

    @Test
    void testRegister() {
        migrationTask.register()

        Mockito.verify(taskContainer).register(
                Mockito.eq(TASK_NAME),
                Mockito.eq(JavaExec.class),
                actionCaptor.capture())

        Action<JavaExec> action = actionCaptor.getValue()
        action.execute(javaExecTask)

        Mockito.verify(javaExecTask).dependsOn(StartDatabaseTask.TASK_NAME)
        Mockito.verify(javaExecTask).setClasspath(fileCollection)
        Mockito.verify(mainClassProperty).set(Mockito.anyString())
    }

    @Test
    void testGetName() {
        Assertions.assertEquals(TASK_NAME, migrationTask.getName())
    }
}
