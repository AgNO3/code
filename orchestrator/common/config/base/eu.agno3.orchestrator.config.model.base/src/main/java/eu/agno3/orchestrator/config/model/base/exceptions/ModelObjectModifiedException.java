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
import eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelObjectModifiedFault;


/**
 * @author mbechler
 * 
 */
@WebFault (
    faultBean = "eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelObjectModifiedFault",
    targetNamespace = "urn:agno3:model:1.0:base:exceptions" )
public class ModelObjectModifiedException extends ModelObjectConflictException {

    /**
     * 
     */
    private static final long serialVersionUID = 5015752580078130238L;


    /**
     * @param type
     * @param id
     * @param t
     */
    public ModelObjectModifiedException ( Class<? extends BaseObject> type, UUID id, Throwable t ) {
        super(new ModelObjectModifiedFault(type, id), t);
    }


    /**
     * @param type
     * @param id
     */
    public ModelObjectModifiedException ( Class<? extends BaseObject> type, UUID id ) {
        super(new ModelObjectModifiedFault(type, id));
    }


    /**
     * @param faultInfo
     * @param cause
     */
    public ModelObjectModifiedException ( ModelObjectModifiedFault faultInfo, Throwable cause ) {
        super(faultInfo, cause);
    }


    /**
     * @param faultInfo
     */
    public ModelObjectModifiedException ( ModelObjectModifiedFault faultInfo ) {
        super(faultInfo);
    }


    /**
     * @param message
     * @param faultInfo
     * @param cause
     */
    public ModelObjectModifiedException ( String message, ModelObjectModifiedFault faultInfo, Throwable cause ) {
        super(message, faultInfo, cause);
    }


    /**
     * @param message
     * @param faultInfo
     */
    public ModelObjectModifiedException ( String message, ModelObjectModifiedFault faultInfo ) {
        super(message, faultInfo);
    }


    /**
     * @param type
     * @param id
     * @param t
     */
    public ModelObjectModifiedException ( String type, String id, Throwable t ) {
        super(new ModelObjectModifiedFault(type, id), t);
    }


    /**
     * @param type
     * @param id
     */
    public ModelObjectModifiedException ( String type, String id ) {
        super(new ModelObjectModifiedFault(type, id));
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectConflictException#getFaultInfo()
     */
    @Override
    public ModelObjectModifiedFault getFaultInfo () {
        return (ModelObjectModifiedFault) super.getFaultInfo();
    }

}
