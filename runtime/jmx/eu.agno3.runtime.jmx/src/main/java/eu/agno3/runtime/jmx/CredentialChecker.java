/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.03.2017 by mbechler
 */
package eu.agno3.runtime.jmx;


/**
 * @author mbechler
 *
 */
public interface CredentialChecker {

    /**
     * @param user
     * @param pass
     * @return true if the username/password is valid
     */
    boolean verifyPassword ( String user, String pass );

}
