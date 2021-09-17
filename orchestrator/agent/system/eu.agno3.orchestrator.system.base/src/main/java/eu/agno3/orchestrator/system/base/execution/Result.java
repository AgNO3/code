/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution;


import java.io.Serializable;


/**
 * @author mbechler
 * 
 */
public interface Result extends Serializable {

    /**
     * @return get the execution status
     */
    Status getStatus ();


    /**
     * @return whether this is a failure result
     */
    boolean failed ();


    /**
     * 
     * @return whether this is a suspend result
     */
    boolean suspended ();
}
