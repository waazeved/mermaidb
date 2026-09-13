package com.waltsoft.mermaidb.task

import com.waltsoft.mermaidb.extension.Extension
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileTree
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

import java.nio.file.Path

class GenerateDiagramTaskTest {

    private static final String TASK_NAME = "generateDatabaseDiagram"
    private static final String DIAGRAM_FILE_NAME = "database-diagram.mmd"
    private static final String MOCK_DIAGRAM_CONTENT = "erDiagram"
    private static final String MAIN_GROUP_NAME = "main"
    private static final String AUTH_GROUP_NAME = "auth"

    @Mock
    private Project project
    @Mock
    private Extension extension
    @Mock
    private ConfigurableFileTree fileTree

    @TempDir
    private Path tempDir

    private File outputDir
    private GenerateDiagramTask generateDiagramTask

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this)
        outputDir = tempDir.toFile()

        Mockito.when(project.file(Mockito.anyString())).thenAnswer { invocation ->
            new File(invocation.getArgument(0))
        }

        Mockito.when(extension.getOutputDirPath()).thenReturn(outputDir.absolutePath)
        Mockito.when(project.fileTree(Mockito.anyMap())).thenReturn(fileTree)
    }

    @Nested
    @DisplayName("Prepare Output Directory")
    class PrepareOutputDir {
        @Test
        void testPrepareOutputDir_whenDirectoryExists() {
            generateDiagramTask = new GenerateDiagramTask(project, extension)
            
            generateDiagramTask.prepareOutputDir()

            Mockito.verify(project).delete(fileTree)
            Mockito.verify(project).fileTree(dir: outputDir.absolutePath)
        }

        @Test
        void testPrepareOutputDir_whenDirectoryDoesNotExist() {
            generateDiagramTask = new GenerateDiagramTask(project, extension)
            File nonExistentDir = Mockito.mock(File.class)
            Mockito.when(nonExistentDir.exists()).thenReturn(false)
            Mockito.when(project.file(extension.getOutputDirPath())).thenReturn(nonExistentDir)

            generateDiagramTask.prepareOutputDir()

            Mockito.verify(project, Mockito.never()).delete(Mockito.any())
            Mockito.verify(nonExistentDir).mkdirs()
        }
    }

    @Nested
    @DisplayName("Process Generated Diagram")
    class ProcessGeneratedDiagram {
        private File diagramFile

        @BeforeEach
        void setUp() {
            diagramFile = new File(outputDir, DIAGRAM_FILE_NAME)
            diagramFile.text = MOCK_DIAGRAM_CONTENT
        }

        @Test
        void testProcessGeneratedDiagram_whenNoGroupsFound() {
            generateDiagramTask = new GenerateDiagramTask(project, extension) {
                @Override
                Optional<Map<String, String>> groupDiagrams(String diagramText) {
                    return Optional.empty()
                }
            }

            generateDiagramTask.processGeneratedDiagram(diagramFile.absolutePath)

            Assertions.assertTrue(diagramFile.exists())
            Assertions.assertEquals(MOCK_DIAGRAM_CONTENT, diagramFile.text)
            Assertions.assertFalse(diagramFile.canWrite())
        }

        @Test
        void testProcessGeneratedDiagram_whenGroupsFound() {
            generateDiagramTask = new GenerateDiagramTask(project, extension) {
                @Override
                Optional<Map<String, String>> groupDiagrams(String diagramText) {
                    return Optional.of([
                            (MAIN_GROUP_NAME): MOCK_DIAGRAM_CONTENT,
                            (AUTH_GROUP_NAME): MOCK_DIAGRAM_CONTENT
                    ])
                }
            }

            generateDiagramTask.processGeneratedDiagram(diagramFile.absolutePath)

            File modulesDir = new File(outputDir, "modules")
            File authDiagramFile = new File(modulesDir, "${AUTH_GROUP_NAME}_${DIAGRAM_FILE_NAME}")
            File mainDiagramFile = new File(outputDir, DIAGRAM_FILE_NAME)

            Assertions.assertTrue(modulesDir.exists())
            Assertions.assertTrue(mainDiagramFile.exists())
            Assertions.assertEquals(MOCK_DIAGRAM_CONTENT, mainDiagramFile.text)
            Assertions.assertFalse(mainDiagramFile.canWrite())
            Assertions.assertTrue(authDiagramFile.exists())
            Assertions.assertEquals(MOCK_DIAGRAM_CONTENT, authDiagramFile.text)
            Assertions.assertFalse(authDiagramFile.canWrite())
        }
    }

    @Test
    void testGetName() {
        generateDiagramTask = new GenerateDiagramTask(project, extension)
        Assertions.assertEquals(TASK_NAME, generateDiagramTask.getName())
    }
}
