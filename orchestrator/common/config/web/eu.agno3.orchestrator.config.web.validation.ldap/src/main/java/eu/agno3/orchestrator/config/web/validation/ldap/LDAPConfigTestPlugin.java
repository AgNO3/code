/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.01.2017 by mbechler
 */
package eu.agno3.orchestrator.config.web.validation.ldap;


import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.LDAPException;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestAsyncHandler;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPluginAsync;
import eu.agno3.orchestrator.config.model.validation.ConfigTestParams;
import eu.agno3.orchestrator.config.model.validation.ConfigTestResult;
import eu.agno3.orchestrator.config.web.LDAPConfiguration;
import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.ldap.client.LDAPClient;
import eu.agno3.runtime.ldap.client.LDAPConfigurationException;


/**
 * @author mbechler
 *
 */
public interface LDAPConfigTestPlugin extends ConfigTestPluginAsync<LDAPConfiguration> {

    /**
     * @param config
     * @param params
     * @param r
     * @param h
     * @return an LDAP client if the connection succeeds
     * @throws ModelServiceException
     */
    LDAPClient getConnection ( LDAPConfiguration config, ConfigTestParams params, ConfigTestResult r, ConfigTestAsyncHandler h )
            throws ModelServiceException;


    /**
     * @param config
     * @param bindDN
     * @param password
     * @throws LDAPException
     * @throws LDAPConfigurationException
     * @throws CryptoException
     */
    void tryBind ( LDAPConfiguration config, DN bindDN, String password ) throws LDAPException, LDAPConfigurationException, CryptoException;

}
