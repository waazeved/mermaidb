package com.waltsoft.mermaidb

import com.waltsoft.mermaidb.extension.Extension
import com.waltsoft.mermaidb.git.Git
import com.waltsoft.mermaidb.task.Task
import org.gradle.api.Project
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
        @Test
        void testRegisterTasks_callsRegisterOnEachTask() {
            MermaidbPlugin pluginSpy = Mockito.spy(plugin)
            Project mockProject = Mockito.mock(Project.class)
            Task mockTask1 = Mockito.mock(Task.class)
            Task mockTask2 = Mockito.mock(Task.class)
            List<Task> tasks = [mockTask1, mockTask2]

            Mockito.doNothing().when(pluginSpy).configureOnlyIf(Mockito.any(), Mockito.any())

            pluginSpy.registerTasks(mockProject, tasks)

            Mockito.verify(mockTask1).register()
            Mockito.verify(mockTask2).register()
        }
    }

    @Nested
    @DisplayName("shouldRunPipeline()")
    class ShouldRunPipelineTest {
        @Mock
        private Project mockProject
        @Mock
        private Extension mockExtension
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

            Mockito.when(mockProject.hasProperty("forceGenerate")).thenReturn(true)
            Mockito.when(mockProject.property("forceGenerate")).thenReturn("true")

            boolean result = pluginSpy.shouldRunPipelineByGit(mockProject, mockGit)

            assert result
            Mockito.verify(mockGit, Mockito.never()).checkIfMigrationsChanged()
        }

        @Test
        void testShouldRunPipeline_returnsTrue_whenMigrationsChanged() {

            Mockito.when(mockProject.hasProperty("forceGenerate")).thenReturn(false)
            Mockito.when(mockGit.checkIfMigrationsChanged()).thenReturn(true)

            boolean result = pluginSpy.shouldRunPipelineByGit(mockProject, mockGit)

            Assertions.assertTrue(result)
        }

        @Test
        void testShouldRunPipeline_returnsFalse_whenMigrationsDidNotChange() {

            Mockito.when(mockProject.hasProperty("forceGenerate")).thenReturn(false)
            Mockito.when(mockGit.checkIfMigrationsChanged()).thenReturn(false)

            boolean result = pluginSpy.shouldRunPipelineByGit(mockProject, mockGit)

            Assertions.assertFalse(result)
        }
    }
}
