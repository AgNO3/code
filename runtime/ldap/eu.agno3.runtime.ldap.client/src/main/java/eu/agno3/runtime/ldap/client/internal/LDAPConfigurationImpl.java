/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2015 by mbechler
 */
package eu.agno3.runtime.ldap.client.internal;


import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.sasl.Sasl;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.Duration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;

import com.unboundid.ldap.sdk.Control;
import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPURL;

import eu.agno3.runtime.ldap.client.LDAPConfigUtil;
import eu.agno3.runtime.ldap.client.LDAPConfiguration;
import eu.agno3.runtime.ldap.client.LDAPConfigurationException;
import eu.agno3.runtime.net.krb5.GSSAPISubjectFactory;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = LDAPConfiguration.class, configurationPid = LDAPConfiguration.PID, configurationPolicy = ConfigurationPolicy.REQUIRE )
public class LDAPConfigurationImpl implements LDAPConfiguration {

    private static final Logger log = Logger.getLogger(LDAPConfigurationImpl.class);

    private static final String AD_TYPE = "AD"; //$NON-NLS-1$

    private String instanceId;

    private int initialPoolSize = 5;
    private boolean ignoreInitialConnectFail = true;
    private int maxPoolSize = 10;

    private List<LDAPURL> servers;

    private boolean startTls;
    private boolean ssl;
    private String tlsContextName;

    private boolean anonymous = true;
    private DN simpleBindDN;
    private String bindPassword;
    private Control[] authControls;

    private boolean useSASL;
    private String saslMech;
    private String saslAuthId;
    private String saslRealm;
    private Map<String, Object> saslOptions;

    private DN defaultBaseDN;

    private String srvRecord;
    private int srvTTL;

    private String srvDomain;

    private int connectionTimeout;
    private long responseTimeout;

    private Object serverType;

    private GSSAPISubjectFactory gssapiSubjectFactory;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        parseConfig(ctx);
    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        parseConfig(ctx);
    }


    /**
     * @param ctx
     */
    private void parseConfig ( ComponentContext ctx ) {
        Dictionary<String, Object> properties = ctx.getProperties();

        dumpProperties(properties);

        String instanceAttr = (String) properties.get(LDAPConfigurationTracker.INSTANCE_ID);
        if ( StringUtils.isBlank(instanceAttr) ) {
            throw new ComponentException("instanceId is required"); //$NON-NLS-1$
        }
        this.instanceId = instanceAttr.trim();

        this.serverType = properties.get("serverType"); //$NON-NLS-1$

        try {
            parseTimeouts(properties);
            parsePoolConfig(properties);
            parseServers(properties);
            parseTlsConfig(properties);
            parseAuthConfig(properties);
            parseSASLConfig(properties);

            String baseDn = (String) properties.get("baseDN"); //$NON-NLS-1$
            if ( !StringUtils.isBlank(baseDn) ) {
                this.defaultBaseDN = new DN(baseDn.trim());
            }
            else if ( AD_TYPE.equals(this.serverType) && !StringUtils.isBlank(this.srvDomain) ) {
                this.defaultBaseDN = LDAPConfigUtil.getActiveDirectoryDefaultBase(this.srvDomain);
            }
        }
        catch (
            LDAPException |
            LDAPConfigurationException |
            IOException e ) {
            throw new ComponentException("Failed to parse ldap config", e); //$NON-NLS-1$
        }

        if ( ( this.ssl || this.startTls ) && StringUtils.isBlank(this.tlsContextName) ) {
            this.tlsContextName = "ldap"; //$NON-NLS-1$
        }
    }


    /**
     * @param properties
     */
    private void parseTimeouts ( Dictionary<String, Object> properties ) {
        this.connectionTimeout = (int) ConfigUtil.parseDuration(properties, "connectTimeout", Duration.standardSeconds(10)).getMillis(); //$NON-NLS-1$
        this.responseTimeout = (int) ConfigUtil.parseDuration(properties, "responseTimeout", Duration.standardSeconds(30)).getMillis(); //$NON-NLS-1$

    }


    /**
     * @param properties
     */
    private void parseSASLConfig ( Dictionary<String, Object> properties ) {

        String saslAttr = (String) properties.get("useSASL"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(saslAttr) && Boolean.parseBoolean(saslAttr.trim()) ) {
            this.useSASL = true;
            this.anonymous = false;
        }

        if ( !this.useSASL ) {
            return;
        }

        String saslMechAttr = (String) properties.get("saslMechanism"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(saslMechAttr) ) {
            this.saslMech = saslMechAttr.trim();
        }

        String saslRealmAttr = (String) properties.get("saslRealm"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(saslRealmAttr) ) {
            this.saslRealm = saslRealmAttr.trim();
        }

        String saslAuthIdAttr = (String) properties.get("saslAuthId"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(saslAuthIdAttr) ) {
            this.saslAuthId = saslAuthIdAttr.trim();
        }

        parseSASLOptions(this.saslMech, properties);
    }


    /**
     * @param properties
     */
    private void parseSASLOptions ( String mech, Dictionary<String, Object> properties ) {
        final String prefix = "sasl."; //$NON-NLS-1$
        Map<String, Object> opts = new HashMap<>(LDAPConfigUtil.getDefaultSASLOptions(mech));
        Enumeration<String> keys = properties.keys();

        if ( LDAPConfigUtil.mechanismSupportsQOP(mech) ) {
            String saslQOP = (String) properties.get("saslQOP"); //$NON-NLS-1$
            if ( !StringUtils.isBlank(saslQOP) ) {
                opts.put(Sasl.QOP, saslQOP);
            }
            else {
                opts.put(Sasl.QOP, "auth-conf"); //$NON-NLS-1$
            }
        }

        while ( keys.hasMoreElements() ) {
            String key = keys.nextElement();
            if ( key.startsWith(prefix) ) {
                opts.put(key.substring(prefix.length() + 1), properties.get(key));
            }
        }

        this.saslOptions = opts;

        if ( "GSSAPI".equals(mech) && !StringUtils.isBlank(this.saslAuthId) //$NON-NLS-1$
                && !StringUtils.isBlank(this.bindPassword) ) {
            this.gssapiSubjectFactory = LDAPConfigUtil.makeClientUserPasswordGSSAPIFactory(this.saslAuthId, this.srvDomain, this.bindPassword);
        }

    }


    /**
     * @param properties
     * @throws LDAPException
     * @throws IOException
     */
    private void parseAuthConfig ( Dictionary<String, Object> properties ) throws LDAPException, IOException {

        String bindDnAttr = (String) properties.get("bindDN"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(bindDnAttr) ) {
            this.simpleBindDN = new DN(bindDnAttr.trim());
            this.anonymous = false;
        }
        else {
            this.simpleBindDN = null;
            this.anonymous = true;
        }

        this.bindPassword = ConfigUtil.parseSecret(properties, "bindPassword", null); //$NON-NLS-1$
    }


    /**
     * @param properties
     */
    private void parsePoolConfig ( Dictionary<String, Object> properties ) {
        String initialPoolSizeAttr = (String) properties.get("initialPoolSize"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(initialPoolSizeAttr) ) {
            this.initialPoolSize = Integer.parseInt(initialPoolSizeAttr.trim());
        }

        String maxPoolSizeAttr = (String) properties.get("maxPoolSize"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(maxPoolSizeAttr) ) {
            this.maxPoolSize = Integer.parseInt(maxPoolSizeAttr.trim());
        }

        String ignoreInitialConnFail = (String) properties.get("ignoreInitialConnectFail"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(ignoreInitialConnFail) && !Boolean.parseBoolean(ignoreInitialConnFail) ) {
            this.ignoreInitialConnectFail = false;
        }
    }


    /**
     * @param properties
     */
    private void parseTlsConfig ( Dictionary<String, Object> properties ) {
        String tlsContextSpec = (String) properties.get("tlsContext"); //$NON-NLS-1$

        if ( !StringUtils.isBlank(tlsContextSpec) ) {
            this.tlsContextName = tlsContextSpec.trim();
        }

        this.ssl = ConfigUtil.parseBoolean(properties, "useSSL", false); //$NON-NLS-1$
        this.startTls = ConfigUtil.parseBoolean(properties, "useStartTLS", false); //$NON-NLS-1$
    }


    /**
     * @param properties
     * @throws LDAPConfigurationException
     */
    private void parseServers ( Dictionary<String, Object> properties ) throws LDAPConfigurationException {

        String srvSpec = (String) properties.get("srvDomain"); //$NON-NLS-1$

        if ( !StringUtils.isBlank(srvSpec) ) {
            this.srvDomain = srvSpec.trim();
            this.srvRecord = ConfigUtil.parseString(
                properties,
                "srvRecord", //$NON-NLS-1$
                AD_TYPE.equals(this.serverType) ? LDAPConfigUtil.SRV_SERVICE_AD : LDAPConfigUtil.SRV_SERVICE_DEFAULT); // $NON-NLS-1$
            this.srvTTL = ConfigUtil.parseInt(properties, "srvTTL", 0); //$NON-NLS-1$
            return;
        }

        this.srvRecord = null;
        String serverSpec = (String) properties.get("servers"); //$NON-NLS-1$

        if ( StringUtils.isBlank(serverSpec) ) {
            throw new LDAPConfigurationException("Invalid configuration, servers is required"); //$NON-NLS-1$
        }

        String[] specs = StringUtils.split(serverSpec, ';');
        List<LDAPURL> servs = LDAPConfigUtil.parseServerList(specs, this.ssl, this.startTls);

        String scheme = servs.get(0).getScheme();
        if ( "ldaps".equals(scheme) ) { //$NON-NLS-1$
            this.ssl = true;
        }

        if ( "ldap+tls".equals(scheme) ) { //$NON-NLS-1$
            this.startTls = true;
        }

        this.servers = servs;

    }


    /**
     * @param properties
     */
    private static void dumpProperties ( Dictionary<String, Object> properties ) {
        if ( log.isDebugEnabled() ) {
            log.debug("LDAP configuration:"); //$NON-NLS-1$
            Enumeration<String> keys = properties.keys();

            while ( keys.hasMoreElements() ) {
                String key = keys.nextElement();
                log.debug(String.format("%s: %s", key, properties.get(key))); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getInitialPoolSize()
     */
    @Override
    public int getInitialPoolSize () {
        return this.initialPoolSize;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#ignoreInitialConnectFail()
     */
    @Override
    public boolean ignoreInitialConnectFail () {
        return this.ignoreInitialConnectFail;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getMaxPoolSize()
     */
    @Override
    public int getMaxPoolSize () {
        return this.maxPoolSize;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#useSASLAuth()
     */
    @Override
    public boolean useSASLAuth () {
        return this.useSASL;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#isAnonymous()
     */
    @Override
    public boolean isAnonymous () {
        return this.anonymous;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getBindDN()
     */
    @Override
    public DN getBindDN () {
        return this.simpleBindDN;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getBindPassword()
     */
    @Override
    public String getBindPassword () {
        return this.bindPassword;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getAuthControls()
     */
    @Override
    public Control[] getAuthControls () {
        return this.authControls;
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
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getSASLMechanism()
     */
    @Override
    public String getSASLMechanism () {
        return this.saslMech;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getSASLAuthID()
     */
    @Override
    public String getSASLAuthID () {
        return this.saslAuthId;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getSASLRealm()
     */
    @Override
    public String getSASLRealm () {
        return this.saslRealm;
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
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getSRVRecord()
     */
    @Override
    public String getSRVRecord () {
        return this.srvRecord;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getSRVDomain()
     */
    @Override
    public String getSRVDomain () {
        return this.srvDomain;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getSRVTTL()
     */
    @Override
    public int getSRVTTL () {
        return this.srvTTL;
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
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getTLSContextName()
     */
    @Override
    public String getTLSContextName () {
        return this.tlsContextName;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#useStartTLS()
     */
    @Override
    public boolean useStartTLS () {
        return this.startTls;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#useSSL()
     */
    @Override
    public boolean useSSL () {
        return this.ssl;
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
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getDefaultBaseDN()
     */
    @Override
    public DN getDefaultBaseDN () {
        return this.defaultBaseDN;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getConnectionTimeout()
     */
    @Override
    public long getConnectionTimeout () {
        return this.connectionTimeout;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getResponseTimeout()
     */
    @Override
    public long getResponseTimeout () {
        return this.responseTimeout;
    }
}
