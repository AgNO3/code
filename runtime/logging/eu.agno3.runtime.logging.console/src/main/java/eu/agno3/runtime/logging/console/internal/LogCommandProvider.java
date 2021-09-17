/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.06.2013 by mbechler
 */
package eu.agno3.runtime.logging.console.internal;


import java.util.Collection;
import java.util.Map.Entry;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.console.Session;
import org.apache.log4j.Level;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.ServiceTracker;

import eu.agno3.runtime.console.CommandProvider;
import eu.agno3.runtime.logging.LogConfigurationService;
import eu.agno3.runtime.logging.TracingVerbosity;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    CommandProvider.class
} )
public class LogCommandProvider implements CommandProvider {

    private ServiceTracker<LogConfigurationService, LogConfigurationService> logConfigServiceTracker;


    /**
     * @param context
     */
    @Activate
    public void activate ( ComponentContext context ) {
        this.logConfigServiceTracker = new ServiceTracker<>(context.getBundleContext(), LogConfigurationService.class, null);
        this.logConfigServiceTracker.open();
    }


    /**
     * @param context
     */
    @Deactivate
    public void deactivate ( ComponentContext context ) {
        this.logConfigServiceTracker.close();
        this.logConfigServiceTracker = null;
    }


    protected LogConfigurationService getLogConfigService () {
        LogConfigurationService logConfigService = this.logConfigServiceTracker.getService();

        if ( logConfigService == null ) {
            throw new IllegalStateException("No LogConfigurationService available."); //$NON-NLS-1$
        }

        return logConfigService;
    }


    Collection<String> getLoggerScopes () {
        return this.getLogConfigService().listOverrides().keySet();
    }

    /**
     * Get the effective log threshold for a logger
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "log", name = "get", description = "Get the effective log threshold for a logger" )
    public class GetCommand implements Action {

        @Argument ( index = 0, name = "logger", required = true )
        @Completion ( LogScopeCompleter.class )
        String logger = null;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        /**
         * 
         * {@inheritDoc}
         *
         * @see org.apache.karaf.shell.api.action.Action#execute()
         */
        @Override
        public Object execute () {
            Level overrideLevel = getLogConfigService().getOverrideLogLevel(this.logger);

            if ( overrideLevel != null ) {
                this.session.getConsole().println(String.format("Override Log Level: %s", overrideLevel.toString())); //$NON-NLS-1$
            }
            else {
                Level effectiveLevel = getLogConfigService().getEffectiveLogLevel(this.logger);
                this.session.getConsole().println(String.format("Effective Log Level: %s", effectiveLevel.toString())); //$NON-NLS-1$
            }
            return null;
        }

    }

    /**
     * Override the logging threshold for a logger
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "log", name = "set", description = "Override the logging threshold for a logger" )
    public class SetCommand implements Action {

        @Argument ( index = 0, name = "logger", required = true )
        @Completion ( LogScopeCompleter.class )
        String logger = null;

        @Argument ( index = 1, name = "level", required = true )
        String level = null;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () {
            Level l = Level.toLevel(this.level);
            getLogConfigService().setOverrideLogLevel(this.logger, l);
            return null;
        }

    }

    /**
     * List logger threshold overrides
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "log", name = "list", description = "List logger threshold overrides" )
    public class ListCommand implements Action {

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () {
            for ( Entry<String, Level> override : getLogConfigService().listOverrides().entrySet() ) {
                this.session.getConsole().println(String.format("%s: %s", override.getKey(), override.getValue().toString())); //$NON-NLS-1$
            }
            return null;
        }

    }

    /**
     * Reset logger threshold overrides below a specified scope
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "log", name = "reset", description = "Reset logger threshold overrides below a specified scope" )
    public class ResetCommand implements Action {

        @Argument ( index = 0, name = "scope", required = true )
        @Completion ( LogScopeCompleter.class )
        String scope = null;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () {
            getLogConfigService().resetOverrides(this.scope);
            return null;
        }

    }

    /**
     * Get or set the tracing verbosity
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "log", name = "verbosity", description = "Get or set the tracing verbosity" )
    public class VerbosityCommand implements Action {

        @Argument ( index = 0, name = "verbosity", required = false )
        TracingVerbosity verbosity = null;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () {
            if ( this.verbosity == null ) {
                this.session.getConsole().println(String.format("Current verbosity: %s", getLogConfigService().getTracingVerbosity())); //$NON-NLS-1$
            }
            else {
                getLogConfigService().setTraceVerbosity(this.verbosity);
            }

            return null;
        }

    }

}
