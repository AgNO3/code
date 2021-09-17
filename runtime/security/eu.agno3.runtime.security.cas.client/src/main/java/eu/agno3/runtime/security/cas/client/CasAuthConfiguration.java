/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.10.2014 by mbechler
 */
package eu.agno3.runtime.security.cas.client;


import java.security.PublicKey;
import java.util.Collection;


/**
 * @author mbechler
 *
 */
public interface CasAuthConfiguration {

    /**
     * 
     * @return the cas server prefix
     */
    String getAuthServerBase ();


    /**
     * @param overrideServerName
     * @return the cas server prefix
     */
    default String getAuthServerBase ( String overrideServerName ) {
        return getAuthServerBase();
    }


    /**
     * @return the local service address
     */
    String getLocalService ();


    /**
     * 
     * @return the local proxy callback address
     */
    String getLocalProxyCallbackAddress ();


    /**
     * @return the roles to add to all authenticated users
     */
    Collection<String> getDefaultRoles ();


    /**
     * @return the permissions to add to all authenticated users
     */
    Collection<String> getDefaultPermissions ();


    /**
     * @return a CAS attribute specifying roles to add, null if none
     */
    String getRoleAttribute ();


    /**
     * @return a CAS attribute specifiying permissions to add, null if none
     */
    String getPermissionAttribute ();


    /**
     * @return the servers public key
     */
    PublicKey getAuthServerPubKey ();

}