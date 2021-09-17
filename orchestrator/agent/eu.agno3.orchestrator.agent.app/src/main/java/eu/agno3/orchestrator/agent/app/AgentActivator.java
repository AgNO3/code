/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.06.2013 by mbechler
 */
package eu.agno3.orchestrator.agent.app;


import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;


/**
 * @author mbechler
 * 
 */
public class AgentActivator implements BundleActivator {

    private static final Logger log = Logger.getLogger(AgentActivator.class);


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start ( BundleContext context ) {
        log.info("AgNO3 Orchestrator Agent starting...\n"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop ( BundleContext context ) {

    }

}
