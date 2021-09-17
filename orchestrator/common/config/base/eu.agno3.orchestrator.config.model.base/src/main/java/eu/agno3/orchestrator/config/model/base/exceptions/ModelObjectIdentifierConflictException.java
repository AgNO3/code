/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.01.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.exceptions;


import java.util.UUID;

import javax.xml.ws.WebFault;

import eu.agno3.orchestrator.config.model.base.BaseObject;
import eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelObjectIdentifierConflictFault;


/**
 * @author mbechler
 * 
 */
@WebFault (
    faultBean = "eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelObjectIdentifierConflictFault",
    targetNamespace = "urn:agno3:model:1.0:base:exceptions" )
public class ModelObjectIdentifierConflictException extends ModelObjectConflictException {

    /**
     * 
     */
    private static final long serialVersionUID = 8406234240256704992L;


    /**
     * @param type
     * @param id
     * @param t
     */
    public ModelObjectIdentifierConflictException ( Class<? extends BaseObject> type, UUID id, Throwable t ) {
        super(new ModelObjectIdentifierConflictFault(type, id), t);
    }


    /**
     * @param type
     * @param id
     */
    public ModelObjectIdentifierConflictException ( Class<? extends BaseObject> type, UUID id ) {
        super(new ModelObjectIdentifierConflictFault(type, id));
    }


    /**
     * @param type
     * @param id
     * @param t
     */
    public ModelObjectIdentifierConflictException ( String type, String id, Throwable t ) {
        super(new ModelObjectIdentifierConflictFault(type, id), t);
    }


    /**
     * @param type
     * @param id
     */
    public ModelObjectIdentifierConflictException ( String type, String id ) {
        super(new ModelObjectIdentifierConflictFault(type, id));
    }


    /**
     * @param message
     * @param faultInfo
     * @param cause
     */
    public ModelObjectIdentifierConflictException ( String message, ModelObjectIdentifierConflictFault faultInfo, Throwable cause ) {
        super(message, faultInfo, cause);
    }


    /**
     * @param message
     * @param faultInfo
     */
    public ModelObjectIdentifierConflictException ( String message, ModelObjectIdentifierConflictFault faultInfo ) {
        super(message, faultInfo);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectConflictException#getFaultInfo()
     */
    @Override
    public ModelObjectIdentifierConflictFault getFaultInfo () {
        return (ModelObjectIdentifierConflictFault) super.getFaultInfo();
    }

}
