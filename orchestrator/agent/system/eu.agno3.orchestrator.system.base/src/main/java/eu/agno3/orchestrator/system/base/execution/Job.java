/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution;


import java.io.Serializable;


/**
 * @author mbechler
 * 
 */
public interface Job extends Named, Serializable {

    /**
     * @return this jobs execution units
     */
    JobIterator getExecutionUnits ();


    /**
     * @return the job flags
     */
    String[] getFlags ();

}
