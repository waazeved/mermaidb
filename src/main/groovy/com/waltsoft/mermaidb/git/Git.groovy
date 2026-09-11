package com.waltsoft.mermaidb.git

import com.waltsoft.mermaidb.extension.Extension
import org.gradle.api.Project
import org.gradle.internal.impldep.com.google.common.annotations.VisibleForTesting

class Git {

    private final Project project
    private final Extension extension

    Git(Project project, Extension extension) {
        this.project = project
        this.extension = extension
    }

    void add() {
        try {
            Process process = buildAddCommand().execute(null, project.rootDir)
            add(process)
        } catch (Exception e) {
            println "⚠️ Git repo not found. Skipping 'git add' for the database diagram files."
        }
    }

    @VisibleForTesting
    void add(Process process) {
        process.waitFor()
        println "✅ Diagram files added to git successfully."
    }

    @VisibleForTesting
    String buildAddCommand() {
        return "git add ${extension.outputDirPath}"
    }

    boolean checkIfMigrationsChanged() {
        try {
            Process process = buildDiffCommand().execute(null, project.rootDir)
            return checkIfMigrationsChanged(process)
        } catch (Exception e) {
            println "⚠️ Error checking git staged files: ${e.message}. Assuming migrations changed."
            return true
        }
    }

    @VisibleForTesting
    boolean checkIfMigrationsChanged(Process process) {
        String output = process.text
        process.waitFor()

        if (process.exitValue() != 0) {
            println "⚠️ Warning: Failed to run git diff. Assuming migrations changed to be safe."
            return true
        }

        if (output.trim().isEmpty()) {
            return false
        }

        String migrationDir = getMigrationDir()

        boolean hasMigrations = output.split('\n').any { relativePath ->

            String cleanedPath = relativePath.trim().replace("\\", "/")

            if (cleanedPath.isEmpty()) {
                return false
            }

            boolean isMatch = cleanedPath.contains(migrationDir)
            if (isMatch) {
                println "✅ Staged migration file detected: ${cleanedPath}"
            }

            return isMatch
        }

        return hasMigrations
    }

    @VisibleForTesting
    String buildDiffCommand() {
        return "git diff --name-only --cached"
    }

    @VisibleForTesting
    String getMigrationDir() {
        String changelogFilePath = "src/main/resources/" + extension.changeLogFilePath.replace("\\", "/")
        int lastSlash = changelogFilePath.lastIndexOf('/')
        if (lastSlash > 0) {
            return changelogFilePath.substring(0, lastSlash)
        }
        return changelogFilePath
    }
}
