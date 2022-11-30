package com.carrot.croquis.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

public class LazyReplicationConnectionDataSourceProxy implements DataSource {

    private Logger log = LoggerFactory.getLogger(LazyReplicationConnectionDataSourceProxy.class);

    private DataSource writeDataSource;
    private DataSource readDataSource;
    private Boolean defaultAutoCommit;
    private Integer defaultTransactionIsolation;

    public LazyReplicationConnectionDataSourceProxy() {
    }

    public LazyReplicationConnectionDataSourceProxy(DataSource writeDataSource, DataSource readDataSource) {
        this.writeDataSource = writeDataSource;
        this.readDataSource = readDataSource;
        init();
    }

    public void init() {
        // Determine default auto-commit and transaction isolation
        // via a Connection from the target DataSource, if possible.
        if (this.defaultAutoCommit == null || this.defaultTransactionIsolation == null) {
            try {
                Connection con = getWriteDataSource().getConnection();
                try {
                    checkDefaultConnectionProperties(con);
                } finally {
                    con.close();
                }
            } catch (SQLException ex) {
                log.warn("Could not retrieve default auto-commit and transaction isolation settings", ex);
            }
        }
    }

    public DataSource setWriteDataSource(DataSource writeDataSource) {
        return this.writeDataSource = writeDataSource;
    }

    public DataSource getWriteDataSource() {
        return this.writeDataSource;
    }

    public DataSource setReadDataSource(DataSource readDataSource) {
        return this.readDataSource = readDataSource;
    }

    public DataSource getReadDataSource() {
        return this.readDataSource;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return (Connection) Proxy.newProxyInstance(
                ReplicationConnectionProxy.class.getClassLoader(),
                new Class<?>[]{ReplicationConnectionProxy.class},
                new LazyReplicationConnectionInvocationHandler());
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return (Connection) Proxy.newProxyInstance(
                ReplicationConnectionProxy.class.getClassLoader(),
                new Class<?>[]{ReplicationConnectionProxy.class},
                new LazyReplicationConnectionInvocationHandler(username, password));
    }

    private static interface ReplicationConnectionProxy extends Connection {
        Connection getReplicationTargetConnection();
    }



    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        getWriteDataSource().setLogWriter(out);
        getReadDataSource().setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        getWriteDataSource().setLoginTimeout(seconds);
        getReadDataSource().setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return getWriteDataSource().getLoginTimeout();
    }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return java.util.logging.Logger.getLogger(java.util.logging.Logger.GLOBAL_LOGGER_NAME);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return (T) this;
        }
        return getWriteDataSource().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return (iface.isInstance(this) || getWriteDataSource().isWrapperFor(iface));
    }

    public void setDefaultAutoCommit(boolean defaultAutoCommit) {
        this.defaultAutoCommit = defaultAutoCommit;
    }

    public void setDefaultTransactionIsolation(int defaultTransactionIsolation) {
        this.defaultTransactionIsolation = defaultTransactionIsolation;
    }

    protected synchronized void checkDefaultConnectionProperties(Connection con) throws SQLException {
        if (this.defaultAutoCommit == null) {
            this.defaultAutoCommit = con.getAutoCommit();
        }
        if (this.defaultTransactionIsolation == null) {
            this.defaultTransactionIsolation = con.getTransactionIsolation();
        }
    }

    protected Boolean defaultAutoCommit() {
        return this.defaultAutoCommit;
    }

    protected Integer defaultTransactionIsolation() {
        return this.defaultTransactionIsolation;
    }

    private class LazyReplicationConnectionInvocationHandler implements InvocationHandler {

        private String username;

        private String password;

        private Boolean readOnly = Boolean.FALSE;

        private Integer transactionIsolation;

        private Boolean autoCommit;

        private boolean closed = false;

        private Connection replicationTargetConnection;

        public LazyReplicationConnectionInvocationHandler() {
            this.autoCommit = defaultAutoCommit();
            this.transactionIsolation = defaultTransactionIsolation();
        }

        public LazyReplicationConnectionInvocationHandler(String username, String password) {
            this();
            this.username = username;
            this.password = password;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // Invocation on ReplicationConnectionProxy interface coming in...

            if (method.getName().equals("equals")) {
                // We must avoid fetching a target Connection for "equals".
                // Only consider equal when proxies are identical.
                return (proxy == args[0]);
            } else if (method.getName().equals("hashCode")) {
                // We must avoid fetching a target Connection for "hashCode",
                // and we must return the same hash code even when the target
                // Connection has been fetched: use hashCode of Connection proxy.
                return System.identityHashCode(proxy);
            } else if (method.getName().equals("unwrap")) {
                if (((Class<?>) args[0]).isInstance(proxy)) {
                    return proxy;
                }
            } else if (method.getName().equals("isWrapperFor")) {
                if (((Class<?>) args[0]).isInstance(proxy)) {
                    return true;
                }
            } else if (method.getName().equals("getReplicationTargetConnection")) {
                // Handle getReplicationTargetConnection method: return underlying connection.
                return getReplicationTargetConnection(method);
            }

            if (!hasTargetConnection()) {
                // No physical target Connection kept yet ->
                // resolve transaction demarcation methods without fetching
                // a physical JDBC Connection until absolutely necessary.

                if (method.getName().equals("toString")) {
                    return "Lazy Connection proxy for target write DataSource  [" + getWriteDataSource() + "] and target read DataSource [" + getReadDataSource() + "]";
                } else if (method.getName().equals("isReadOnly")) {
                    return this.readOnly;
                } else if (method.getName().equals("setReadOnly")) {
                    this.readOnly = (Boolean) args[0];
                    return null;
                } else if (method.getName().equals("getTransactionIsolation")) {
                    if (this.transactionIsolation != null) {
                        return this.transactionIsolation;
                    }
                    // Else fetch actual Connection and check there,
                    // because we didn't have a default specified.
                } else if (method.getName().equals("setTransactionIsolation")) {
                    this.transactionIsolation = (Integer) args[0];
                    return null;
                } else if (method.getName().equals("getAutoCommit")) {
                    if (this.autoCommit != null) {
                        return this.autoCommit;
                    }
                    // Else fetch actual Connection and check there,
                    // because we didn't have a default specified.
                } else if (method.getName().equals("setAutoCommit")) {
                    this.autoCommit = (Boolean) args[0];
                    return null;
                } else if (method.getName().equals("commit")) {
                    // Ignore: no statements created yet.
                    return null;
                } else if (method.getName().equals("rollback")) {
                    // Ignore: no statements created yet.
                    return null;
                } else if (method.getName().equals("getWarnings")) {
                    return null;
                } else if (method.getName().equals("clearWarnings")) {
                    return null;
                } else if (method.getName().equals("close")) {
                    // Ignore: no target connection yet.
                    this.closed = true;
                    return null;
                } else if (method.getName().equals("isClosed")) {
                    return this.closed;
                } else if (this.closed) {
                    // Connection proxy closed, without ever having fetched a
                    // physical JDBC Connection: throw corresponding SQLException.
                    throw new SQLException("Illegal operation: connection is closed");
                }
            }

            // Target Connection already fetched,
            // or target Connection necessary for current operation ->
            // invoke method on target connection.
            try {
                return method.invoke(getReplicationTargetConnection(method), args);
            } catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        }

        private boolean hasTargetConnection() {
            return (this.replicationTargetConnection != null);
        }

        private Connection getReplicationTargetConnection(Method operation) throws SQLException {
            if (this.replicationTargetConnection == null) {
                log.debug("Connecting to database for operation '{}'", operation.getName());

                log.debug("current readOnly : {}", readOnly);
                DataSource targetDataSource = (readOnly == Boolean.TRUE) ? getReadDataSource() : getWriteDataSource();

                // Fetch physical Connection from DataSource.
                this.replicationTargetConnection = (this.username != null) ?
                        targetDataSource.getConnection(this.username, this.password) :
                        targetDataSource.getConnection();

                // If we still lack default connection properties, check them now.
                checkDefaultConnectionProperties(this.replicationTargetConnection);

                // Apply kept transaction settings, if any.
                if (this.readOnly) {
                    try {
                        this.replicationTargetConnection.setReadOnly(this.readOnly);
                    } catch (Exception ex) {
                        // "read-only not supported" -> ignore, it's just a hint anyway
                        log.debug("Could not set JDBC Connection read-only", ex);
                    }
                }
                if (this.transactionIsolation != null &&
                        !this.transactionIsolation.equals(defaultTransactionIsolation())) {
                    this.replicationTargetConnection.setTransactionIsolation(this.transactionIsolation);
                }
                if (this.autoCommit != null && this.autoCommit != this.replicationTargetConnection.getAutoCommit()) {
                    this.replicationTargetConnection.setAutoCommit(this.autoCommit);
                }
            } else {
                log.debug("Using existing database connection for operation '{}'", operation.getName());
            }

            return this.replicationTargetConnection;
        }
    }
}
