/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.09.2015 by mbechler
 */
package eu.agno3.runtime.update.internal;


import org.eclipse.equinox.p2.core.IProvisioningAgent;


/**
 * @author mbechler
 *
 */
public class ClosableProvisioningAgent implements IProvisioningAgent, AutoCloseable {

    private IProvisioningAgent delegate;


    /**
     * @param agent
     * 
     */
    public ClosableProvisioningAgent ( IProvisioningAgent agent ) {
        this.delegate = agent;

    }


    /**
     * @param arg0
     * @return service
     * @see org.eclipse.equinox.p2.core.IProvisioningAgent#getService(java.lang.String)
     */
    @Override
    public Object getService ( String arg0 ) {
        return this.delegate.getService(arg0);
    }


    /**
     * @param arg0
     * @param arg1
     * @see org.eclipse.equinox.p2.core.IProvisioningAgent#registerService(java.lang.String, java.lang.Object)
     */
    @Override
    public void registerService ( String arg0, Object arg1 ) {
        this.delegate.registerService(arg0, arg1);
    }


    /**
     * 
     * @see org.eclipse.equinox.p2.core.IProvisioningAgent#stop()
     */
    @Override
    public void stop () {
        this.delegate.stop();
    }


    /**
     * @param arg0
     * @param arg1
     * @see org.eclipse.equinox.p2.core.IProvisioningAgent#unregisterService(java.lang.String, java.lang.Object)
     */
    @Override
    public void unregisterService ( String arg0, Object arg1 ) {
        this.delegate.unregisterService(arg0, arg1);
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close () {
        stop();
    }

}
