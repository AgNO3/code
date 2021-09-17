/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution;

import java.io.Serializable;


/**
 * @author mbechler
 *
 */
public interface BaseExecutable extends Serializable {

    /**
     * 
     * @return the number of execution units in the executable
     */
    int unitCount ();
}
