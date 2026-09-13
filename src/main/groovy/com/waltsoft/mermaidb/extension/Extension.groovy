package com.waltsoft.mermaidb.extension

import com.waltsoft.mermaidb.database.DatabaseType

class Extension {

    DatabaseType dbType
    String dbVersion
    String dbCustomDockerImage
    String changeLogFilePath
    String outputDirPath
    boolean autoGitAdd = false
    boolean uppercaseColumns = false

    void setDbType(String dbTypeName) {
        this.dbType = DatabaseType.fromString(dbTypeName)
    }

    void setDbType(DatabaseType dbType) {
        this.dbType = dbType
    }
}