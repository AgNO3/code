/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.exceptions.faults;


import java.util.UUID;


/**
 * @author mbechler
 * 
 */
public class ModelObjectNotFoundFault extends ModelObjectFault {

    /**
     * 
     */
    public ModelObjectNotFoundFault () {}


    /**
     * @param objectType
     * @param id
     */
    public ModelObjectNotFoundFault ( String objectType, String id ) {
        super(objectType, id);
    }


    /**
     * @param type
     * @param id
     */
    public ModelObjectNotFoundFault ( Class<?> type, UUID id ) {
        super(type, id);
    }

}
