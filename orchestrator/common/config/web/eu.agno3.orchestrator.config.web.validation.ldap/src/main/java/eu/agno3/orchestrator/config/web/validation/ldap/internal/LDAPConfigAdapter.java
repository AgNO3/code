/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.01.2017 by mbechler
 */
package eu.agno3.orchestrator.config.web.validation.ldap.internal;


import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.security.sasl.Sasl;

import org.apache.commons.lang3.StringUtils;

import com.unboundid.ldap.sdk.Control;
import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPURL;

import eu.agno3.orchestrator.config.web.LDAPAuthType;
import eu.agno3.orchestrator.config.web.LDAPServerType;
import eu.agno3.orchestrator.config.web.SSLClientMode;
import eu.agno3.runtime.ldap.client.LDAPConfigUtil;
import eu.agno3.runtime.ldap.client.LDAPConfiguration;
import eu.agno3.runtime.ldap.client.LDAPConfigurationException;
import eu.agno3.runtime.net.krb5.GSSAPISubjectFactory;


/**
 * @author mbechler
 *
 */
public class LDAPConfigAdapter implements LDAPConfiguration {

    private eu.agno3.orchestrator.config.web.LDAPConfiguration config;
    private String instanceId;
    private DN bindDn;
    private DN baseDn;
    private List<LDAPURL> servers;
    private String srvRecord;
    private GSSAPISubjectFactory gssapiSubjectFactory;
    private Map<String, Object> saslOptions;


    /**
     * @param config
     * @throws LDAPException
     * @throws LDAPConfigurationException
     */
    public LDAPConfigAdapter ( eu.agno3.orchestrator.config.web.LDAPConfiguration config ) throws LDAPException, LDAPConfigurationException {
        this.config = config;
        this.instanceId = "test-" + UUID.randomUUID(); //$NON-NLS-1$
        this.bindDn = this.config.getBindDN() != null ? new DN(this.config.getBindDN()) : null;
        this.baseDn = this.config.getBaseDN() != null ? new DN(this.config.getBaseDN()) : null;

        if ( config.getSrvDomain() == null ) {
            this.servers = LDAPConfigUtil.parseServerList(
                config.getServers(),
                config.getSslClientMode() == SSLClientMode.SSL,
                config.getSslClientMode() == SSLClientMode.REQUIRE_STARTTLS || config.getSslClientMode() == SSLClientMode.TRY_STARTTLS);
        }

        if ( config.getServerType() == LDAPServerType.PLAIN ) {
            this.srvRecord = LDAPConfigUtil.SRV_SERVICE_DEFAULT;
        }
        else if ( config.getServerType() == LDAPServerType.AD ) {
            if ( this.baseDn == null ) {
                this.baseDn = LDAPConfigUtil.getActiveDirectoryDefaultBase(config.getSrvDomain());
            }
            this.srvRecord = LDAPConfigUtil.SRV_SERVICE_AD;
        }

        if ( config.getAuthType() == LDAPAuthType.SASL ) {

            this.saslOptions = makeSASLOptions(config);

            if ( "GSSAPI".equals(config.getSaslMechanism()) && !StringUtils.isBlank(config.getSaslUsername()) //$NON-NLS-1$
                    && !StringUtils.isBlank(config.getPassword()) ) {
                this.gssapiSubjectFactory = LDAPConfigUtil
                        .makeClientUserPasswordGSSAPIFactory(config.getSaslUsername(), config.getSrvDomain(), config.getPassword());
            }
        }
        else {
            this.saslOptions = Collections.EMPTY_MAP;
        }
    }


    /**
     * @param cfg
     * @return
     */
    Map<String, Object> makeSASLOptions ( eu.agno3.orchestrator.config.web.LDAPConfiguration cfg ) {
        Map<String, Object> saslOpts = new HashMap<>(LDAPConfigUtil.getDefaultSASLOptions(cfg.getSaslMechanism()));
        if ( LDAPConfigUtil.mechanismSupportsQOP(cfg.getSaslMechanism()) ) {
            if ( cfg.getSslClientMode() == SSLClientMode.DISABLE && cfg.getSaslQOP() != null ) {
                switch ( cfg.getSaslQOP() ) {
                case INTEGRITY:
                    saslOpts.put(Sasl.QOP, "auth-int"); //$NON-NLS-1$
                    break;
                case AUTH:
                    saslOpts.put(Sasl.QOP, "auth"); //$NON-NLS-1$
                default:
                case CONF:
                    saslOpts.put(Sasl.QOP, "auth-conf"); //$NON-NLS-1$
                    break;
                }
            }
            else {
                saslOpts.put(Sasl.QOP, "auth-conf"); //$NON-NLS-1$
            }
        }
        return saslOpts;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getInstanceId()
     */
    @Override
    public String getInstanceId () {
        return this.instanceId;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getSRVRecord()
     */
    @Override
    public String getSRVRecord () {
        return this.srvRecord; // $NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getSRVTTL()
     */
    @Override
    public int getSRVTTL () {
        return 0;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getDefaultBaseDN()
     */
    @Override
    public DN getDefaultBaseDN () {
        return this.baseDn;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getLDAPServers()
     */
    @Override
    public List<LDAPURL> getLDAPServers () {
        return this.servers;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getSRVDomain()
     */
    @Override
    public String getSRVDomain () {
        return this.config.getSrvDomain();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getTLSContextName()
     */
    @Override
    public String getTLSContextName () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#useStartTLS()
     */
    @Override
    public boolean useStartTLS () {
        return this.config.getSslClientMode() == SSLClientMode.REQUIRE_STARTTLS || this.config.getSslClientMode() == SSLClientMode.TRY_STARTTLS;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#useSSL()
     */
    @Override
    public boolean useSSL () {
        return this.config.getSslClientMode() == SSLClientMode.SSL;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getInitialPoolSize()
     */
    @Override
    public int getInitialPoolSize () {
        return 0;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getMaxPoolSize()
     */
    @Override
    public int getMaxPoolSize () {
        return 0;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#ignoreInitialConnectFail()
     */
    @Override
    public boolean ignoreInitialConnectFail () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#isAnonymous()
     */
    @Override
    public boolean isAnonymous () {
        return this.config.getAuthType() == LDAPAuthType.ANONYMOUS;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getBindDN()
     */
    @Override
    public DN getBindDN () {
        return this.bindDn;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getBindPassword()
     */
    @Override
    public String getBindPassword () {
        return this.config.getPassword();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getAuthControls()
     */
    @Override
    public Control[] getAuthControls () {
        return new Control[0];
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getGSSAPISubjectFactory()
     */
    @Override
    public GSSAPISubjectFactory getGSSAPISubjectFactory () {
        return this.gssapiSubjectFactory;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#useSASLAuth()
     */
    @Override
    public boolean useSASLAuth () {
        return this.config.getAuthType() == LDAPAuthType.SASL;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getSASLMechanism()
     */
    @Override
    public String getSASLMechanism () {
        return this.config.getSaslMechanism();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getSASLAuthID()
     */
    @Override
    public String getSASLAuthID () {
        return this.config.getSaslUsername();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getSASLRealm()
     */
    @Override
    public String getSASLRealm () {
        return this.config.getSaslRealm();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getSASLOptions()
     */
    @Override
    public Map<String, Object> getSASLOptions () {
        return this.saslOptions;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getResponseTimeout()
     */
    @Override
    public long getResponseTimeout () {
        return this.config.getSocketTimeout().getMillis();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getConnectionTimeout()
     */
    @Override
    public long getConnectionTimeout () {
        return this.config.getSocketTimeout().getMillis();
    }

}
