package com.waltsoft

enum DatabaseType {

    POSTGRESQL(
            POSTGRESQL_PORT, POSTGRESQL_IMAGE,
            POSTGRESQL_JDBC_URL, POSTGRESQL_MERMERD_URL,
            DEFAULT_USER, DEFAULT_PASSWORD, DEFAULT_DB_NAME
    ),
    ALLOYDB(
            POSTGRESQL_PORT, POSTGRESQL_IMAGE,
            POSTGRESQL_JDBC_URL, POSTGRESQL_MERMERD_URL,
            DEFAULT_USER, DEFAULT_PASSWORD, DEFAULT_DB_NAME
    ),
    MYSQL(
            MYSQL_PORT, MYSQL_IMAGE,
            MYSQL_JDBC_URL, MYSQL_MERMERD_URL,
            MYSQL_ROOT_USER, DEFAULT_PASSWORD, DEFAULT_DB_NAME
    ),
    SQLSERVER(
            SQLSERVER_PORT, SQLSERVER_IMAGE,
            SQLSERVER_JDBC_URL, SQLSERVER_MERMERD_URL,
            SQLSERVER_USER, SQLSERVER_PASSWORD, DEFAULT_DB_NAME
    ),
    SQLITE(
            SQLITE_PORT, EMPTY_STRING,
            SQLITE_JDBC_URL, SQLITE_MERMERD_URL,
            EMPTY_STRING, EMPTY_STRING, DEFAULT_DB_NAME
    ),
    MARIADB(
            MYSQL_PORT, MARIADB_IMAGE,
            MARIADB_JDBC_URL, MYSQL_MERMERD_URL,
            MYSQL_ROOT_USER, DEFAULT_PASSWORD, DEFAULT_DB_NAME
    ),
    COCKROACHDB(
            COCKROACHDB_PORT, COCKROACHDB_IMAGE,
            POSTGRESQL_JDBC_URL, POSTGRESQL_MERMERD_URL,
            DEFAULT_USER, DEFAULT_PASSWORD, DEFAULT_DB_NAME
    ),
    TIDB(
            TIDB_PORT, TIDB_IMAGE,
            MYSQL_JDBC_URL, MYSQL_MERMERD_URL,
            MYSQL_ROOT_USER, DEFAULT_PASSWORD, DEFAULT_DB_NAME
    );

    public static final String DEFAULT_USER       = "usr"
    public static final String DEFAULT_PASSWORD   = "pwd"
    public static final String DEFAULT_DB_NAME    = "mydb"
    public static final String SQLSERVER_USER     = "sa"
    public static final String SQLSERVER_PASSWORD = "pwdStrong!123"
    public static final String MYSQL_ROOT_USER    = "root"
    public static final String EMPTY_STRING       = ""

    public static final int POSTGRESQL_PORT  = 5432
    public static final int MYSQL_PORT       = 3306
    public static final int SQLSERVER_PORT   = 1433
    public static final int COCKROACHDB_PORT = 26257
    public static final int TIDB_PORT        = 4000
    public static final int SQLITE_PORT      = 0

    public static final String POSTGRESQL_IMAGE  = "postgres:%s-alpine"
    public static final String MYSQL_IMAGE       = "mysql:%s"
    public static final String SQLSERVER_IMAGE   = "mcr.microsoft.com/mssql/server:%s-latest"
    public static final String MARIADB_IMAGE     = "mariadb:%s"
    public static final String COCKROACHDB_IMAGE = "cockroachdb/cockroach:%s"
    public static final String TIDB_IMAGE        = "pingcap/tidb:%s"

    public static final String POSTGRESQL_JDBC_URL = 'jdbc:postgresql://localhost:%d/%s'
    public static final String MYSQL_JDBC_URL      = 'jdbc:mysql://localhost:%d/%s'
    public static final String SQLSERVER_JDBC_URL  = 'jdbc:sqlserver://localhost:%d;databaseName=%s'
    public static final String SQLITE_JDBC_URL     = 'jdbc:sqlite:%2$s'
    public static final String MARIADB_JDBC_URL    = 'jdbc:mariadb://localhost:%d/%s'

    public static final String POSTGRESQL_MERMERD_URL = 'postgresql://%s:%s@db:%d/%s'
    public static final String MYSQL_MERMERD_URL      = 'mysql://%s:%s@db:%d/%s'
    public static final String SQLSERVER_MERMERD_URL  = 'sqlserver://%s:%s@db:%d?database=%s'
    public static final String SQLITE_MERMERD_URL     = 'sqlite://%4$s'

    final int defaultPort
    final String dockerImageFormat
    final String jdbcUrlFormat
    final String mermerdUrlFormat
    final String defaultUser
    final String defaultPassword
    final String defaultDbName

    DatabaseType(int defaultPort, String dockerImageFormat,
                 String jdbcUrlFormat, String mermerdUrlFormat,
                 String defaultUser, String defaultPassword, String defaultDbName) {
        this.defaultPort = defaultPort
        this.dockerImageFormat = dockerImageFormat
        this.jdbcUrlFormat = jdbcUrlFormat
        this.mermerdUrlFormat = mermerdUrlFormat
        this.defaultUser = defaultUser
        this.defaultPassword = defaultPassword
        this.defaultDbName = defaultDbName
    }
}