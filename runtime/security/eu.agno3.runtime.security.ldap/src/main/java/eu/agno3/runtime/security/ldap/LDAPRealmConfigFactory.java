/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.03.2015 by mbechler
 */
package eu.agno3.runtime.security.ldap;


import java.util.Dictionary;

import com.unboundid.ldap.sdk.LDAPException;


/**
 * @author mbechler
 *
 */
public interface LDAPRealmConfigFactory {

    /**
     * 
     * @param properties
     * @return parsed configuration
     * @throws LDAPException
     */
    public LDAPRealmConfig createConfig ( Dictionary<String, Object> properties ) throws LDAPException;
}
