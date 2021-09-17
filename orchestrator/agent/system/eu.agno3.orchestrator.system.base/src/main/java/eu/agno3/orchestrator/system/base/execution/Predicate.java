/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution;


import java.io.Serializable;


/**
 * @author mbechler
 * 
 */
public interface Predicate extends Serializable {

    /**
     * @param context
     * @return whether this predicate is true
     */
    boolean evaluate ( Context context );

}
