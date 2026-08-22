package com.waltsoft.mermaidb.git

import com.waltsoft.mermaidb.extension.Extension
import groovy.transform.Memoized
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
            "git add ${extension.outputDirPath}".execute()
        } catch (Exception e) {
            println "⚠️ Git repo not found. Skipping 'git add' for the database diagram files."
        }
    }

    @Memoized
    boolean checkIfMigrationsChanged() {
        File migrationFile = project.file("src/main/resources/" + extension.changeLogFilePath)
        String migrationDirPath = migrationFile.getParentFile().getAbsolutePath()

        try {
            Process process = "git diff --name-only --cached".execute(null, project.rootDir)
            process.waitFor()

            if (process.exitValue() != 0) {
                println "⚠️ Warning: Failed to run git diff. Assuming migrations changed to be safe."
                return true
            }

            String output = process.in.text
            if (output.trim().isEmpty()) {
                return false
            }

            String[] stagedFiles = output.split('\n')

            for (String relativePath : stagedFiles) {
                if (relativePath.trim().isEmpty()) continue

                File stagedFile = project.file(relativePath.trim())

                if (stagedFile.getAbsolutePath().startsWith(migrationDirPath)) {
                    println "✅ Staged migration file detected: ${relativePath}"
                    return true
                }
            }

        } catch (Exception e) {
            println "⚠️ Error checking git staged files: ${e.message}. Assuming migrations changed."
            return true
        }

        return false
    }

}
