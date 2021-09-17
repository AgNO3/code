/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.07.2013 by mbechler
 */
package eu.agno3.runtime.db.derby.auth.internal;


import java.sql.SQLException;
import java.util.Properties;
import java.util.Random;

import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.db.derby.auth.DerbyAuthenticatorBackend;
import eu.agno3.runtime.db.derby.auth.UserAccess;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    DerbyAuthenticatorBackend.class
} )
public class DerbyConfigurationAuthenticator implements DerbyAuthenticatorBackend {

    private static final Logger log = Logger.getLogger(DerbyConfigurationAuthenticator.class);

    private static final double MAX_FUZZ_MS = 10;

    private Random randomGen = new Random();

    private DerbyAuthConfiguration authConfiguration;


    @Reference
    protected synchronized void setAuthConfig ( DerbyAuthConfiguration authConfig ) {
        this.authConfiguration = authConfig;
    }


    protected synchronized void unsetAuthConfig ( DerbyAuthConfiguration authConfig ) {
        if ( this.authConfiguration == authConfig ) {
            this.authConfiguration = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.derby.auth.DerbyAuthenticatorBackend#authenticateUser(java.lang.String,
     *      java.lang.String, java.lang.String, java.util.Properties)
     */
    @Override
    public boolean authenticateUser ( String userName, String userPassword, String databaseName, Properties info ) throws SQLException {

        if ( !this.authConfiguration.checkPassword(userName, userPassword) ) {
            return fail();
        }

        if ( this.authConfiguration.getAccess(databaseName, userName) == UserAccess.NONE ) {
            return fail();
        }

        return true;
    }


    private boolean fail () {
        // Add some timing fuzz to mitigate against timing attacks
        try {
            Thread.sleep(Math.round(this.randomGen.nextDouble() * MAX_FUZZ_MS));
        }
        catch ( InterruptedException e ) {
            log.debug("Interrupted", e); //$NON-NLS-1$
        }
        return false;
    }
}
