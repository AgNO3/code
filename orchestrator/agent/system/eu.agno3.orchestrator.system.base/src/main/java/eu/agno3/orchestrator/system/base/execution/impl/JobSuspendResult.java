/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.impl;


import eu.agno3.orchestrator.system.base.execution.Result;
import eu.agno3.orchestrator.system.base.execution.Status;


/**
 * @author mbechler
 *
 */
public class JobSuspendResult implements Result {

    /**
     * 
     */
    private static final long serialVersionUID = -7646542036323480677L;
    private Result result;


    /**
     * @param result
     */
    public JobSuspendResult ( Result result ) {
        this.result = result;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.Result#getStatus()
     */
    @Override
    public Status getStatus () {
        return this.result.getStatus();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.Result#failed()
     */
    @Override
    public boolean failed () {
        return this.result.failed();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.Result#suspended()
     */
    @Override
    public boolean suspended () {
        return true;
    }

}
