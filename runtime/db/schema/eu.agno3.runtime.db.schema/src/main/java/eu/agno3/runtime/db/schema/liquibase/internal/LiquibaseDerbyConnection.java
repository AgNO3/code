/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.07.2014 by mbechler
 */
package eu.agno3.runtime.db.schema.liquibase.internal;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;


/**
 * @author mbechler
 * 
 */
public class LiquibaseDerbyConnection extends JdbcConnection implements DatabaseConnection {

    private static final Logger log = Logger.getLogger(LiquibaseDerbyConnection.class);
    private boolean originalAutoCommit;


    /**
     * @param connection
     */
    public LiquibaseDerbyConnection ( Connection connection ) {
        super(connection);
        try {
            this.originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
        }
        catch ( SQLException e ) {
            log.warn("Failed to disable autocommit", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see liquibase.database.jvm.JdbcConnection#commit()
     */
    @Override
    public void commit () throws DatabaseException {
        log.debug("Commit"); //$NON-NLS-1$
        super.commit();
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.database.jvm.JdbcConnection#close()
     */
    @Override
    public void close () throws DatabaseException {
        // Reenable autocommit before closing connection
        // otherwise a bogus exception is thrown

        this.commit();
        this.setAutoCommit(this.originalAutoCommit);
        this.checkPoint();
        super.close();
    }


    private void checkPoint () throws DatabaseException {
        log.debug("Checkpointing database"); //$NON-NLS-1$
        try ( Statement st = createStatement() ) {
            st.execute("CALL SYSCS_UTIL.SYSCS_CHECKPOINT_DATABASE()"); //$NON-NLS-1$
        }
        catch ( SQLException e ) {
            throw new DatabaseException(e);
        }
    }

}
