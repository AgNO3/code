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
public class EnableService extends AbstractServiceExecutionUnit<EnableService, EnableServiceConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = 7434722437080745445L;
    private boolean immediateStart = true;


    /**
     * @return the immediateStart
     */
    public boolean isImmediateStart () {
        return this.immediateStart;
    }


    /**
     * @param immediateStart
     *            the immediateStart to set
     */
    void setImmediateStart ( boolean immediateStart ) {
        this.immediateStart = immediateStart;
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
            s.enableOnBoot();
        }
        catch ( ServiceException e ) {
            throw new ExecutionException("Failed to enable service" + this.getServiceDescription(), e); //$NON-NLS-1$
        }

        if ( !this.isImmediateStart() ) {
            return false;
        }

        try {
            return s.start();
        }
        catch ( ServiceException e ) {
            throw new ExecutionException("Failed to start service " + this.getServiceDescription(), e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.units.service.AbstractServiceExecutionUnit#execute(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public ServiceResult execute ( Context context ) throws ExecutionException {
        if ( this.isImmediateStart() ) {
            context.getOutput().info("Enabling and starting service " + this.getServiceDescription()); //$NON-NLS-1$
        }
        else {
            context.getOutput().info("Enabling service " + this.getServiceDescription()); //$NON-NLS-1$
        }
        return super.execute(context);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public EnableServiceConfigurator createConfigurator () {
        return new EnableServiceConfigurator(this);
    }

}
