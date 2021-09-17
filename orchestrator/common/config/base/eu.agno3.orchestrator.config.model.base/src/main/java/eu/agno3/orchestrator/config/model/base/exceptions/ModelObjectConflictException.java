/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.01.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.exceptions;


import java.util.UUID;

import javax.xml.ws.WebFault;

import eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelObjectConflictFault;


/**
 * @author mbechler
 * 
 */
@WebFault (
    faultBean = "eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelObjectConflictFault",
    targetNamespace = "urn:agno3:model:1.0:base:exceptions" )
public class ModelObjectConflictException extends ModelObjectException {

    /**
     * 
     */
    private static final long serialVersionUID = -7185385487908471189L;


    /**
     * @param type
     * @param id
     * @param t
     */
    public ModelObjectConflictException ( Class<?> type, UUID id, Throwable t ) {
        super(new ModelObjectConflictFault(type, id), t);
    }


    /**
     * @param type
     * @param id
     */
    public ModelObjectConflictException ( Class<?> type, UUID id ) {
        super(new ModelObjectConflictFault(type, id));
    }


    /**
     * @param type
     * @param id
     * @param t
     */
    public ModelObjectConflictException ( String type, String id, Throwable t ) {
        super(new ModelObjectConflictFault(type, id), t);
    }


    /**
     * @param type
     * @param id
     */
    public ModelObjectConflictException ( String type, String id ) {
        super(new ModelObjectConflictFault(type, id));
    }


    /**
     * 
     * @param message
     * @param faultInfo
     */
    public ModelObjectConflictException ( String message, ModelObjectConflictFault faultInfo ) {
        super(message, faultInfo);
    }


    /**
     * 
     * @param message
     * @param faultInfo
     * @param cause
     */
    public ModelObjectConflictException ( String message, ModelObjectConflictFault faultInfo, Throwable cause ) {
        super(message, faultInfo, cause);
    }


    protected ModelObjectConflictException ( ModelObjectConflictFault faultInfo, Throwable cause ) {
        super(faultInfo, cause);
    }


    protected ModelObjectConflictException ( ModelObjectConflictFault faultInfo ) {
        super(faultInfo);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException#getFaultInfo()
     */
    @Override
    public ModelObjectConflictFault getFaultInfo () {
        return (ModelObjectConflictFault) super.getFaultInfo();
    }

}
