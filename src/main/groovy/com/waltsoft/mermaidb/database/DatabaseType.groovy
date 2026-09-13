package com.waltsoft.mermaidb.database

enum DatabaseType {

    POSTGRESQL(
            POSTGRESQL_PORT, POSTGRESQL_IMAGE,
            POSTGRESQL_JDBC_URL, POSTGRESQL_MERMERD_URL,
            DEFAULT_USER, DEFAULT_PASSWORD, DEFAULT_DB_NAME,
            POSTGRESQL_DRIVER_DEPENDENCY, POSTGRESQL_DRIVER_CLASS
    ),
    ALLOYDB(
            POSTGRESQL_PORT, ALLOYDB_IMAGE,
            POSTGRESQL_JDBC_URL, POSTGRESQL_MERMERD_URL,
            DEFAULT_USER, DEFAULT_PASSWORD, DEFAULT_DB_NAME,
            POSTGRESQL_DRIVER_DEPENDENCY, POSTGRESQL_DRIVER_CLASS
    ),
    MYSQL(
            MYSQL_PORT, MYSQL_IMAGE,
            MYSQL_JDBC_URL, MYSQL_MERMERD_URL,
            MYSQL_ROOT_USER, DEFAULT_PASSWORD, DEFAULT_DB_NAME,
            MYSQL_DRIVER_DEPENDENCY, MYSQL_DRIVER_CLASS
    ),
    SQLSERVER(
            SQLSERVER_PORT, SQLSERVER_IMAGE,
            SQLSERVER_JDBC_URL, SQLSERVER_MERMERD_URL,
            SQLSERVER_USER, SQLSERVER_PASSWORD, DEFAULT_DB_NAME,
            SQLSERVER_DRIVER_DEPENDENCY, SQLSERVER_DRIVER_CLASS
    ),
    SQLITE(
            SQLITE_PORT, EMPTY_STRING,
            SQLITE_JDBC_URL, SQLITE_MERMERD_URL,
            EMPTY_STRING, EMPTY_STRING, DEFAULT_DB_NAME,
            SQLITE_DRIVER_DEPENDENCY, SQLITE_DRIVER_CLASS
    ),
    MARIADB(
            MYSQL_PORT, MARIADB_IMAGE,
            MARIADB_JDBC_URL, MYSQL_MERMERD_URL,
            MYSQL_ROOT_USER, DEFAULT_PASSWORD, DEFAULT_DB_NAME,
            MARIADB_DRIVER_DEPENDENCY, MARIADB_DRIVER_CLASS
    ),
    COCKROACHDB(
            COCKROACHDB_PORT, COCKROACHDB_IMAGE,
            POSTGRESQL_JDBC_URL, POSTGRESQL_MERMERD_URL,
            DEFAULT_USER, DEFAULT_PASSWORD, DEFAULT_DB_NAME,
            POSTGRESQL_DRIVER_DEPENDENCY, POSTGRESQL_DRIVER_CLASS
    ),
    TIDB(
            TIDB_PORT, TIDB_IMAGE,
            MYSQL_JDBC_URL, MYSQL_MERMERD_URL,
            MYSQL_ROOT_USER, DEFAULT_PASSWORD, DEFAULT_DB_NAME,
            MYSQL_DRIVER_DEPENDENCY, MYSQL_DRIVER_CLASS
    );

    private static final String DEFAULT_USER = "usr"
    private static final String DEFAULT_PASSWORD = "pwd"
    private static final String DEFAULT_DB_NAME = "mydb"
    private static final String SQLSERVER_USER = "sa"
    private static final String SQLSERVER_PASSWORD = "pwdStrong!123"
    private static final String MYSQL_ROOT_USER = "root"
    private static final String EMPTY_STRING = ""

    private static final int POSTGRESQL_PORT = 5432
    private static final int MYSQL_PORT = 3306
    private static final int SQLSERVER_PORT = 1433
    private static final int COCKROACHDB_PORT = 26257
    private static final int TIDB_PORT = 4000
    private static final int SQLITE_PORT = 0

    private static final String POSTGRESQL_IMAGE = "postgres:%s-alpine"
    private static final String ALLOYDB_IMAGE = "google/alloydbomni:%s"
    private static final String MYSQL_IMAGE = "mysql:%s"
    private static final String SQLSERVER_IMAGE = "mcr.microsoft.com/mssql/server:%s-latest"
    private static final String MARIADB_IMAGE = "mariadb:%s"
    private static final String COCKROACHDB_IMAGE = "cockroachdb/cockroach:%s"
    private static final String TIDB_IMAGE = "pingcap/tidb:%s"

    private static final String POSTGRESQL_JDBC_URL = 'jdbc:postgresql://localhost:%d/%s'
    private static final String MYSQL_JDBC_URL = 'jdbc:mysql://localhost:%d/%s'
    private static final String SQLSERVER_JDBC_URL = 'jdbc:sqlserver://localhost:%d;databaseName=%s'
    private static final String SQLITE_JDBC_URL = 'jdbc:sqlite:%2$s'
    private static final String MARIADB_JDBC_URL = 'jdbc:mariadb://localhost:%d/%s'

    private static final String POSTGRESQL_MERMERD_URL = 'postgresql://%s:%s@db:%d/%s'
    private static final String MYSQL_MERMERD_URL = 'mysql://%s:%s@db:%d/%s'
    private static final String SQLSERVER_MERMERD_URL = 'sqlserver://%s:%s@db:%d?database=%s'
    private static final String SQLITE_MERMERD_URL = 'sqlite://%4$s'

    private static final String POSTGRESQL_DRIVER_DEPENDENCY = "org.postgresql:postgresql:42.7.5"
    private static final String MYSQL_DRIVER_DEPENDENCY      = "com.mysql:mysql-connector-j:8.3.0"
    private static final String SQLSERVER_DRIVER_DEPENDENCY  = "com.microsoft.sqlserver:mssql-jdbc:12.6.1.jre11"
    private static final String SQLITE_DRIVER_DEPENDENCY     = "org.xerial:sqlite-jdbc:3.45.2.0"
    private static final String MARIADB_DRIVER_DEPENDENCY    = "org.mariadb.jdbc:mariadb-java-client:3.3.3"

    private static final String POSTGRESQL_DRIVER_CLASS = "org.postgresql.Driver"
    private static final String MYSQL_DRIVER_CLASS = "com.mysql.cj.jdbc.Driver"
    private static final String SQLSERVER_DRIVER_CLASS = "com.microsoft.sqlserver.jdbc.SQLServerDriver"
    private static final String SQLITE_DRIVER_CLASS = "org.sqlite.JDBC"
    private static final String MARIADB_DRIVER_CLASS = "org.mariadb.jdbc.Driver"

    final int defaultPort
    final String dockerImageFormat
    final String jdbcUrlFormat
    final String mermerdUrlFormat
    final String defaultUser
    final String defaultPassword
    final String defaultDbName
    final String jdbcDriverDependency
    final String driverClassName

    DatabaseType(int defaultPort, String dockerImageFormat,
                 String jdbcUrlFormat, String mermerdUrlFormat,
                 String defaultUser, String defaultPassword, String defaultDbName,
                 String jdbcDriverDependency, String driverClassName) {
        this.defaultPort = defaultPort
        this.dockerImageFormat = dockerImageFormat
        this.jdbcUrlFormat = jdbcUrlFormat
        this.mermerdUrlFormat = mermerdUrlFormat
        this.defaultUser = defaultUser
        this.defaultPassword = defaultPassword
        this.defaultDbName = defaultDbName
        this.jdbcDriverDependency = jdbcDriverDependency
        this.driverClassName = driverClassName
    }

    String getDockerImageName(String version) {
        return String.format(dockerImageFormat, version)
    }

    static DatabaseType fromString(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Mermaidb: The database type name cannot be null or empty.")
        }

        DatabaseType match = values().find { it.name().equalsIgnoreCase(name.trim()) }

        if (match == null) {
            throw new IllegalArgumentException("Mermaidb: Unknown database type '${name}'. " +
                    "Supported types are: ${values().collect { it.name() }.join(', ')}")
        }

        return match
    }
}
