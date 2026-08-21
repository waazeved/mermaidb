package com.waltsoft.mermaidb.extension

import com.waltsoft.mermaidb.database.DatabaseType

class Extension {
    DatabaseType dbType
    String dbVersion
    String dbCustomDockerImage
    String changeLogFilePath
    String outputDirPath
    String outputFileName = 'database-diagram.mmd'
    boolean autoGitAdd = false
    boolean uppercaseColumns = false
}