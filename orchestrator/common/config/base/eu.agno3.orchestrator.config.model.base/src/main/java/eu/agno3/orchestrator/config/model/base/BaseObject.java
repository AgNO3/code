/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.base;


import java.io.Serializable;
import java.util.UUID;


/**
 * @author mbechler
 * 
 */
public interface BaseObject extends Serializable {

    /**
     * @return the object id
     */
    UUID getId ();


    /**
     * @return the object version (local revision, for optimisitic locking)
     */
    Long getVersion ();

}