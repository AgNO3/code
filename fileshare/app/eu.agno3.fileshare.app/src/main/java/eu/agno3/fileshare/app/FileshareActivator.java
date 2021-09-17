/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.06.2013 by mbechler
 */
package eu.agno3.fileshare.app;


import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;


/**
 * @author mbechler
 * 
 */
public class FileshareActivator implements BundleActivator {

    private static final Logger log = Logger.getLogger(FileshareActivator.class);


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start ( BundleContext context ) {
        log.info("AgNO3 File-Shield Server starting...\n"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop ( BundleContext context ) {
        log.info("AgNO3 File-Shield Server stopping...\n"); //$NON-NLS-1$
    }

}
