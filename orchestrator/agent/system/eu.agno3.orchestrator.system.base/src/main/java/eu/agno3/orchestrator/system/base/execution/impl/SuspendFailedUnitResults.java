/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.01.2016 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.impl;


import java.util.List;

import eu.agno3.orchestrator.system.base.execution.ExecutionUnit;
import eu.agno3.orchestrator.system.base.execution.Result;
import eu.agno3.orchestrator.system.base.execution.Status;
import eu.agno3.orchestrator.system.base.execution.UnitResults;
import eu.agno3.orchestrator.system.base.execution.exception.ResultReferenceException;


/**
 * @author mbechler
 *
 */
public class SuspendFailedUnitResults implements UnitResults {

    /**
     * 
     */
    private static final long serialVersionUID = 793117891405091751L;

    private UnitResults res;


    /**
     * @param ur
     * 
     */
    public SuspendFailedUnitResults ( UnitResults ur ) {
        this.res = ur;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.Result#getStatus()
     */
    @Override
    public Status getStatus () {
        return Status.FAIL;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.Result#failed()
     */
    @Override
    public boolean failed () {
        return true;
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
     * @see eu.agno3.orchestrator.system.base.execution.UnitResults#getExecutedUnits()
     */
    @Override
    public List<ExecutionUnit<?, ?, ?>> getExecutedUnits () {
        return this.res.getExecutedUnits();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.UnitResults#getResult(eu.agno3.orchestrator.system.base.execution.ExecutionUnit)
     */
    @Override
    public <T extends Result> T getResult ( ExecutionUnit<T, ?, ?> eu ) throws ResultReferenceException {
        return this.res.getResult(eu);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.UnitResults#add(eu.agno3.orchestrator.system.base.execution.ExecutionUnit,
     *      eu.agno3.orchestrator.system.base.execution.Result)
     */
    @Override
    public void add ( ExecutionUnit<?, ?, ?> eu, Result r ) {
        // ignore
    }

}
