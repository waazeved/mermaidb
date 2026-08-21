package com.waltsoft.mermaidb.extension

import com.waltsoft.mermaidb.database.DatabaseType
import org.gradle.api.GradleException

class ExtensionValidator {

    private final Extension extension

    ExtensionValidator(Extension extension) {
        this.extension = extension
    }

    void validate() {
        if (extension.dbType == null) {
            throw new GradleException("Error (Mermaidb): The 'dbType' field is required.")
        }

        if (extension.dbType != DatabaseType.SQLITE && (extension.dbVersion == null || extension.dbVersion.trim().isEmpty())) {
            throw new GradleException("Error (Mermaidb): The 'dbVersion' field is required for non-SQLite databases.")
        }

        if (extension.changeLogFilePath == null || extension.changeLogFilePath.trim().isEmpty()) {
            throw new GradleException("Error (Mermaidb): The 'changeLogFilePath' field is required.")
        }

        if (extension.outputDirPath == null || extension.outputDirPath.trim().isEmpty()) {
            throw new GradleException("Error (Mermaidb): The 'outputDirPath' field is required.")
        }

        if (extension.outputFileName == null || extension.outputFileName.trim().isEmpty()) {
            throw new GradleException("Error (Mermaidb): The 'outputFileName' field cannot be empty.")
        }

        if (extension.outputFileName != null && !extension.outputFileName.endsWith(".mmd")) {
            throw new GradleException("Error (Mermaidb): The 'outputFileName' (${extension.outputFileName}) does not end with '.mmd'. It is necessary to use the .mmd extension for Mermaid files.")
        }
    }
}