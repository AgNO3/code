/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.03.2015 by mbechler
 */
package eu.agno3.fileshare.service.ldap.internal;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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

import com.unboundid.ldap.sdk.LDAPException;

import eu.agno3.fileshare.service.api.internal.DefaultServiceContext;
import eu.agno3.fileshare.service.api.internal.GroupServiceInternal;
import eu.agno3.fileshare.service.api.internal.UserServiceInternal;
import eu.agno3.fileshare.service.config.QuotaConfiguration;
import eu.agno3.fileshare.service.config.SecurityPolicyConfiguration;
import eu.agno3.fileshare.service.config.UserConfiguration;
import eu.agno3.runtime.db.orm.EntityTransactionException;
import eu.agno3.runtime.ldap.client.LDAPClientFactory;
import eu.agno3.runtime.security.UserMapper;
import eu.agno3.runtime.security.ldap.LDAPRealmConfig;
import eu.agno3.runtime.security.ldap.LDAPRealmConfigFactory;
import eu.agno3.runtime.security.ldap.LDAPUserSynchronizer;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component (
    service = FileshareLDAPSynchronizer.class,
    immediate = true,
    configurationPid = FileshareLDAPSynchronizer.PID,
    configurationPolicy = ConfigurationPolicy.REQUIRE )
public class FileshareLDAPSynchronizer implements Runnable {

    /**
     * 
     */
    public static final String PID = "ldap.sync"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(FileshareLDAPSynchronizer.class);

    private LDAPUserSynchronizer ldapSync;
    private DefaultServiceContext ctx;
    private LDAPRealmConfig realmConfig;
    private LDAPRealmConfigFactory realmConfigFactory;
    private LDAPClientFactory clientFactory;
    private String realm;
    private DateTime lastRun;
    private UserMapper userMapper;
    private UserServiceInternal userService;
    private GroupServiceInternal groupService;
    private ScheduledExecutorService executor;

    private boolean runFullSync;

    private ScheduledFuture<?> syncRunner;
    private Duration syncInterval;


    @Reference
    protected synchronized void setLDAPUserSynchronizer ( LDAPUserSynchronizer ls ) {
        this.ldapSync = ls;
    }


    protected synchronized void unsetLDAPUserSynchronizer ( LDAPUserSynchronizer ls ) {
        if ( this.ldapSync == ls ) {
            this.ldapSync = null;
        }
    }


    @Reference
    protected synchronized void setServiceContext ( DefaultServiceContext sctx ) {
        this.ctx = sctx;
    }


    protected synchronized void unsetServiceContext ( DefaultServiceContext sctx ) {
        if ( this.ctx == sctx ) {
            this.ctx = null;
        }
    }


    @Reference
    protected synchronized void setUserMapper ( UserMapper um ) {
        this.userMapper = um;
    }


    protected synchronized void unsetUserMapper ( UserMapper um ) {
        if ( this.userMapper == um ) {
            this.userMapper = null;
        }
    }


    @Reference
    protected synchronized void setLdapClientFactory ( LDAPClientFactory lcf ) {
        this.clientFactory = lcf;
    }


    protected synchronized void unsetLdapClientFactory ( LDAPClientFactory lcf ) {
        if ( this.clientFactory == lcf ) {
            this.clientFactory = null;
        }
    }


    @Reference
    protected synchronized void setUserService ( UserServiceInternal us ) {
        this.userService = us;
    }


    protected synchronized void unsetUserService ( UserServiceInternal us ) {
        if ( this.userService == us ) {
            this.userService = null;
        }
    }


    @Reference
    protected synchronized void setGroupService ( GroupServiceInternal gs ) {
        this.groupService = gs;
    }


    protected synchronized void unsetGroupService ( GroupServiceInternal gs ) {
        if ( this.groupService == gs ) {
            this.groupService = null;
        }
    }


    @Reference
    protected synchronized void setLDAPConfigFactory ( LDAPRealmConfigFactory lrcf ) {
        this.realmConfigFactory = lrcf;
    }


    protected synchronized void unsetLDAPConfigFactory ( LDAPRealmConfigFactory lrcf ) {
        if ( this.realmConfigFactory == lrcf ) {
            this.realmConfigFactory = null;
        }
    }


    @Reference ( updated = "updatedUserConfiguration" )
    protected synchronized void setUserConfiguration ( UserConfiguration uc ) {}


    protected synchronized void unsetUserConfiguration ( UserConfiguration uc ) {}


    protected synchronized void updatedUserConfiguration ( UserConfiguration uc ) {
        triggerFullSync();
    }


    @Reference ( updated = "updatedQuotaConfiguration" )
    protected synchronized void setQuotaConfiguration ( QuotaConfiguration qc ) {}


    protected synchronized void unsetQuotaConfiguration ( QuotaConfiguration qc ) {}


    protected synchronized void updatedQuotaConfiguration ( QuotaConfiguration qc ) {
        triggerFullSync();
    }


    @Reference ( updated = "updatedSecurityPolicyConfiguration" )
    protected synchronized void setSecurityPolicyConfiguration ( SecurityPolicyConfiguration spc ) {}


    protected synchronized void unsetSecurityPolicyConfiguration ( SecurityPolicyConfiguration spc ) {}


    protected synchronized void updatedSecurityPolicyConfiguration ( SecurityPolicyConfiguration spc ) {
        triggerFullSync();
    }


    @Activate
    protected synchronized void activate ( ComponentContext cc ) throws LDAPException {
        this.realm = ConfigUtil.parseString(cc.getProperties(), "instanceId", null); //$NON-NLS-1$

        if ( StringUtils.isBlank(this.realm) ) {
            log.error("Failed to get instanceId"); //$NON-NLS-1$
            return;
        }

        this.realmConfig = this.realmConfigFactory.createConfig(cc.getProperties());
        this.executor = Executors.newSingleThreadScheduledExecutor();
        this.syncInterval = ConfigUtil.parseDuration(cc.getProperties(), "syncInterval", Duration.standardMinutes(30)); //$NON-NLS-1$
        this.syncRunner = this.executor.scheduleWithFixedDelay(this, 0, this.syncInterval.getStandardSeconds(), TimeUnit.SECONDS);
    }


    @Modified
    protected synchronized void modified ( ComponentContext cc ) throws LDAPException {
        this.realmConfig = this.realmConfigFactory.createConfig(cc.getProperties());
        Duration oldSync = this.syncInterval;
        this.syncInterval = ConfigUtil.parseDuration(cc.getProperties(), "syncInterval", Duration.standardMinutes(30)); //$NON-NLS-1$

        this.syncRunner = this.executor.scheduleWithFixedDelay(this, 0, this.syncInterval.getStandardSeconds(), TimeUnit.SECONDS);
        if ( this.syncRunner != null ) {
            if ( !this.syncInterval.equals(oldSync) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Changing sync interval to " + this.syncInterval); //$NON-NLS-1$
                }
                this.syncRunner.cancel(false);
                this.syncRunner = this.executor.scheduleWithFixedDelay(
                    this,
                    this.syncInterval.getStandardSeconds(),
                    this.syncInterval.getStandardSeconds(),
                    TimeUnit.SECONDS);
            }
        }
        else {
            this.syncRunner = this.executor
                    .scheduleWithFixedDelay(this, this.syncInterval.getStandardSeconds(), this.syncInterval.getStandardSeconds(), TimeUnit.SECONDS);
        }

        triggerFullSync();
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext cc ) {
        if ( this.executor != null ) {
            this.executor.shutdown();
            this.executor = null;
        }
    }


    /**
     * 
     */
    private void triggerFullSync () {
        this.runFullSync = true;
        this.executor.submit(this);
    }


    /**
     * 
     */
    @Override
    public void run () {
        FileshareLDAPSynchronizationHandler handler;
        try {
            handler = makeHandler();
        }
        catch ( EntityTransactionException e ) {
            log.error("Failed to create synchronization handler", e); //$NON-NLS-1$
            return;
        }
        try {
            this.lastRun = DateTime.now();
            long start = System.currentTimeMillis();
            this.ldapSync.run(handler);
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("LDAP synchronization took %.2f s", ( System.currentTimeMillis() - start ) / 1000.0f)); //$NON-NLS-1$
            }
        }
        catch ( Exception e ) {
            log.error("Failed to synchronize", e); //$NON-NLS-1$
        }
        finally {
            handler.close();
        }
    }


    /**
     * @param subClient
     * @return
     * @throws EntityTransactionException
     */
    private FileshareLDAPSynchronizationHandler makeHandler () throws EntityTransactionException {
        DateTime lr = null;
        if ( this.lastRun != null && !this.runFullSync ) {
            lr = new DateTime(this.lastRun);
            log.debug("Running incremental LDAP synchronization on " + this.realm); //$NON-NLS-1$
        }
        else {
            log.info("Running full LDAP synchronization on " + this.realm); //$NON-NLS-1$
        }
        this.runFullSync = false;
        return new FileshareLDAPSynchronizationHandler(
            this.ctx,
            this.userMapper,
            this.userService,
            this.groupService,
            this.realmConfig,
            this.clientFactory,
            this.realm,
            lr);
    }

}
