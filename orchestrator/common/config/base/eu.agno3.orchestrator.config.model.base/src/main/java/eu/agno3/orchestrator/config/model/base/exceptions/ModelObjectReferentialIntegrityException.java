/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.exceptions;


import java.util.UUID;

import javax.xml.ws.WebFault;

import eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelObjectReferentialIntegrityFault;


/**
 * @author mbechler
 * 
 */
@WebFault (
    faultBean = "eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelObjectReferentialIntegrityFault",
    targetNamespace = "urn:agno3:model:1.0:base:exceptions" )
public class ModelObjectReferentialIntegrityException extends ModelObjectException {

    /**
     * 
     */
    private static final long serialVersionUID = -8211388322664927028L;


    /**
     * @param type
     * @param id
     * @param t
     */
    public ModelObjectReferentialIntegrityException ( Class<?> type, UUID id, Throwable t ) {
        super(new ModelObjectReferentialIntegrityFault(type, id), t);
    }


    /**
     * @param type
     * @param id
     */
    public ModelObjectReferentialIntegrityException ( Class<?> type, UUID id ) {
        super(new ModelObjectReferentialIntegrityFault(type, id));
    }


    /**
     * @param type
     * @param id
     * @param t
     */
    public ModelObjectReferentialIntegrityException ( String type, String id, Throwable t ) {
        super(new ModelObjectReferentialIntegrityFault(type, id), t);
    }


    /**
     * @param type
     * @param id
     */
    public ModelObjectReferentialIntegrityException ( String type, String id ) {
        super(new ModelObjectReferentialIntegrityFault(type, id));
    }


    /**
     * @param message
     * @param faultInfo
     * @param cause
     */
    public ModelObjectReferentialIntegrityException ( String message, ModelObjectReferentialIntegrityFault faultInfo, Throwable cause ) {
        super(message, faultInfo, cause);
    }


    /**
     * @param message
     * @param faultInfo
     */
    public ModelObjectReferentialIntegrityException ( String message, ModelObjectReferentialIntegrityFault faultInfo ) {
        super(message, faultInfo);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException#getFaultInfo()
     */
    @Override
    public ModelObjectReferentialIntegrityFault getFaultInfo () {
        return (ModelObjectReferentialIntegrityFault) super.getFaultInfo();
    }

}
