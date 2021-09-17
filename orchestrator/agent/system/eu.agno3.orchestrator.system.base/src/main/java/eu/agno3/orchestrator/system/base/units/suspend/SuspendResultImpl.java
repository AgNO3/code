/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.suspend;


import eu.agno3.orchestrator.system.base.execution.Status;
import eu.agno3.orchestrator.system.base.execution.SuspendData;
import eu.agno3.orchestrator.system.base.execution.SuspendResult;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class SuspendResultImpl extends StatusOnlyResult implements SuspendResult {

    /**
     * 
     */
    private static final long serialVersionUID = -4638989514805463963L;
    private int after;


    /**
     * @param after
     */
    public SuspendResultImpl ( int after ) {
        super(Status.SUCCESS);
        this.after = after;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.SuspendResult#getSuspendAfter()
     */
    @Override
    public int getSuspendAfter () {
        return this.after;
    }


    @Override
    public boolean suspended () {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.SuspendResult#getSuspendData()
     */
    @Override
    public SuspendData getSuspendData () {
        return null;
    }

}
