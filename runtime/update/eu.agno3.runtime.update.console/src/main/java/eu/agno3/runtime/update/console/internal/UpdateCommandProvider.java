/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.06.2013 by mbechler
 */
package eu.agno3.runtime.update.console.internal;


import java.net.URI;
import java.util.Collections;
import java.util.Set;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.console.Session;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.console.CommandProvider;
import eu.agno3.runtime.update.Feature;
import eu.agno3.runtime.update.FeatureUpdate;
import eu.agno3.runtime.update.PlatformStateMXBean;
import eu.agno3.runtime.update.UpdateException;
import eu.agno3.runtime.update.UpdateManager;
import eu.agno3.runtime.update.UpdateManagerProvider;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    CommandProvider.class
} )
public class UpdateCommandProvider implements CommandProvider {

    private UpdateManagerProvider updateManager;
    private PlatformStateMXBean platformState;


    @Reference
    protected synchronized void setUpdateManager ( UpdateManagerProvider um ) {
        this.updateManager = um;
    }


    protected synchronized void unsetUpdateManager ( UpdateManagerProvider um ) {
        if ( this.updateManager == um ) {
            this.updateManager = null;
        }
    }


    @Reference
    protected synchronized void setPlatformStatusManager ( PlatformStateMXBean ps ) {
        this.platformState = ps;
    }


    protected synchronized void unsetPlatformStatusManager ( PlatformStateMXBean ps ) {
        if ( this.platformState == ps ) {
            this.platformState = null;
        }
    }


    /**
     * @return the updateManager
     * @throws UpdateException
     */
    synchronized UpdateManager getUpdateManager () throws UpdateException {
        return this.updateManager.getLocalUpdateManager();
    }


    /**
     * @return the platformState
     */
    synchronized PlatformStateMXBean getPlatformState () {
        return this.platformState;
    }

    /**
     * Get the repositories available to the update manager
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "update", name = "repositories", description = "Get the repositories available to the update manager" )
    public class RepositoriesCommand implements Action {

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () throws UpdateException {
            for ( URI repoUri : getUpdateManager().listRepositories() ) {
                this.session.getConsole().println(repoUri.toString());
            }
            return null;
        }

    }

    /**
     * List the installed features
     * 
     * @author mbechler
     *
     */
    @Command ( scope = "update", name = "installed", description = "List the installed features" )
    public class InstalledCommand implements Action {

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () throws UpdateException {

            try ( UpdateManager um = getUpdateManager() ) {
                for ( Feature iu : um.listInstalledFeatures() ) {
                    this.session.getConsole().println(iu);
                }

                return null;
            }
        }

    }

    /**
     * List the optional software that can be installed
     * 
     * @author mbechler
     *
     */
    @Command ( scope = "update", name = "installable", description = "List the optional software that can be installed" )
    public class InstallableCommand implements Action {

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () throws UpdateException {
            try ( UpdateManager um = getUpdateManager() ) {
                for ( Feature iu : um.getInstallableFeatures() ) {
                    this.session.getConsole().println(iu);
                }
                return null;
            }
        }

    }

    /**
     * Check for updates
     * 
     * @author mbechler
     *
     */
    @Command ( scope = "update", name = "check", description = "Check for updates" )
    public class CheckCommand implements Action {

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () throws UpdateException {
            try ( UpdateManager um = getUpdateManager() ) {
                Set<FeatureUpdate> updates = um.checkForUpdates(new ConsoleProgressMonitor(this.session));

                if ( updates.isEmpty() ) {
                    this.session.getConsole().println("No updates available"); //$NON-NLS-1$
                }
                else {
                    for ( FeatureUpdate upd : updates ) {
                        this.session.getConsole().println(upd.getOld());
                    }
                }
                return null;
            }
        }

    }

    /**
     * Install all available updates
     * 
     * @author mbechler
     *
     */
    @Command ( scope = "update", name = "upgrade", description = "Install all available updates" )
    public class UpgradeCommand implements Action {

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () throws UpdateException {
            try ( UpdateManager um = getUpdateManager() ) {
                um.installAllUpdates(Collections.EMPTY_SET, new ConsoleProgressMonitor(this.session));
                return null;
            }
        }

    }

    /**
     * Run garbage collections (removes unneccecary IUs)
     * 
     * @author mbechler
     */
    @Command ( scope = "update", name = "gc", description = "Remove unnecessary IUs" )
    public class GcCommand implements Action {

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () throws UpdateException {
            try ( UpdateManager um = getUpdateManager() ) {
                um.runGarbageCollection();
                return null;
            }
        }

    }

    /**
     * Install an optional feature
     */
    @Command ( scope = "update", name = "install", description = "Install an optional feature" )
    public class InstallCommand implements Action {

        @Argument ( index = 0, name = "feature", required = true )
        private String spec;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () throws UpdateException {
            try ( UpdateManager um = getUpdateManager() ) {
                um.installFeature(this.spec, new ConsoleProgressMonitor(this.session));
                return null;
            }
        }

    }

    /**
     * Remove an optional feature
     */
    @Command ( scope = "update", name = "remove", description = "Remove an optional feature" )
    public class RemoveCommand implements Action {

        @Argument ( index = 0, name = "feature", required = true )
        private String spec;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () throws UpdateException {
            try ( UpdateManager um = getUpdateManager() ) {
                um.removeFeature(this.spec, new ConsoleProgressMonitor(this.session));
                return null;
            }
        }

    }

    /**
     * Remove an optional feature
     */
    @Command ( scope = "platform", name = "state", description = "Get platform status" )
    public class PlatformStatusCommand implements Action {

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () throws UpdateException {
            this.session.getConsole().println(getPlatformState().getState());
            return null;
        }

    }

    /**
     * Remove an optional feature
     */
    @Command ( scope = "platform", name = "refreshState", description = "Refresh platform status" )
    public class PlatformRefreshCommand implements Action {

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () throws UpdateException {
            PlatformStateMXBean ps = getPlatformState();
            ps.refreshAppState();
            this.session.getConsole().println(ps.getState());
            return null;
        }

    }

}
