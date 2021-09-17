/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.service;


import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.service.Service;
import eu.agno3.orchestrator.system.base.service.ServiceException;
import eu.agno3.orchestrator.system.base.service.ServiceResult;


/**
 * @author mbechler
 * 
 */
public class DisableService extends AbstractServiceExecutionUnit<DisableService, DisableServiceConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = 4968137433844733875L;
    private boolean immediateStop = true;


    /**
     * @return the immediateStart
     */
    public boolean isImmediateStop () {
        return this.immediateStop;
    }


    /**
     * @param immediateStop
     *            the immediateStop to set
     */
    void setImmediateStop ( boolean immediateStop ) {
        this.immediateStop = immediateStop;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.units.service.AbstractServiceExecutionUnit#doWithService(eu.agno3.orchestrator.system.base.execution.Context,
     *      eu.agno3.orchestrator.system.base.service.Service)
     */
    @Override
    protected boolean doWithService ( Context context, Service s ) throws ExecutionException {

        try {
            s.disableOnBoot();
        }
        catch ( ServiceException e ) {
            throw new ExecutionException("Failed to disable service" + this.getServiceDescription(), e); //$NON-NLS-1$
        }

        if ( !this.isImmediateStop() ) {
            return false;
        }

        try {
            return s.stop();
        }
        catch ( ServiceException e ) {
            throw new ExecutionException("Failed to stop service " + this.getServiceDescription(), e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.units.service.AbstractServiceExecutionUnit#execute(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public ServiceResult execute ( Context context ) throws ExecutionException {
        if ( this.isImmediateStop() ) {
            context.getOutput().info("Disabling and stopping service " + this.getServiceDescription()); //$NON-NLS-1$
        }
        else {
            context.getOutput().info("Disabling service " + this.getServiceDescription()); //$NON-NLS-1$
        }
        return super.execute(context);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public DisableServiceConfigurator createConfigurator () {
        return new DisableServiceConfigurator(this);
    }

}
