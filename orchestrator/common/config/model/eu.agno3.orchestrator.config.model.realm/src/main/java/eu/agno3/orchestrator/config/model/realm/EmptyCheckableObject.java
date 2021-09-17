/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm;


/**
 * @author mbechler
 *
 */
public interface EmptyCheckableObject {

    /**
     * 
     * @return whether the object is empty (i.e. contains no values)
     */
    boolean isEmpty ();
}
