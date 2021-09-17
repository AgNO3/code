/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 26, 2016 by mbechler
 */
package eu.agno3.runtime.configloader.internal;


import org.apache.log4j.Logger;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.service.cm.ConfigurationListener;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    ConfigurationListener.class, SynchronousBundleListener.class
} )
public class ConfigEventDebugger implements ConfigurationListener, SynchronousBundleListener {

    private static final Logger log = Logger.getLogger(ConfigEventDebugger.class);


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        ctx.getBundleContext().addBundleListener(this);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.service.cm.ConfigurationListener#configurationEvent(org.osgi.service.cm.ConfigurationEvent)
     */
    @Override
    public void configurationEvent ( ConfigurationEvent ev ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("CFG %s %s %s", ev.getType(), ev.getFactoryPid(), ev.getPid())); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.framework.BundleListener#bundleChanged(org.osgi.framework.BundleEvent)
     */
    @Override
    public void bundleChanged ( BundleEvent ev ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("BND %s %s", ev.getType(), ev.getBundle().getSymbolicName())); //$NON-NLS-1$
        }
    }

}
