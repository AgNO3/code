/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 25, 2016 by mbechler
 */
package eu.agno3.orchestrator.config.web.validation.ldap.internal;


import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import javax.net.ssl.SSLException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.ExtendedResult;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.RootDSE;
import com.unboundid.ldap.sdk.SimpleBindRequest;
import com.unboundid.ldap.sdk.extensions.WhoAmIExtendedRequest;
import com.unboundid.ldap.sdk.extensions.WhoAmIExtendedResult;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestAsyncHandler;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestContext;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPlugin;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPluginAsync;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPluginRunOn;
import eu.agno3.orchestrator.config.model.validation.ConfigTestParams;
import eu.agno3.orchestrator.config.model.validation.ConfigTestResult;
import eu.agno3.orchestrator.config.model.validation.ConfigTestState;
import eu.agno3.orchestrator.config.web.LDAPAuthType;
import eu.agno3.orchestrator.config.web.LDAPConfiguration;
import eu.agno3.orchestrator.config.web.validation.KRBValidationUtils;
import eu.agno3.orchestrator.config.web.validation.SSLEndpointConfigTestFactory;
import eu.agno3.orchestrator.config.web.validation.SocketValidationUtils;
import eu.agno3.orchestrator.config.web.validation.TLSTestContext;
import eu.agno3.orchestrator.config.web.validation.TLSValidationUtils;
import eu.agno3.orchestrator.config.web.validation.ldap.LDAPConfigTestPlugin;
import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.ldap.client.LDAPClient;
import eu.agno3.runtime.ldap.client.LDAPClientBuilder;
import eu.agno3.runtime.ldap.client.LDAPConfigurationException;
import eu.agno3.runtime.net.krb5.KerberosException;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    LDAPConfigTestPlugin.class, ConfigTestPlugin.class
} )
public class LDAPClientConfigurationTestPlugin implements LDAPConfigTestPlugin, ConfigTestPluginAsync<LDAPConfiguration> {

    private static final Logger log = Logger.getLogger(LDAPClientConfigurationTestPlugin.class);

    private SSLEndpointConfigTestFactory sslEndpointFactory;
    private LDAPClientBuilder ldapClientBuilder;


    @Override
    public Set<ConfigTestPluginRunOn> getRunOn () {
        return Collections.singleton(ConfigTestPluginRunOn.SERVER);
    }


    @Reference
    protected synchronized void setLDAPClientBuilder ( LDAPClientBuilder lcf ) {
        this.ldapClientBuilder = lcf;
    }


    protected synchronized void unsetLDAPClientBuilder ( LDAPClientBuilder lcf ) {
        if ( this.ldapClientBuilder == lcf ) {
            this.ldapClientBuilder = null;
        }
    }


    @Reference
    protected synchronized void setSSLEndpointFactory ( SSLEndpointConfigTestFactory sectf ) {
        this.sslEndpointFactory = sectf;
    }


    protected synchronized void unsetSSLEndpointFactory ( SSLEndpointConfigTestFactory sectf ) {
        if ( this.sslEndpointFactory == sectf ) {
            this.sslEndpointFactory = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPlugin#getTargetType()
     */
    @Override
    public Class<LDAPConfiguration> getTargetType () {
        return LDAPConfiguration.class;
    }


    @Override
    public ConfigTestResult testAsync ( LDAPConfiguration config, ConfigTestContext ctx, ConfigTestParams params, ConfigTestResult r,
            ConfigTestAsyncHandler h ) throws ModelServiceException {
        log.debug("Running LDAP test"); //$NON-NLS-1$

        @SuppressWarnings ( "resource" )
        LDAPClient c = getConnection(config, params, r, h);
        if ( c != null ) {
            c.close();
            return r.state(ConfigTestState.SUCCESS);
        }
        return r.state(ConfigTestState.FAILURE);
    }


    @Override
    public LDAPClient getConnection ( LDAPConfiguration config, ConfigTestParams params, ConfigTestResult r, ConfigTestAsyncHandler h )
            throws ModelServiceException {
        String did = config.getSrvDomain() != null ? config.getSrvDomain() : config.getServers().toString();

        TLSTestContext tc;
        try {
            tc = this.sslEndpointFactory.adaptSSLClient(config.getSslClientConfiguration());
            TLSValidationUtils.checkTruststoreUsage(tc, r);
        }
        catch ( CryptoException e ) {
            log.debug("Failed to create ssl parameters", e); //$NON-NLS-1$
            r.error("SSL_CONFIG", did, e.getMessage()); //$NON-NLS-1$
            return null;
        }

        try {
            LDAPConfigAdapter cfg = new LDAPConfigAdapter(config);
            h.update(r.info("CONNECTING", did)); //$NON-NLS-1$
            LDAPClient c = this.ldapClientBuilder.createSingleConnection(cfg, tc.getContext());
            try {

                checkRootDSE(r, c);
                checkAuth(config, r, c);

                try {
                    DN baseDN = c.getBaseDN();
                    if ( log.isDebugEnabled() ) {
                        log.debug("BaseDN: " + baseDN); //$NON-NLS-1$
                    }

                    r.info("LDAP_BASEDN_FOUND", baseDN.toString()); //$NON-NLS-1$

                    try {
                        c.getEntry(baseDN.toString());
                        r.info("LDAP_BASEDN_GET_OK"); //$NON-NLS-1$
                    }
                    catch ( LDAPException e ) {
                        log.debug("Failed to get entry of baseDN", e); //$NON-NLS-1$
                        r.warn("FAIL_LDAP_BASEDN_GET", e.getMessage()); //$NON-NLS-1$
                    }

                    return c;
                }
                catch ( LDAPException e ) {
                    log.debug("Failed to get base DN", e); //$NON-NLS-1$
                    r.error("FAIL_LDAP_BASEDN_MISSING"); // $NON-NLS-1$ //$NON-NLS-1$
                    c.close();
                    return null;
                }
            }
            catch ( Exception e ) {
                c.close();
                throw e;
            }
        }
        catch (
            LDAPException |
            CryptoException |
            LDAPConfigurationException e ) {
            log.debug("Caught exception", e); //$NON-NLS-1$

            if ( e.getCause() instanceof IOException && e.getCause().getCause() instanceof LDAPException
                    && e.getCause().getCause().getCause() instanceof IOException ) {
                handleIOException(r, tc, did, (IOException) e.getCause().getCause().getCause());
            }
            else if ( e.getCause() instanceof IOException ) {
                handleIOException(r, tc, did, (IOException) e.getCause());
            }
            else if ( e.getCause() instanceof KerberosException ) {
                KRBValidationUtils.handleException(r, (KerberosException) e.getCause());
            }
            else if ( e instanceof LDAPException ) {
                LDAPException le = (LDAPException) e;
                if ( le.getCause() instanceof LDAPException ) {
                    le = (LDAPException) le.getCause();
                }

                if ( le.getCause() instanceof IOException ) {
                    handleIOException(r, tc, did, (IOException) le.getCause());
                }
                else if ( le.getResultCode() == ResultCode.INVALID_CREDENTIALS ) {
                    r.error("FAIL_LDAP_AUTH", le.getResultString()); //$NON-NLS-1$
                }
                else {
                    r.error("FAIL_LDAP_UNKNOWN", e.getMessage()); //$NON-NLS-1$
                }
            }
            else {
                r.error("FAIL_LDAP_UNKNOWN", e.getMessage()); //$NON-NLS-1$
            }
            return null;
        }
    }


    @Override
    public void tryBind ( LDAPConfiguration config, DN bindDN, String password ) throws LDAPException, LDAPConfigurationException, CryptoException {
        TLSTestContext tc;
        try {
            tc = this.sslEndpointFactory.adaptSSLClient(config.getSslClientConfiguration());
        }
        catch ( CryptoException e ) {
            log.debug("Failed to create ssl parameters", e); //$NON-NLS-1$
            return;
        }

        LDAPConfigAdapter cfg = new LDAPConfigAdapter(config);
        SimpleBindRequest bindReq = new SimpleBindRequest(bindDN, password);
        bindReq.setResponseTimeoutMillis(config.getSocketTimeout().getMillis());
        try ( LDAPClient cl = this.ldapClientBuilder.createSingleConnection(cfg, tc.getContext(), bindReq) ) {}
    }


    /**
     * @param r
     * @param tc
     * @param did
     * @param e
     */
    private static void handleIOException ( ConfigTestResult r, TLSTestContext tc, String did, IOException e ) {
        if ( e instanceof SSLException ) {
            TLSValidationUtils.handleTLSException((SSLException) e, r, tc);
        }
        else {
            SocketValidationUtils.handleIOException(e, r, did);
        }
    }


    /**
     * @param r
     * @param c
     */
    private static void checkRootDSE ( ConfigTestResult r, LDAPClient c ) {
        try {
            RootDSE rootDse = c.getCachedRootDSE();

            int[] versions = rootDse.getSupportedLDAPVersions();
            String[] saslMechs = rootDse.getSupportedSASLMechanismNames();

            r.info(
                "LDAP_ROOTDSE_FOUND", //$NON-NLS-1$
                rootDse.getVendorName() != null ? rootDse.getVendorName() : StringUtils.EMPTY,
                rootDse.getVendorVersion() != null ? rootDse.getVendorVersion() : StringUtils.EMPTY,
                versions != null ? Arrays.toString(versions) : StringUtils.EMPTY,
                saslMechs != null ? Arrays.toString(saslMechs) : "[]"); //$NON-NLS-1$

            String[] capabilities = rootDse.getAttributeValues("supportedCapabilities"); //$NON-NLS-1$
            if ( capabilities != null ) {
                log.debug("Capabilities " + Arrays.toString(capabilities)); //$NON-NLS-1$
            }

        }
        catch ( LDAPException e ) {
            r.warn("LDAP_ROOTDSE_NOTFOUND"); //$NON-NLS-1$
            log.debug("Failed to get root DSE"); //$NON-NLS-1$
        }
    }


    /**
     * @param config
     * @param r
     * @param c
     */
    private static void checkAuth ( LDAPConfiguration config, ConfigTestResult r, LDAPClient c ) {
        WhoAmIExtendedResult runWhoAmI = runWhoAmI(c);
        if ( !runWhoAmI.hasValue() ) {
            r.info("LDAP_AUTH_NO_WHOAMI"); //$NON-NLS-1$
        }
        else {
            String authz = runWhoAmI.getAuthorizationID();

            if ( authz.equals("") || //$NON-NLS-1$
                    authz.equals("dn:") ) { //$NON-NLS-1$

                if ( config.getAuthType() == LDAPAuthType.ANONYMOUS ) {
                    r.info("LDAP_AUTH_ANONYMOUS"); //$NON-NLS-1$
                }
                else {
                    r.error("LDAP_AUTH_ANONYMOUS_WRONG"); //$NON-NLS-1$
                }
            }
            else {
                r.info("LDAP_AUTH_AS", authz); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param c
     * @return
     */
    private static WhoAmIExtendedResult runWhoAmI ( LDAPClient c ) {
        WhoAmIExtendedRequest er = new WhoAmIExtendedRequest();
        WhoAmIExtendedResult resp;
        try {
            resp = (WhoAmIExtendedResult) c.processExtendedOperation(er);
        }
        catch ( LDAPException e ) {
            log.debug("Who am I failed", e); //$NON-NLS-1$
            resp = new WhoAmIExtendedResult(new ExtendedResult(e));
        }

        return resp;
    }

}
