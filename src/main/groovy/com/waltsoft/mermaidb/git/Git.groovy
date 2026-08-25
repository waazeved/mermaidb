package com.waltsoft.mermaidb.git

import com.waltsoft.mermaidb.extension.Extension
import org.gradle.api.Project

class Git {

    private final Project project
    private final Extension extension

    Git(Project project, Extension extension) {
        this.project = project
        this.extension = extension
    }

    void add() {
        try {
            Process process = "git add ${extension.outputDirPath}".execute(null, project.rootDir)
            process.waitFor()
            println "✅ Diagram files added to git successfully."
        } catch (Exception e) {
            println "⚠️ Git repo not found. Skipping 'git add' for the database diagram files."
        }
    }

    boolean checkIfMigrationsChanged() {
        String changelogFilePath = "src/main/resources/" + extension.changeLogFilePath.replace("\\", "/")

        String migrationDir = changelogFilePath
        int lastSlash = changelogFilePath.lastIndexOf('/')

        if (lastSlash > 0) {
            migrationDir = changelogFilePath.substring(0, lastSlash)
        }

        try {
            Process process = "git diff --name-only --cached".execute(null, project.rootDir)
            String output = process.text
            process.waitFor()

            if (process.exitValue() != 0) {
                println "⚠️ Warning: Failed to run git diff. Assuming migrations changed to be safe."
                return true
            }

            if (output.trim().isEmpty()) {
                return false
            }

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

        } catch (Exception e) {
            println "⚠️ Error checking git staged files: ${e.message}. Assuming migrations changed."
            return true
        }
    }
}
