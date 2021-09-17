/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.packagekit.internal;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.freedesktop.PackageKit;
import org.freedesktop.PackageKit.Transaction.ItemProgress;
import org.freedesktop.PackageKit.Transaction.Package;
import org.freedesktop.PackageKit.Transaction.RepoDetail;
import org.freedesktop.PackageKit.Transaction.UpdateDetail;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.freedesktop.dbus.types.UInt64;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.system.base.SystemService;
import eu.agno3.orchestrator.system.base.SystemServiceType;
import eu.agno3.orchestrator.system.dbus.SystemDBusClient;
import eu.agno3.orchestrator.system.img.util.SystemImageUtil;
import eu.agno3.orchestrator.system.packagekit.PackageId;
import eu.agno3.orchestrator.system.packagekit.PackageKitException;
import eu.agno3.orchestrator.system.packagekit.PackageKitProgressListener;
import eu.agno3.orchestrator.system.packagekit.PackageUpdate;
import eu.agno3.orchestrator.system.packagekit.SystemUpdateManager;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    SystemUpdateManager.class, SystemService.class
}, configurationPid = "systemUpdate" )
@SystemServiceType ( SystemUpdateManager.class )
public class SystemUpdateManagerImpl implements SystemUpdateManager {

    private static final String SYSTEMD_BUSNAME = "org.freedesktop.PackageKit"; //$NON-NLS-1$
    private static final String SYSTEMD_ROOT_PATH = "/org/freedesktop/PackageKit"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(SystemUpdateManager.class);

    private SystemDBusClient dbus;

    protected static final UInt64 PK_FILTER_ENUM_UNKNOWN = new UInt64(0);
    protected static final UInt64 PK_FILTER_ENUM_NONE = new UInt64(1);
    protected static final UInt64 PK_FILTER_ENUM_INSTALLED = new UInt64(1 << 2);
    protected static final UInt64 PK_FILTER_ENUM_NOT_INSTALLED = new UInt64(1 << 3);

    protected static final UInt64 PK_TRANSACTION_ALLOW_UNTRUSTED = new UInt64(0);
    protected static final UInt64 PK_TRANSACTION_ONLY_TRUSTED = new UInt64(1);
    protected static final UInt64 PK_TRANSACTION_DOWNLOAD_TRUSTED = new UInt64(1 + ( 1 << 2 ) + ( 1 << 3 ));
    protected static final UInt64 PK_TRANSACTION_DOWNLOAD = new UInt64( ( 1 << 3 ));

    private int transactionTimeout = 120000;
    private boolean noSignatures = false;

    private String mainRepoDistMatch;
    private String mainRepoSectionMatch;


    /**
     * Test only
     * 
     * @param match
     */
    public void setMainRepoDistMatch ( String match ) {
        this.mainRepoDistMatch = match;
    }


    /**
     * Test only
     * 
     * @param match
     */
    public void setMainRepoSectionMatch ( String match ) {
        this.mainRepoSectionMatch = match;
    }


    @Activate
    protected void activate ( ComponentContext ctx ) throws DBusException {
        this.mainRepoDistMatch = ConfigUtil.parseString(ctx.getProperties(), "mainRepoDistribution", SystemImageUtil.getDistributionCodename()); //$NON-NLS-1$
        this.mainRepoSectionMatch = ConfigUtil.parseString(ctx.getProperties(), "mainRepoSection", "main"); //$NON-NLS-1$ //$NON-NLS-2$
        this.noSignatures = ConfigUtil.parseBoolean(ctx.getProperties(), "disableSignatureCheck", false); //$NON-NLS-1$

        ClassLoader origTCCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        try {
            this.dbus.getRemoteObject(SYSTEMD_BUSNAME, SYSTEMD_ROOT_PATH, PackageKit.class);
        }
        finally {
            Thread.currentThread().setContextClassLoader(origTCCL);
        }
    }


    @Deactivate
    protected void deactivate ( ComponentContext ctx ) {

    }


    @Reference
    protected synchronized void setDBusClient ( SystemDBusClient cl ) {
        this.dbus = cl;
    }


    protected synchronized void unsetDBusClient ( SystemDBusClient cl ) {
        if ( this.dbus == cl ) {
            this.dbus = null;
        }
    }


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }


    private int getTransactionTimeout () {
        return this.transactionTimeout;
    }


    @Override
    public void setRepositoryLocation ( String id, String url ) throws PackageKitException {
        ClassLoader origTCCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        try {
            PackageKit pk = this.dbus.getRemoteObject(SYSTEMD_BUSNAME, SYSTEMD_ROOT_PATH, PackageKit.class);
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Set repository %s to %s", id, url)); //$NON-NLS-1$
            }
            try ( SignalHandlers h = new SignalHandlers(this.dbus, pk, null, new BasePackageKitListener()) ) {
                h.getTransaction().RepoSetData(id, "url", url); //$NON-NLS-1$
                h.waitForCompletion(getTransactionTimeout());
            }
        }
        catch (
            DBusException |
            DBusExecutionException |
            InterruptedException e ) {
            throw new PackageKitException("Failed to set repository url", e); //$NON-NLS-1$
        }
        finally {
            Thread.currentThread().setContextClassLoader(origTCCL);
        }
    }


    /**
     * {@inheritDoc}
     * 
     *
     * @see eu.agno3.orchestrator.system.packagekit.SystemUpdateManager#switchRepository(java.lang.String)
     */
    @Override
    public String switchRepository ( String newRepo ) throws PackageKitException {
        ClassLoader origTCCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        try {
            PackageKit pk = this.dbus.getRemoteObject(SYSTEMD_BUSNAME, SYSTEMD_ROOT_PATH, PackageKit.class);

            log.debug("Getting repository list"); //$NON-NLS-1$
            BasePackageKitListener l = new BasePackageKitListener();
            try ( SignalHandlers h = new SignalHandlers(this.dbus, pk, null, l, RepoDetail.class) ) {
                h.getTransaction().GetRepoList(PK_FILTER_ENUM_UNKNOWN);
                h.waitForCompletion(getTransactionTimeout());
            }

            for ( String repoId : l.getCollectedRepositoryIds() ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Found repository " + repoId); //$NON-NLS-1$
                }

                APTCCRepoId parsed = APTCCRepoId.fromString(repoId);

                if ( parsed != null && isMainRepository(parsed) ) {
                    try ( SignalHandlers h = new SignalHandlers(this.dbus, pk, null, new BasePackageKitListener()) ) {
                        h.getTransaction().RepoSetData(repoId, "url", newRepo); //$NON-NLS-1$
                        h.waitForCompletion(getTransactionTimeout());
                    }
                    return parsed.getUri();
                }
            }
        }
        catch (
            DBusException |
            DBusExecutionException |
            InterruptedException e ) {
            throw new PackageKitException("Failed to get updates", e); //$NON-NLS-1$
        }
        finally {
            Thread.currentThread().setContextClassLoader(origTCCL);
        }

        if ( log.isDebugEnabled() ) {
            log.warn("Update repository not found"); //$NON-NLS-1$
        }
        return null;
    }


    /**
     * Test only
     * 
     * @param parsed
     * @return whether this is the main repository
     */
    public boolean isMainRepository ( APTCCRepoId parsed ) {
        if ( this.mainRepoDistMatch == null || this.mainRepoSectionMatch == null ) {
            return false;
        }

        if ( !this.mainRepoDistMatch.equals(parsed.getDist()) ) {
            return false;
        }

        if ( parsed.getSections() != null ) {
            for ( String section : parsed.getSections() ) {
                if ( this.mainRepoSectionMatch.equals(section) ) {
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public Set<PackageUpdate> checkForUpdates ( PackageKitProgressListener pl ) throws PackageKitException {
        ClassLoader origTCCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        try {
            PackageKit pk = this.dbus.getRemoteObject(SYSTEMD_BUSNAME, SYSTEMD_ROOT_PATH, PackageKit.class);

            // TODO: check whether this is useful with APT
            // Set<PackageId> installedSoftware = getInstalledSoftware();
            Set<PackageId> installedSoftware = Collections.EMPTY_SET;
            UpdateListener listener = new UpdateListener(installedSoftware);

            log.debug("Refreshing cache"); //$NON-NLS-1$
            try ( SignalHandlers h = new SignalHandlers(this.dbus, pk, pl, listener) ) {
                h.scaleProgress(30, 0);
                h.getTransaction().RefreshCache(true);
                h.waitForCompletion(getTransactionTimeout());
            }

            log.debug("Getting updates"); //$NON-NLS-1$
            try ( SignalHandlers h = new SignalHandlers(this.dbus, pk, pl, listener, Package.class) ) {
                h.scaleProgress(50, 30);
                h.getTransaction().GetUpdates(PK_FILTER_ENUM_NONE);
                h.waitForCompletion(getTransactionTimeout());
            }

            log.debug("Getting update details"); //$NON-NLS-1$
            List<String> packageIds = listener.getCollectedPackageIds();
            if ( !packageIds.isEmpty() ) {
                try ( SignalHandlers h = new SignalHandlers(this.dbus, pk, pl, listener, UpdateDetail.class) ) {
                    h.scaleProgress(20, 80);
                    h.getTransaction().GetUpdateDetail(packageIds);
                    h.waitForCompletion(getTransactionTimeout());
                }
            }
            return listener.getUpdates();
        }
        catch (
            DBusException |
            DBusExecutionException |
            InterruptedException e ) {
            throw new PackageKitException("Failed to get updates", e); //$NON-NLS-1$
        }
        finally {
            Thread.currentThread().setContextClassLoader(origTCCL);
        }
    }


    @Override
    public Set<PackageId> getInstalledSoftware ( PackageKitProgressListener pl ) throws PackageKitException {
        ClassLoader origTCCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        try {
            PackageKit pk = this.dbus.getRemoteObject(SYSTEMD_BUSNAME, SYSTEMD_ROOT_PATH, PackageKit.class);
            PackageListListener listener = new PackageListListener();

            log.debug("Listing installed software"); //$NON-NLS-1$
            try ( SignalHandlers h = new SignalHandlers(this.dbus, pk, pl, listener, Package.class) ) {
                h.getTransaction().GetPackages(PK_FILTER_ENUM_INSTALLED);
                h.waitForCompletion(getTransactionTimeout());
            }

            return new HashSet<>(listener.getPackages());
        }
        catch (
            DBusException |
            DBusExecutionException |
            InterruptedException e ) {
            throw new PackageKitException("Failed to get installed packages", e); //$NON-NLS-1$
        }
        finally {
            Thread.currentThread().setContextClassLoader(origTCCL);
        }
    }


    @Override
    public void prepareUpdates ( Set<PackageId> pkgs, PackageKitProgressListener pl ) throws PackageKitException {
        if ( pkgs == null || pkgs.isEmpty() ) {
            return;
        }

        ClassLoader origTCCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        try {
            PackageKit pk = this.dbus.getRemoteObject(SYSTEMD_BUSNAME, SYSTEMD_ROOT_PATH, PackageKit.class);
            BasePackageKitListener listener = new BasePackageKitListener();

            log.debug("Refreshing cache"); //$NON-NLS-1$
            try ( SignalHandlers h = new SignalHandlers(this.dbus, pk, pl, listener) ) {
                h.scaleProgress(20, 0);
                h.getTransaction().RefreshCache(false);
                h.waitForCompletion(getTransactionTimeout());
            }

            List<String> packageIds = toPackageIds(pkgs);
            if ( log.isDebugEnabled() ) {
                log.debug("Preparing package updates " + packageIds); //$NON-NLS-1$
            }

            UInt64 flags = this.noSignatures ? PK_TRANSACTION_DOWNLOAD : PK_TRANSACTION_DOWNLOAD_TRUSTED;
            try ( SignalHandlers h = new SignalHandlers(this.dbus, pk, pl, listener, Package.class, ItemProgress.class) ) {
                h.getTransaction().RepairSystem(flags);
                h.waitForCompletion();
            }

            try ( SignalHandlers h = new SignalHandlers(this.dbus, pk, pl, listener, Package.class, ItemProgress.class) ) {
                h.scaleProgress(80, 20);
                h.getTransaction().UpdatePackages(flags, packageIds);
                h.waitForCompletion();
            }
        }
        catch (
            DBusException |
            DBusExecutionException |
            InterruptedException e ) {
            throw new PackageKitException("Failed to get installed packages", e); //$NON-NLS-1$
        }
        finally {
            Thread.currentThread().setContextClassLoader(origTCCL);
        }
    }


    @Override
    public void installUpdates ( Set<PackageId> pkgs, PackageKitProgressListener pl ) throws PackageKitException {

        if ( pkgs == null || pkgs.isEmpty() ) {
            return;
        }

        ClassLoader origTCCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        try {
            PackageKit pk = this.dbus.getRemoteObject(SYSTEMD_BUSNAME, SYSTEMD_ROOT_PATH, PackageKit.class);
            BasePackageKitListener listener = new BasePackageKitListener();

            List<String> packageIds = toPackageIds(pkgs);
            if ( log.isDebugEnabled() ) {
                log.debug("Installing package updates " + packageIds); //$NON-NLS-1$
            }
            try ( SignalHandlers h = new SignalHandlers(this.dbus, pk, pl, listener, Package.class, ItemProgress.class) ) {
                h.getTransaction().UpdatePackages(this.noSignatures ? PK_TRANSACTION_ALLOW_UNTRUSTED : PK_TRANSACTION_ONLY_TRUSTED, packageIds);
                h.waitForCompletion();
            }
        }
        catch (
            DBusException |
            DBusExecutionException |
            InterruptedException e ) {
            throw new PackageKitException("Failed to get installed packages", e); //$NON-NLS-1$
        }
        finally {
            Thread.currentThread().setContextClassLoader(origTCCL);
        }

    }


    @Override
    public void repair ( PackageKitProgressListener pl ) throws PackageKitException {
        ClassLoader origTCCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        try {
            PackageKit pk = this.dbus.getRemoteObject(SYSTEMD_BUSNAME, SYSTEMD_ROOT_PATH, PackageKit.class);
            BasePackageKitListener listener = new BasePackageKitListener();

            log.debug("Repairing package installation"); //$NON-NLS-1$
            try ( SignalHandlers h = new SignalHandlers(this.dbus, pk, pl, listener, Package.class, ItemProgress.class) ) {
                h.getTransaction().RepairSystem(this.noSignatures ? PK_TRANSACTION_ALLOW_UNTRUSTED : PK_TRANSACTION_ONLY_TRUSTED);
                h.waitForCompletion();
            }
        }
        catch (
            DBusException |
            DBusExecutionException |
            InterruptedException e ) {
            throw new PackageKitException("Failed to repair", e); //$NON-NLS-1$
        }
        finally {
            Thread.currentThread().setContextClassLoader(origTCCL);
        }

    }


    /**
     * @param pkgs
     * @return
     */
    private static List<String> toPackageIds ( Set<PackageId> pkgs ) {
        List<String> pids = new ArrayList<>();
        for ( PackageId p : pkgs ) {
            pids.add(String.format(
                "%s;%s;%s;%s", //$NON-NLS-1$
                p.getPackageName(),
                p.getPackageVersion(),
                p.getPackageArch() != null ? p.getPackageArch() : StringUtils.EMPTY,
                p.getPackageRepo() != null ? p.getPackageRepo() : StringUtils.EMPTY));
        }
        return pids;
    }
}
