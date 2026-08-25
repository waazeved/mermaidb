package com.waltsoft.mermaidb.diagram

import com.waltsoft.mermaidb.database.Database
import com.waltsoft.mermaidb.extension.Extension
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

        return ['docker', 'run', '--rm',
                '--link', "${Database.DOCKER_CONTAINER_NAME}:db",
                '-v', "${rootDir}:/workspace",
                'golang:alpine',
                'sh', '-c', buildMermerdCommand(diagramFilePath)]
    }

    private String buildMermerdCommand(String diagramFilePath) {
        return "echo '⏳ Compiling Mermerd (This may take a few minutes)...' && " +
                "apk add --no-cache git && " +
                "go install github.com/KarnerTh/mermerd@${VERSION} && " +
                "/go/bin/mermerd " +
                "-c \"${buildDatabaseUrl()}\" " +
                "--schema public " +
                "--useAllTables " +
                "--outputFileName /workspace/${diagramFilePath}"
    }

    private String buildDatabaseUrl() {
        return String.format(
                extension.dbType.mermerdUrlFormat,
                extension.dbType.defaultUser,
                extension.dbType.defaultPassword,
                extension.dbType.defaultPort,
                extension.dbType.defaultDbName
        )
    }
}
