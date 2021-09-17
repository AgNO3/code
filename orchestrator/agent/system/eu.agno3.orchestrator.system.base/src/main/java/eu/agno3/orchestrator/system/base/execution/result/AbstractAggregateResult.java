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
public abstract class AbstractAggregateResult implements Result {

    /**
     * 
     */
    private static final long serialVersionUID = -2897817110124882534L;

    private Status overallStatus = Status.SKIPPED;


    /**
     * @param r
     *            Add the result to this aggregation
     */
    protected void add ( Result r ) {
        if ( r != null ) {
            this.overallStatus = this.overallStatus.and(r.getStatus());
        }
    }


    /**
     * @return whether the overall status indicates failure
     */
    @Override
    public boolean failed () {
        return this.getStatus() == Status.FAIL;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.Result#getStatus()
     */
    @Override
    public Status getStatus () {
        return this.overallStatus;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return "Aggregate result: " + this.getStatus(); //$NON-NLS-1$
    }

}
