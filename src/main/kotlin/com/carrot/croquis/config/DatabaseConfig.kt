package com.carrot.croquis.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

@Configuration
@EnableJpaRepositories("com.carrot.croquis.repository")
@EnableTransactionManagement
class DatabaseConfig(
    @Value("\${spring.datasource.driver-class-name}")
    private val DRIVER_CLASS_NAME: String,

    @Value("\${spring.datasource.url.read-write}")
    private val READ_WRITE_URL: String,

    @Value("\${spring.datasource.url.read-only}")
    private val READ_ONLY_URL: String,

    @Value("\${spring.datasource.username}")
    private val USERNAME: String,

    @Value("\${spring.datasource.password}")
    private val PASSWORD: String,

    @Value("\${spring.datasource.hikari.maximum-pool-size}")
    private val MAXIMUM_POOL_SIZE: Int,

    @Value("\${spring.datasource.hikari.auto-commit}")
    private val AUTO_COMMIT: Boolean,

    @Value("\${spring.datasource.hikari.profile-sql}")
    private val PROFILE_SQL: Boolean
) {
    @Bean(name = ["readWriteDataSource"])
    fun readWriteDataSource(): DataSource {
        return buildDataSource(
            DRIVER_CLASS_NAME,
            READ_WRITE_URL,
            USERNAME,
            PASSWORD,
            "read-write",
            MAXIMUM_POOL_SIZE,
            AUTO_COMMIT,
            PROFILE_SQL
        )
    }

    @Bean(name = ["readOnlyDataSource"])
    fun readDataSource(): DataSource {
        return buildDataSource(
            DRIVER_CLASS_NAME,
            READ_ONLY_URL,
            USERNAME,
            PASSWORD,
            "read-only",
            MAXIMUM_POOL_SIZE,
            AUTO_COMMIT,
            PROFILE_SQL
        )
    }

    @Primary
    @Bean(name = ["dataSource"])
    fun dataSource(
        @Qualifier("readWriteDataSource") readWriteDataSource: DataSource,
        @Qualifier("readOnlyDataSource") readOnlyDataSource: DataSource
    ): DataSource {
        return LazyReplicationConnectionDataSourceProxy(readWriteDataSource, readOnlyDataSource)
    }

    private fun buildDataSource(
        driverClassName: String,
        jdbcUrl: String,
        username: String,
        password: String,
        poolName: String,
        maximumPoolSize: Int,
        autoCommit: Boolean,
        profileSql: Boolean
    ): DataSource {

        val config = HikariConfig()
        config.driverClassName = driverClassName
        config.jdbcUrl = jdbcUrl
        config.username = username
        config.password = password
        config.poolName = poolName
        config.maximumPoolSize = maximumPoolSize
        config.isAutoCommit = autoCommit
        config.addDataSourceProperty("profileSql", profileSql)
        if (DRIVER_CLASS_NAME == "com.mysql.cj.jdbc.Driver") {
            config.connectionInitSql = "SET NAMES utf8mb4"
            config.addDataSourceProperty("cachePrepStmts", true)
            config.addDataSourceProperty("prepStmtCacheSize", 250)
            config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048)
            config.addDataSourceProperty("useServerPrepStmts", true)
            config.addDataSourceProperty("useLocalSessionState", true)
            config.addDataSourceProperty("rewriteBatchedStatements", true)
            config.addDataSourceProperty("cacheResultSetMetadata", true)
            config.addDataSourceProperty("cacheServerConfiguration", true)
            config.addDataSourceProperty("elideSetAutoCommits", true)
            config.addDataSourceProperty("maintainTimeStats", false)
            config.addDataSourceProperty("rewriteBatchedStatements", true)
        }

        return HikariDataSource(config)
    }
}