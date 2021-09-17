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


/**
 * @author mbechler
 * 
 */
public interface DerbyAuthenticatorBackend extends UserAuthenticator {

    @Override
    boolean authenticateUser ( String userName, String userPassword, String databaseName, Properties info ) throws SQLException;
}
