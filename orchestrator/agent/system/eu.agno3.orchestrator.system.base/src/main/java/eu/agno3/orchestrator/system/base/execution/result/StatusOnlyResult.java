/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.result;


import eu.agno3.orchestrator.system.base.execution.Result;
import eu.agno3.orchestrator.system.base.execution.Status;


/**
 * @author mbechler
 * 
 */
public class StatusOnlyResult implements Result {

    /**
     * 
     */
    private static final long serialVersionUID = 2939249250215550350L;
    private final Status status;


    /**
     * @param s
     *            status
     */
    public StatusOnlyResult ( Status s ) {
        this.status = s;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.Result#getStatus()
     */
    @Override
    public Status getStatus () {
        return this.status;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.Result#suspended()
     */
    @Override
    public boolean suspended () {
        return false;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.Result#failed()
     */
    @Override
    public boolean failed () {
        return this.status == Status.FAIL;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return this.status.toString();
    }
}
