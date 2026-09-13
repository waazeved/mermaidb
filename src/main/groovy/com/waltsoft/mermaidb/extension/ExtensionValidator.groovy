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

        boolean isNotSqlite = extension.dbType != DatabaseType.SQLITE
        boolean hasNoCustomImage = extension.dbCustomDockerImage == null || extension.dbCustomDockerImage.trim().isEmpty()
        boolean hasNoDbVersion = extension.dbVersion == null || extension.dbVersion.trim().isEmpty()

        if (isNotSqlite && hasNoCustomImage && hasNoDbVersion) {
            throw new GradleException("Error (Mermaidb): The 'dbVersion' field is required when not using a custom Docker image.")
        }

        if (extension.changeLogFilePath == null || extension.changeLogFilePath.trim().isEmpty()) {
            throw new GradleException("Error (Mermaidb): The 'changeLogFilePath' field is required.")
        }

        if (extension.outputDirPath == null || extension.outputDirPath.trim().isEmpty()) {
            throw new GradleException("Error (Mermaidb): The 'outputDirPath' field is required.")
        }
    }
}