/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.01.2014 by mbechler
 */
package eu.agno3.runtime.db.derby.server;


/**
 * @author mbechler
 * 
 */
@SuppressWarnings ( "javadoc" )
public final class DerbyConfigProperties {

    public static final String RO_USERS = "derby.database.readOnlyAccessUsers"; //$NON-NLS-1$
    public static final String RW_USERS = "derby.database.fullAccessUsers"; //$NON-NLS-1$
    public static final String LOG_STATEMENTS = "derby.language.logStatementText"; //$NON-NLS-1$
    public static final String DEADLOCK_TRACE = "derby.locks.deadlockTrace"; //$NON-NLS-1$
    public static final String LOCKS_MONITOR = "derby.locks.monitor"; //$NON-NLS-1$
    public static final String LOG_SEVERITY = "derby.stream.error.logSeverityLevel"; //$NON-NLS-1$
    public static final String SYSTEM_HOME = "derby.system.home"; //$NON-NLS-1$
    public static final String STREAM_ERROR_FIELD = "derby.stream.error.field"; //$NON-NLS-1$
    public static final String DATABASE_PROPERTIES_ONLY = "derby.database.propertiesOnly"; //$NON-NLS-1$
    public static final String DEFAULT_CONN_MODE = "derby.database.defaultConnectionMode"; //$NON-NLS-1$
    public static final String AUTH_PROVIDER = "derby.authentication.provider"; //$NON-NLS-1$
    public static final String CONN_REQUIRE_AUTH = "derby.connection.requireAuthentication"; //$NON-NLS-1$

    public static final String PAGE_CACHE_SIZE = "derby.storage.pageCacheSize"; //$NON-NLS-1$
    public static final String PAGE_SIZE = "derby.storage.pageSize"; //$NON-NLS-1$

    public static final String NO_ACCESS = "noAccess"; //$NON-NLS-1$
    public static final String SHUTDOWN = "shutdown"; //$NON-NLS-1$
    public static final String SHUTDOWN_SQLCODE = "08006"; //$NON-NLS-1$


    private DerbyConfigProperties () {}

}
