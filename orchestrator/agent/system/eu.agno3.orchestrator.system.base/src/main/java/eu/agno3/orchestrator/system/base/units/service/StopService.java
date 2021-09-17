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
public class StopService extends AbstractServiceExecutionUnit<StopService, StopServiceConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = -3787909632584239521L;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.units.service.AbstractServiceExecutionUnit#doWithService(eu.agno3.orchestrator.system.base.execution.Context,
     *      eu.agno3.orchestrator.system.base.service.Service)
     */
    @Override
    protected boolean doWithService ( Context context, Service s ) throws ExecutionException {
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
        context.getOutput().info("Stopping service " + this.getServiceDescription()); //$NON-NLS-1$
        return super.execute(context);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public StopServiceConfigurator createConfigurator () {
        return new StopServiceConfigurator(this);
    }

}
