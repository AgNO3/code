/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jan 15, 2017 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config.test;


import java.util.Collection;

import org.apache.log4j.Logger;
import org.primefaces.event.SelectEvent;

import eu.agno3.orchestrator.server.webgui.components.OuterWrapper;


/**
 * @author mbechler
 *
 */
public class ConfigReturnWrapper {

    private static final Logger log = Logger.getLogger(ConfigReturnWrapper.class);

    private OuterWrapper<?> ctx;
    private ConfigTestReturnHandler handler;


    /**
     * @param ctx
     * @param handler
     */
    public ConfigReturnWrapper ( OuterWrapper<?> ctx, ConfigTestReturnHandler handler ) {
        this.ctx = ctx;
        this.handler = handler;
    }


    public void onReturn ( SelectEvent ev ) {

        if ( this.ctx == null ) {
            log.warn("No context provided"); //$NON-NLS-1$
            return;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Have return " + ev.getObject()); //$NON-NLS-1$
        }

        if ( ! ( ev.getObject() instanceof Collection ) ) {
            log.debug("Invalid return interaction " + ev.getObject()); //$NON-NLS-1$
            return;
        }

        Collection<?> c = (Collection<?>) ev.getObject();

        for ( Object inter : c ) {
            if ( ! ( inter instanceof ConfigTestInteraction ) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Invalid interaction " + inter); //$NON-NLS-1$
                }
                continue;
            }

            this.handler.interact(this.ctx, (ConfigTestInteraction) inter);
        }

    }

}
