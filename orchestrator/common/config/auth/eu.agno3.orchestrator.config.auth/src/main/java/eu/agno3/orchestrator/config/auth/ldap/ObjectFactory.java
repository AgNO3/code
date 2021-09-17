/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth.ldap;


/**
 * @author mbechler
 *
 */
public class ObjectFactory {

    /**
     * 
     * @return default impl
     */
    public LDAPAuthenticatorConfig makeLDAPAuthenticatorConfig () {
        return new LDAPAuthenticatorConfigImpl();
    }


    /**
     * 
     * @return default impl
     */
    public LDAPAuthSchemaConfig makeLDAPAuthSchemaConfig () {
        return new LDAPAuthSchemaConfigImpl();
    }


    /**
     * 
     * @return default impl
     */
    public LDAPSyncOptions makeLDAPSyncOptions () {
        return new LDAPSyncOptionsImpl();
    }
}
