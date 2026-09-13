package io.github.waazeved.mermaidb.diagram

import io.github.waazeved.mermaidb.database.Database
import io.github.waazeved.mermaidb.extension.Extension
import org.gradle.api.Project

class DiagramGenerator {

    private static final String VERSION = "v0.13.0"

    private final Extension extension
    private final Project project

    DiagramGenerator(Project project, Extension extension) {
        this.extension = extension
        this.project = project
    }

    List<String> buildCommand(String diagramFilePath) {
        def rootDir = project.layout.projectDirectory.asFile.absolutePath.replace("\\", "/")
        String dbHost = Database.DOCKER_CONTAINER_NAME
        String mermerdCommand = buildMermerdCommand(diagramFilePath, dbHost)

        return ['docker', 'run', '--rm',
                '--link', "${dbHost}:db",
                '-v', "${rootDir}:/workspace",
                'golang:alpine',
                'sh', '-c', mermerdCommand]
    }

    String buildMermerdCommand(String diagramFilePath, String dbHost) {
        return "echo '⏳ Compiling Mermerd (This may take a few minutes)...' && " +
                "apk add --no-cache git && " +
                "go install github.com/KarnerTh/mermerd@${VERSION} && " +
                "/go/bin/mermerd " +
                "-c \"${buildDatabaseUrl(dbHost)}\" " +
                "--schema public " +
                "--useAllTables " +
                "--outputFileName /workspace/${diagramFilePath} " +
                "--debug"
    }

    private String buildDatabaseUrl(String dbHost) {
        String urlFormat = extension.dbType.mermerdUrlFormat.replace('@db:', "@${dbHost}:")
        
        return String.format(
                urlFormat,
                extension.dbType.defaultUser,
                extension.dbType.defaultPassword,
                extension.dbType.defaultPort,
                extension.dbType.defaultDbName
        )
    }
}
