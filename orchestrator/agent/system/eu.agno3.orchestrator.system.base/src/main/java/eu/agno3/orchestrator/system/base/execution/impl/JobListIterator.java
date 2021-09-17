/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.impl;


import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import eu.agno3.orchestrator.system.base.execution.ExecutionUnit;


/**
 * @author mbechler
 *
 */
public class JobListIterator implements Iterator<ExecutionUnit<?, ?, ?>>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3633595696379644279L;

    private int pos;
    private List<ExecutionUnit<?, ?, ?>> units;


    /**
     * @param units
     */
    public JobListIterator ( List<ExecutionUnit<?, ?, ?>> units ) {
        this.units = units;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext () {
        return this.pos < this.units.size();
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.Iterator#next()
     */
    @Override
    public ExecutionUnit<?, ?, ?> next () {
        return this.units.get(this.pos++);
    }

}
