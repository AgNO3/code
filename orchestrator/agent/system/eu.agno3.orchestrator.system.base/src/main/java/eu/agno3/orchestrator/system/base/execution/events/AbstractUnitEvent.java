/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.events;


import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.ExecutionUnit;


/**
 * @author mbechler
 * 
 */
public abstract class AbstractUnitEvent extends AbstractExecutorEvent {

    private ExecutionUnit<?, ?, ?> unit;


    /**
     * @param context
     * @param unit
     */
    public AbstractUnitEvent ( Context context, ExecutionUnit<?, ?, ?> unit ) {
        super(context);
        this.unit = unit;
    }


    /**
     * @return the execution unit
     */
    public ExecutionUnit<?, ?, ?> getUnit () {
        return this.unit;
    }

}