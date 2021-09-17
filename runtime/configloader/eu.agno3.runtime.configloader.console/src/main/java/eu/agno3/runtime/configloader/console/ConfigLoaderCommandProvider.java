/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.07.2013 by mbechler
 */
package eu.agno3.runtime.configloader.console;


import java.io.IOException;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.console.Session;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.configloader.ConfigLoader;
import eu.agno3.runtime.console.CommandProvider;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    CommandProvider.class
} )
public class ConfigLoaderCommandProvider implements CommandProvider {

    private static final Logger log = Logger.getLogger(ConfigLoaderCommandProvider.class);

    private ConfigLoader configLoader;


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }


    @Reference
    protected synchronized void setConfigLoader ( ConfigLoader loader ) {
        this.configLoader = loader;
    }


    protected synchronized void unsetConfigLoader ( ConfigLoader loader ) {
        if ( this.configLoader == loader ) {
            this.configLoader = null;
        }
    }


    /**
     * @return the configLoader
     */
    public synchronized ConfigLoader getConfigLoader () {
        return this.configLoader;
    }

    /**
     * Refresh system configuration
     * 
     * @author mbechler
     */
    @Command ( scope = "config", name = "reload", description = "Reload system configuration" )
    public class RefreshCommand implements Action {

        @Argument ( index = 0, name = "hint", description = "Reload hint (empty=all, pid or pid@instance)", required = false )
        private String hint;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () {

            try {
                getConfigLoader().reload(this.hint);
                this.session.getConsole().println("Confguration reloaded"); //$NON-NLS-1$
            }
            catch ( IOException e ) {
                getLog().warn("Failed to reload configuration", e); //$NON-NLS-1$
                this.session.getConsole().println("Failed to reload configuration: " + e.getMessage()); //$NON-NLS-1$
            }

            return null;
        }

    }

    /**
     * Refresh system configuration
     * 
     * @author mbechler
     */
    @Command ( scope = "config", name = "forceReload", description = "Force update system configuration" )
    public class ForceRefreshCommand implements Action {

        @Argument ( index = 0, name = "hint", description = "Reload hint (empty=all, pid or pid@instance)", required = false )
        private String hint;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () {

            try {
                getConfigLoader().forceReload(this.hint);
                this.session.getConsole().println("Confguration reloaded"); //$NON-NLS-1$
            }
            catch ( IOException e ) {
                getLog().warn("Failed to reload configuration", e); //$NON-NLS-1$
                this.session.getConsole().println("Failed to reload configuration: " + e.getMessage()); //$NON-NLS-1$
            }

            return null;
        }

    }
}
