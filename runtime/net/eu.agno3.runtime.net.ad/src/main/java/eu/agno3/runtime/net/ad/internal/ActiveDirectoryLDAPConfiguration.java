/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.04.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.internal;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.security.sasl.Sasl;

import org.apache.commons.lang3.StringUtils;

import com.unboundid.ldap.sdk.Control;
import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.LDAPURL;
import com.unboundid.ldap.sdk.RDN;

import eu.agno3.runtime.ldap.client.LDAPConfiguration;
import eu.agno3.runtime.net.ad.ADException;
import eu.agno3.runtime.net.ad.ADRealm;
import eu.agno3.runtime.net.krb5.GSSAPISubjectFactory;
import eu.agno3.runtime.net.krb5.KerberosException;


/**
 * @author mbechler
 *
 */
public class ActiveDirectoryLDAPConfiguration implements LDAPConfiguration {

    private ADRealm realm;
    private DN defaultBaseDN;
    private int initialPoolSize = 1;
    private int maxPoolSize = 4;
    private int srvTTL;
    private String srvRecord;
    private GSSAPISubjectFactory gssapiSubjectFactory;
    private String srvDomain;

    private final long connectTimeout = 10 * 1000;
    private final long responseTimeout = 30 * 1000;


    /**
     * @param realm
     * @throws ADException
     * @throws KerberosException
     * 
     */
    public ActiveDirectoryLDAPConfiguration ( ADRealm realm ) throws KerberosException, ADException {
        this(realm, new ADGSSAPISubjectFactory(realm));
    }


    /**
     * @param realm
     * @param subjectFactory
     * @throws ADException
     * @throws KerberosException
     * 
     */
    public ActiveDirectoryLDAPConfiguration ( ADRealm realm, GSSAPISubjectFactory subjectFactory ) throws KerberosException, ADException {
        this.realm = realm;

        String[] dcs = StringUtils.split(realm.getDomainName(), '.');
        List<RDN> rdns = new LinkedList<>();
        for ( String dc : dcs ) {
            rdns.add(new RDN("dc", dc)); //$NON-NLS-1$
        }
        this.defaultBaseDN = new DN(rdns);
        this.srvRecord = "_ldap._tcp.dc._msdcs"; //$NON-NLS-1$
        this.srvDomain = realm.getDomainName();
        this.gssapiSubjectFactory = subjectFactory;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getInstanceId()
     */
    @Override
    public String getInstanceId () {
        return this.realm.getDomainName();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getConnectionTimeout()
     */
    @Override
    public long getConnectionTimeout () {
        return this.connectTimeout;
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
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getSRVRecord()
     */
    @Override
    public String getSRVRecord () {
        return this.srvRecord;
    }


    /**
     * 
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
        return null;
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
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#useSSL()
     */
    @Override
    public boolean useSSL () {
        return false;
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
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getInitialPoolSize()
     */
    @Override
    public int getInitialPoolSize () {
        return this.initialPoolSize;
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
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#ignoreInitialConnectFail()
     */
    @Override
    public boolean ignoreInitialConnectFail () {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#isAnonymous()
     */
    @Override
    public boolean isAnonymous () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getBindDN()
     */
    @Override
    public DN getBindDN () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getBindPassword()
     */
    @Override
    public String getBindPassword () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getAuthControls()
     */
    @Override
    public Control[] getAuthControls () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#useSASLAuth()
     */
    @Override
    public boolean useSASLAuth () {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getSASLMechanism()
     */
    @Override
    public String getSASLMechanism () {
        return "GSSAPI"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getSASLAuthID()
     */
    @Override
    public String getSASLAuthID () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getSASLRealm()
     */
    @Override
    public String getSASLRealm () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPConfiguration#getSASLOptions()
     */
    @Override
    public Map<String, Object> getSASLOptions () {
        Map<String, Object> opts = new HashMap<>();
        opts.put(Sasl.QOP, "auth-conf"); //$NON-NLS-1$
        return opts;
    }

}
