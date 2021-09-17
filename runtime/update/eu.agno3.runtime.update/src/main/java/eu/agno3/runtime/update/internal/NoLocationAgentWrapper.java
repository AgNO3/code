/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.11.2015 by mbechler
 */
package eu.agno3.runtime.update.internal;


import org.eclipse.equinox.p2.core.IAgentLocation;
import org.eclipse.equinox.p2.core.IProvisioningAgent;


/**
 * @author mbechler
 *
 */
public class NoLocationAgentWrapper implements IProvisioningAgent {

    private IProvisioningAgent delegate;


    /**
     * @param agent
     */
    public NoLocationAgentWrapper ( IProvisioningAgent agent ) {
        this.delegate = agent;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.equinox.p2.core.IProvisioningAgent#getService(java.lang.String)
     */
    @Override
    public Object getService ( String arg0 ) {
        if ( IAgentLocation.SERVICE_NAME.equals(arg0) ) {
            return null;
        }
        return this.delegate.getService(arg0);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.equinox.p2.core.IProvisioningAgent#registerService(java.lang.String, java.lang.Object)
     */
    @Override
    public void registerService ( String arg0, Object arg1 ) {
        this.delegate.registerService(arg0, arg1);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.equinox.p2.core.IProvisioningAgent#stop()
     */
    @Override
    public void stop () {
        this.delegate.stop();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.equinox.p2.core.IProvisioningAgent#unregisterService(java.lang.String, java.lang.Object)
     */
    @Override
    public void unregisterService ( String arg0, Object arg1 ) {
        this.delegate.unregisterService(arg0, arg1);
    }

}
