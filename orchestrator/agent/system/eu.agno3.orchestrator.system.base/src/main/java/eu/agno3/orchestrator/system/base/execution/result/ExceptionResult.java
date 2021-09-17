/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.result;


import org.apache.commons.lang3.StringUtils;

import eu.agno3.orchestrator.system.base.execution.Result;
import eu.agno3.orchestrator.system.base.execution.Status;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;


/**
 * @author mbechler
 * 
 */
public class ExceptionResult implements Result {

    /**
     * 
     */
    private static final long serialVersionUID = -7466373938363021689L;
    private ExecutionException ex;


    /**
     * @param ex
     */
    public ExceptionResult ( ExecutionException ex ) {
        this.ex = ex;
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
     * @return the exception associated with this result
     */
    public ExecutionException getException () {
        return this.ex;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return this.ex != null ? this.ex.toString() : StringUtils.EMPTY;
    }
}
