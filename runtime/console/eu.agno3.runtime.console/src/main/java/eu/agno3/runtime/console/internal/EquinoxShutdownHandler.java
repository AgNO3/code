/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.06.2013 by mbechler
 */
package eu.agno3.runtime.console.internal;


import org.apache.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.console.ShutdownHandler;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    ShutdownHandler.class
} )
public class EquinoxShutdownHandler implements ShutdownHandler {

    private static final Logger log = Logger.getLogger(EquinoxShutdownHandler.class);
    private BundleContext context;


    /**
     * @param context
     */
    @Activate
    protected void activate ( ComponentContext ctx ) {
        this.context = ctx.getBundleContext();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.console.ShutdownHandler#shutdown()
     */
    @Override
    public void shutdown () {
        try {
            this.context.getBundle(0).stop();
        }
        catch ( BundleException e ) {
            log.error("Shutdown failed:", e); //$NON-NLS-1$
        }

    }

}
