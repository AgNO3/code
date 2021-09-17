/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 31, 2017 by mbechler
 */
package eu.agno3.runtime.net.ad.internal;


import java.io.File;
import java.io.IOException;
import java.nio.file.attribute.GroupPrincipal;
import java.util.Collection;
import java.util.Dictionary;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.kerberos.KeyTab;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.runtime.crypto.random.SecureRandomProvider;
import eu.agno3.runtime.ldap.client.LDAPClientBuilder;
import eu.agno3.runtime.ldap.client.LDAPClientFactory;
import eu.agno3.runtime.net.ad.ADConnector;
import eu.agno3.runtime.net.ad.ADException;
import eu.agno3.runtime.net.ad.ADRealm;
import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.net.krb5.KerberosRealm;
import eu.agno3.runtime.net.krb5.internal.KerberosConfig;
import eu.agno3.runtime.security.credentials.CredentialUnwrapper;
import eu.agno3.runtime.util.config.ConfigUtil;
import eu.agno3.runtime.util.threads.NamedThreadFactory;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    ADRealm.class, KerberosRealm.class, LDAPClientFactory.class
}, configurationPid = "ad.standalone", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class StandaloneADRealmImpl extends AbstractADRealmImpl implements Runnable {

    private static final Logger log = Logger.getLogger(StandaloneADRealmImpl.class);

    private ADOSInfo operatingSystem;
    private KerberosConfig kerberosConfig;
    private StandaloneADRealmConfigImpl config;
    private StandaloneADStateConfig stateConfig;
    private ScheduledExecutorService rekeyExecutor;
    private File keytab;


    @Reference
    protected void setKerberosConfig ( KerberosConfig kc ) {
        this.kerberosConfig = kc;
    }


    protected void unsetKerberosConfig ( KerberosConfig kc ) {
        if ( this.kerberosConfig == kc ) {
            this.kerberosConfig = null;
        }
    }


    @Override
    @Reference
    protected synchronized void setLDAPClientBuilder ( LDAPClientBuilder lcb ) {
        super.setLDAPClientBuilder(lcb);
    }


    @Override
    protected synchronized void unsetLDAPClientBuilder ( LDAPClientBuilder lcb ) {
        super.unsetLDAPClientBuilder(lcb);
    }


    @Reference
    @Override
    protected synchronized void setADConnector ( ADConnector adc ) {
        super.setADConnector(adc);
    }


    @Override
    protected synchronized void unsetADConnector ( ADConnector adc ) {
        super.unsetADConnector(adc);
    }


    @Reference
    @Override
    protected synchronized void setCIFSSetup ( CIFSSetup cs ) {
        super.setCIFSSetup(cs);
    }


    @Override
    protected synchronized void unsetCIFSSetup ( CIFSSetup cs ) {
        super.unsetCIFSSetup(cs);
    }


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC )
    @Override
    protected synchronized void setCredentialUnwrapper ( CredentialUnwrapper cu ) {
        super.setCredentialUnwrapper(cu);
    }


    @Override
    protected synchronized void unsetCredentialUnwrapper ( CredentialUnwrapper cu ) {
        super.unsetCredentialUnwrapper(cu);
    }


    @Reference
    @Override
    protected synchronized void setSecureRandomProvider ( SecureRandomProvider srp ) {
        super.setSecureRandomProvider(srp);
    }


    @Override
    protected synchronized void unsetSecureRandomProvider ( SecureRandomProvider srp ) {
        super.unsetSecureRandomProvider(srp);
    }


    @Reference
    protected synchronized void setStateConfig ( StandaloneADStateConfig sc ) {
        this.stateConfig = sc;
    }


    protected synchronized void unsetStateConfig ( StandaloneADStateConfig sc ) {
        if ( this.stateConfig == sc ) {
            this.stateConfig = null;
        }
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        Dictionary<String, Object> cfg = ctx.getProperties();
        String instance = ConfigUtil.parseString(cfg, "instanceId", null); //$NON-NLS-1$
        String domain = ConfigUtil.parseString(cfg, "domain", instance); //$NON-NLS-1$
        if ( StringUtils.isBlank(domain) ) {
            log.error("No domain configured"); //$NON-NLS-1$
            return;
        }

        this.operatingSystem = ADOSInfo.fromProperties(cfg);
        StandaloneADRealmConfigImpl adc = new StandaloneADRealmConfigImpl(domain, this.stateConfig.getAdStateDirectory(), cfg);
        this.config = adc;
        configureFromDomain(domain, true);
        this.kerberosConfig.ensureConfigured(adc);

        ensureJoined(domain, ctx.getProperties());
        doRekey();

        if ( this.config.isRekeyMachineAccount() ) {
            long secs = adc.getMachineRekeyInterval().getStandardSeconds();
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Starting rekey scheduler every %d seconds", secs)); //$NON-NLS-1$
            }
            this.rekeyExecutor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("ADRekeyScheduler-" + domain)); //$NON-NLS-1$
            this.rekeyExecutor.scheduleWithFixedDelay(this, secs, secs, TimeUnit.SECONDS);
        }
    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        reloadMachinePassword();
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        if ( this.rekeyExecutor != null ) {
            this.rekeyExecutor.shutdown();
            this.rekeyExecutor = null;
        }
        close();
    }


    /**
     * @param ctx
     */
    protected void ensureJoined ( String domain, Dictionary<String, Object> props ) {
        boolean wasJoined = isJoined();
        if ( !wasJoined ) {
            // try join regularly
            try {
                log.info("Joining domain " + domain); //$NON-NLS-1$
                doJoin(props);
            }
            catch (
                ADException |
                IOException e ) {
                log.error("AD join failed", e); //$NON-NLS-1$
                return;
            }
        }

        try {
            ensureJoined();
        }
        catch ( ADException e ) {
            if ( wasJoined ) {
                // try to rejoin
                log.warn("AD join no longer valid", e); //$NON-NLS-1$
                try {
                    doJoin(props);
                }
                catch (
                    ADException |
                    IOException e1 ) {
                    log.error("Attempt to re-join AD failed", e); //$NON-NLS-1$
                }
            }
            else {
                log.error("AD join failed", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run () {
        doRekey();
    }


    protected void doRekey () {
        if ( !this.config.isRekeyMachineAccount() ) {
            log.trace("Not configured for rekeying the machine account"); //$NON-NLS-1$
            return;
        }

        if ( !this.isJoined() ) {
            log.debug("Not joined"); //$NON-NLS-1$
            return;
        }

        try {
            DateTime lastChange = getMachinePasswordLastChange();
            if ( lastChange == null ) {
                log.error("Failed to determine last machine password change time, skipping rekey"); //$NON-NLS-1$
                return;
            }

            if ( log.isDebugEnabled() ) {
                log.debug("last machine account password change " + lastChange); //$NON-NLS-1$
            }

            Duration intvl = getConfig().getMachineRekeyInterval();
            if ( DateTime.now().minus(intvl).isBefore(lastChange) ) {
                log.debug("No need to rekey, max age " + intvl); //$NON-NLS-1$
                return;
            }

            log.debug("Rekey machine account now"); //$NON-NLS-1$
            rekey(null, null);
        }
        catch ( ADException e ) {
            log.error("Failed to rekey machine account", e); //$NON-NLS-1$
        }
    }


    /**
     * @param props
     * @throws ADException
     * @throws IOException
     */
    protected void doJoin ( Dictionary<String, Object> props ) throws ADException, IOException {
        GroupPrincipal realmGroup = null;
        GroupPrincipal hostGroup = null;
        if ( ConfigUtil.parseBoolean(props, "joinResetPassword", false) ) { //$NON-NLS-1$
            joinWithMachinePassword(null, realmGroup, hostGroup);
            return;
        }

        String joinMachinePassword = ConfigUtil.parseSecret(props, "joinMachinePassword", null); //$NON-NLS-1$
        if ( !StringUtils.isBlank(joinMachinePassword) ) {
            joinWithMachinePassword(joinMachinePassword.trim(), realmGroup, hostGroup);
            return;
        }

        String joinUser = ConfigUtil.parseString(props, "joinUser", null); //$NON-NLS-1$
        String joinPassword = ConfigUtil.parseSecret(props, "joinPassword", null); //$NON-NLS-1$

        if ( !StringUtils.isBlank(joinUser) && !StringUtils.isBlank(joinPassword) ) {
            join(joinUser, joinPassword, realmGroup, hostGroup);
            return;
        }

        throw new ADException("Need to join domain but don't have any join credentials configured"); //$NON-NLS-1$
    }


    private File getKeytabFile ( String keyTabId ) throws KerberosException {
        File f = this.keytab;
        if ( f == null ) {
            throw new KerberosException("Keytab not initialized"); //$NON-NLS-1$
        }
        return f;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.KerberosRealm#getKeytab(java.lang.String,
     *      javax.security.auth.kerberos.KerberosPrincipal)
     */
    @Override
    public KeyTab getKeytab ( String keyTabId, KerberosPrincipal servicePrincipal ) throws KerberosException {
        return KeyTab.getInstance(servicePrincipal, getKeytabFile(keyTabId));
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.KerberosRealm#getUnboundKeytab(java.lang.String)
     */
    @Override
    public KeyTab getUnboundKeytab ( String keyTabId ) throws KerberosException {
        return KeyTab.getUnboundInstance(getKeytabFile(keyTabId));
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.internal.AbstractADRealmImpl#getOSInfo()
     */
    @Override
    protected ADOSInfo getOSInfo () {
        return this.operatingSystem;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.internal.AbstractADRealmImpl#getPermittedETypes()
     */
    @Override
    protected Collection<Integer> getPermittedETypes () {
        return this.kerberosConfig.getPermittedETypes();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.internal.AbstractADRealmImpl#getConfig()
     */
    @Override
    protected AbstractADRealmConfigImpl getConfig () throws ADException {
        return this.config;
    }

}
