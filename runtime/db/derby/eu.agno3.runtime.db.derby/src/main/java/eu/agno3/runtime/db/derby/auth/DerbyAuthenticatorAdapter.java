/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.07.2013 by mbechler
 */
package eu.agno3.runtime.db.derby.auth;


import java.sql.SQLException;
import java.util.Properties;

import org.apache.derby.authentication.UserAuthenticator;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    UserAuthenticator.class
}, immediate = true )
public class DerbyAuthenticatorAdapter implements UserAuthenticator {

    private static final Logger log = Logger.getLogger(DerbyAuthenticatorAdapter.class);
    private static DerbyAuthenticatorBackend backend;


    @Reference
    protected synchronized void setBackend ( DerbyAuthenticatorBackend b ) {
        backend = b;
    }


    protected synchronized void unsetBackend ( DerbyAuthenticatorBackend b ) {
        if ( backend == b ) {
            backend = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.apache.derby.authentication.UserAuthenticator#authenticateUser(java.lang.String, java.lang.String,
     *      java.lang.String, java.util.Properties)
     */
    @Override
    public boolean authenticateUser ( String userName, String userPassword, String databaseName, Properties info ) throws SQLException {

        log.debug(String.format("Trying to authenticate derby user '%s' accessing '%s'", userName, databaseName)); //$NON-NLS-1$

        if ( backend == null ) {
            log.warn("Authentication backend not available"); //$NON-NLS-1$
            return false;
        }

        boolean successful = false;

        try {
            successful = backend.authenticateUser(userName, userPassword, databaseName, info);
        }
        catch ( Exception e ) {
            log.error("Failure in authentication module:", e); //$NON-NLS-1$
        }

        if ( !successful ) {
            log.warn(String.format("Failed to to authenticate derby user '%s' accessing '%s'", userName, databaseName)); //$NON-NLS-1$
        }

        return successful;
    }
}
