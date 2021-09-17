/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.12.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.bootstrap;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.bootstrap.BootstrapContext;
import eu.agno3.orchestrator.bootstrap.service.BootstrapService;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
public class BootstrapContextProvider {

    private static final Logger log = Logger.getLogger(BootstrapContextProvider.class);

    @Inject
    private ServerServiceProvider ssp;

    private boolean loadedContext = false;
    private BootstrapContext context;


    /**
     * @return the context
     */
    public BootstrapContext getContext () {

        if ( !this.loadedContext ) {
            this.loadedContext = true;
            log.debug("Loading bootstrap context"); //$NON-NLS-1$
            try {
                this.context = this.ssp.getService(BootstrapService.class).getBootstrapContext();

                if ( this.context == null ) {
                    log.debug("No context available"); //$NON-NLS-1$
                }
                else if ( log.isDebugEnabled() ) {
                    log.debug("Context type: " + this.context.getType()); //$NON-NLS-1$
                }
            }
            catch ( Exception e ) {
                log.error("Failed to get bootstrap context", e); //$NON-NLS-1$
                ExceptionHandler.handle(e);
            }

        }

        return this.context;
    }


    /**
     * 
     */
    public void reset () {
        log.debug("Resetting bootstrap context"); //$NON-NLS-1$
        this.loadedContext = false;
        this.context = null;
    }
}
