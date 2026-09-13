package com.waltsoft.mermaidb

import com.waltsoft.mermaidb.extension.Extension
import com.waltsoft.mermaidb.git.Git
import com.waltsoft.mermaidb.task.Task
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class MermaidbPluginTest {

    private MermaidbPlugin plugin

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this)
        plugin = new MermaidbPlugin()
    }

    @Nested
    @DisplayName("buildTasks()")
    class BuildTasksTest {
        @Test
        void testBuildTasks_callsAllCreateMethods() {
            MermaidbPlugin pluginSpy = Mockito.spy(plugin)
            Project mockProject = Mockito.mock(Project.class)
            Extension mockExtension = Mockito.mock(Extension.class)

            pluginSpy.buildTasks(mockProject, mockExtension)

            Mockito.verify(pluginSpy).createStopDatabaseTask(Mockito.any(), Mockito.any())
            Mockito.verify(pluginSpy).createCleanDatabaseTask(Mockito.any(), Mockito.any())
            Mockito.verify(pluginSpy).createStartDatabaseTask(Mockito.any(), Mockito.any(), Mockito.any())
            Mockito.verify(pluginSpy).createMigrationTask(Mockito.any(), Mockito.any())
            Mockito.verify(pluginSpy).createGenerateDiagramTask(Mockito.any(), Mockito.any())
        }
    }

    @Nested
    @DisplayName("registerTasks()")
    class RegisterTasksTest {
        @Mock
        private Extension mockExtension
        private Project realProject

        @BeforeEach
        void setUp() {
            realProject = ProjectBuilder.builder().build()
        }

        @Test
        void testRegisterTasks_appliesOnlyIfPredicateToTasks() {
            MermaidbPlugin pluginSpy = Mockito.spy(plugin)
            Task mockTask1 = Mockito.mock(Task.class)
            Task mockTask2 = Mockito.mock(Task.class)
            List<Task> tasks = [mockTask1, mockTask2]

            Mockito.doReturn("task1").when(mockTask1).getName()
            Mockito.doReturn("task2").when(mockTask2).getName()
            realProject.getTasks().create("task1")
            realProject.getTasks().create("task2")

            Mockito.doReturn(false).when(pluginSpy).shouldRunPipeline(realProject, mockExtension)

            pluginSpy.registerTasks(realProject, tasks, mockExtension)

            def task1 = realProject.getTasks().findByName("task1")
            def task2 = realProject.getTasks().findByName("task2")

            Assertions.assertNotNull(task1)
            Assertions.assertNotNull(task2)
            Assertions.assertFalse(task1.getOnlyIf().isSatisfiedBy(task1))
            Assertions.assertFalse(task2.getOnlyIf().isSatisfiedBy(task2))
        }
    }

    @Nested
    @DisplayName("shouldRunPipeline()")
    class ShouldRunPipelineTest {
        @Mock
        private Project mockProject
        @Mock
        private Git mockGit

        private MermaidbPlugin pluginSpy

        @BeforeEach
        void setUp() {
            MockitoAnnotations.openMocks(this)
            pluginSpy = Mockito.spy(new MermaidbPlugin())
        }

        @Test
        void testShouldRunPipeline_returnsTrue_whenForceGenerateIsTrue() {
            Mockito.doReturn(true).when(mockProject).hasProperty("forceGenerate")
            Mockito.doReturn("true").when(mockProject).property("forceGenerate")

            boolean result = pluginSpy.shouldRunPipelineByGit(mockProject, mockGit)

            assert result
            Mockito.verify(mockGit, Mockito.never()).checkIfMigrationsChanged()
        }

        @Test
        void testShouldRunPipeline_returnsTrue_whenMigrationsChanged() {
            Mockito.doReturn(false).when(mockProject).hasProperty("forceGenerate")
            Mockito.doReturn(true).when(mockGit).checkIfMigrationsChanged()

            boolean result = pluginSpy.shouldRunPipelineByGit(mockProject, mockGit)

            Assertions.assertTrue(result)
        }

        @Test
        void testShouldRunPipeline_returnsFalse_whenMigrationsDidNotChange() {
            Mockito.doReturn(false).when(mockProject).hasProperty("forceGenerate")
            Mockito.doReturn(false).when(mockGit).checkIfMigrationsChanged()

            boolean result = pluginSpy.shouldRunPipelineByGit(mockProject, mockGit)

            Assertions.assertFalse(result)
        }
    }
}
