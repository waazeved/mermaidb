package com.waltsoft

class MermaidbExtension {
    DatabaseType dbType
    String dbVersion
    String dbCustomDockerImage
    String changeLogFilePath
    String outputFilePath = 'database-diagram.mmd'
    boolean uppercaseColumns = false
}