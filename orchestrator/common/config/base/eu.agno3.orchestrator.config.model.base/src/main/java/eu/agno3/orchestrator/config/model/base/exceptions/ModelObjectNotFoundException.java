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
import eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelObjectNotFoundFault;


/**
 * @author mbechler
 * 
 */
@WebFault (
    faultBean = "eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelObjectNotFoundFault",
    targetNamespace = "urn:agno3:model:1.0:base:exceptions" )
public class ModelObjectNotFoundException extends ModelObjectException {

    /**
     * 
     */
    private static final long serialVersionUID = -6125816876604510854L;


    /**
     * @param type
     * @param id
     * @param t
     */
    public ModelObjectNotFoundException ( Class<? extends BaseObject> type, UUID id, Throwable t ) {
        super(new ModelObjectNotFoundFault(type, id), t);
    }


    /**
     * @param type
     * @param id
     */
    public ModelObjectNotFoundException ( Class<? extends BaseObject> type, UUID id ) {
        super(new ModelObjectNotFoundFault(type, id));
    }


    /**
     * @param fault
     * @param t
     */
    public ModelObjectNotFoundException ( ModelObjectNotFoundFault fault, Throwable t ) {
        super(fault, t);
    }


    /**
     * @param fault
     */
    public ModelObjectNotFoundException ( ModelObjectNotFoundFault fault ) {
        super(fault);
    }


    /**
     * @param message
     * @param faultInfo
     * @param cause
     */
    public ModelObjectNotFoundException ( String message, ModelObjectNotFoundFault faultInfo, Throwable cause ) {
        super(message, faultInfo, cause);
    }


    /**
     * @param message
     * @param faultInfo
     */
    public ModelObjectNotFoundException ( String message, ModelObjectNotFoundFault faultInfo ) {
        super(message, faultInfo);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException#getFaultInfo()
     */
    @Override
    public ModelObjectNotFoundFault getFaultInfo () {
        return (ModelObjectNotFoundFault) super.getFaultInfo();
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Throwable#getMessage()
     */
    @Override
    public String getMessage () {
        return String.format("Object of type '%s' with ID '%s' could not be found.",//$NON-NLS-1$
            this.getFaultInfo().getObjectType(),
            this.getFaultInfo().getId());
    }

}
