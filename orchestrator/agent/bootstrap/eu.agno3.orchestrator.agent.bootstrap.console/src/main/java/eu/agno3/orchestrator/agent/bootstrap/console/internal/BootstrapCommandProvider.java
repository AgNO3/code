/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.09.2013 by mbechler
 */
package eu.agno3.orchestrator.agent.bootstrap.console.internal;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.console.Session;
import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;

import eu.agno3.orchestrator.agent.bootstrap.BootstrapRunnerLock;
import eu.agno3.runtime.configloader.ConfigContribution;
import eu.agno3.runtime.configloader.ConfigLoader;
import eu.agno3.runtime.console.CommandProvider;


/**
 * @author mbechler
 * 
 */
@Component ( immediate = true )
public class BootstrapCommandProvider implements CommandProvider {

    private static final Logger log = Logger.getLogger(BootstrapCommandProvider.class);
    private ComponentContext componentContext;

    private ConfigLoader configLoader;
    private ServiceComponentRuntime scrService;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.componentContext = ctx;
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        this.componentContext = null;
    }


    @Reference
    protected synchronized void setConfigLoader ( ConfigLoader cl ) {
        this.configLoader = cl;
    }


    protected synchronized void unsetConfigLoader ( ConfigLoader cl ) {
        if ( this.configLoader == cl ) {
            this.configLoader = null;
        }
    }


    @Reference
    protected synchronized void setScrService ( ServiceComponentRuntime scr ) {
        this.scrService = scr;
    }


    protected synchronized void unsetScrService ( ServiceComponentRuntime scr ) {
        if ( this.scrService == scr ) {
            this.scrService = null;
        }
    }


    /**
     * @return the componentContext
     */
    ComponentContext getComponentContext () {
        return this.componentContext;
    }


    /**
     * @return the scrService
     */
    public ServiceComponentRuntime getScrService () {
        return this.scrService;
    }


    /**
     * @return the configLoader
     */
    ConfigLoader getConfigLoader () {
        return this.configLoader;
    }


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }

    /**
     * Show server connector status
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "bootstrap", name = "run", description = "Rerun bootstrap process" )
    public class RunCommand implements Action {

        /**
         * 
         */
        private static final String BOOTSTRAP_PID = "bootstrap"; //$NON-NLS-1$

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () {

            try {
                getConfigLoader().reload(BOOTSTRAP_PID);
            }
            catch ( IOException e ) {
                getLog().warn("Failed to reload bootstrap configuration", e); //$NON-NLS-1$
            }

            Collection<ConfigContribution> sourcesForPid = getConfigLoader().getSourcesForPid(BOOTSTRAP_PID);
            if ( sourcesForPid == null || sourcesForPid.isEmpty() ) {
                this.session.getConsole().append("No bootstrap configuration available"); //$NON-NLS-1$
                return null;
            }

            ComponentDescriptionDTO comp = getScrService().getComponentDescriptionDTO(
                getComponentContext().getBundleContext().getBundle("eu.agno3.orchestrator.agent.bootstrap"), //$NON-NLS-1$
                "eu.agno3.orchestrator.agent.bootstrap.internal.BootstrapRunnerImpl"); //$NON-NLS-1$

            if ( comp == null ) {
                this.session.getConsole().append("Failure to locate bootstrap runner component"); //$NON-NLS-1$
                return null;
            }

            BootstrapRunnerLock.reenable();

            try {
                getScrService().disableComponent(comp).then( ( p ) -> getScrService().enableComponent(comp), ( p ) -> {
                    this.session.getConsole().append("Failure to disable runner component"); //$NON-NLS-1$
                }).getValue();
            }
            catch (
                InvocationTargetException |
                InterruptedException e ) {
                this.session.getConsole().append("Failure to enable runner component"); //$NON-NLS-1$
                e.printStackTrace(this.session.getConsole());
            }

            return null;
        }
    }

}
