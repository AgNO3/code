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
public class ModelObjectIdentifierConflictFault extends ModelObjectConflictFault {

    /**
     * 
     */
    public ModelObjectIdentifierConflictFault () {}


    /**
     * @param objectType
     * @param id
     */
    public ModelObjectIdentifierConflictFault ( String objectType, String id ) {
        super(objectType, id);
    }


    /**
     * @param type
     * @param id
     */
    public ModelObjectIdentifierConflictFault ( Class<?> type, UUID id ) {
        super(type, id);
    }

}
