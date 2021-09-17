/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.03.2015 by mbechler
 */
package eu.agno3.runtime.security.ldap.internal;


import java.util.Dictionary;

import org.osgi.service.component.annotations.Component;

import com.unboundid.ldap.sdk.LDAPException;

import eu.agno3.runtime.security.ldap.LDAPRealmConfig;
import eu.agno3.runtime.security.ldap.LDAPRealmConfigFactory;


/**
 * @author mbechler
 *
 */
@Component ( service = LDAPRealmConfigFactory.class )
public class LDAPRealmConfigFactoryImpl implements LDAPRealmConfigFactory {

    /**
     * {@inheritDoc}
     * 
     * @throws LDAPException
     *
     * @see eu.agno3.runtime.security.ldap.LDAPRealmConfigFactory#createConfig(java.util.Dictionary)
     */
    @Override
    public LDAPRealmConfig createConfig ( Dictionary<String, Object> properties ) throws LDAPException {
        return LDAPRealmConfigImpl.parseConfig(properties);
    }

}
