/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.06.2013 by mbechler
 */
package eu.agno3.orchestrator.agent.app;


import org.apache.log4j.Logger;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;


/**
 * @author mbechler
 * 
 */
public class AgentApplication implements IApplication {

    private static final Logger log = Logger.getLogger(AgentApplication.class);


    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
     */
    @Override
    public Object start ( IApplicationContext context ) {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.equinox.app.IApplication#stop()
     */
    @Override
    public void stop () {
        log.info("Agent shut down..."); //$NON-NLS-1$
    }

}
