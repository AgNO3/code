/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.11.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm;


import java.io.Serializable;
import java.util.UUID;


/**
 * @author mbechler
 *
 */
public interface StructuralObjectReference extends Serializable {

    /**
     * @return the id
     */
    UUID getId ();


    /**
     * @return the type
     */
    StructuralObjectType getType ();


    /**
     * @return the localType
     */
    String getLocalType ();

}