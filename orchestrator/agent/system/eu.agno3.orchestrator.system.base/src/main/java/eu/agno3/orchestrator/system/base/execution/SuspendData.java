/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution;


import java.io.Serializable;
import java.util.Iterator;


/**
 * @author mbechler
 *
 */
public interface SuspendData extends Serializable {

    /**
     * @return the remaining units for the current phase
     */
    Iterator<ExecutionUnit<?, ?, ?>> getRemainIterator ();


    /**
     * @return the job
     */
    Job getJob ();

}
