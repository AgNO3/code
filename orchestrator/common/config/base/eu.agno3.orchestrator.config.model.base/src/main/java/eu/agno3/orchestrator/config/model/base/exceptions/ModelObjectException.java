/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.01.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.exceptions;


import java.util.UUID;

import javax.xml.ws.WebFault;

import eu.agno3.orchestrator.config.model.base.AbstractModelException;
import eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelObjectFault;


/**
 * @author mbechler
 * 
 */
@WebFault (
    faultBean = "eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelObjectFault",
    targetNamespace = "urn:agno3:model:1.0:base:exceptions" )
public class ModelObjectException extends AbstractModelException {

    /**
     * 
     */
    private static final long serialVersionUID = -7097643955552019375L;

    private final ModelObjectFault faultInfo;


    /**
     * @param type
     * @param id
     * @param msg
     * @param t
     */
    public ModelObjectException ( Class<?> type, UUID id, String msg, Throwable t ) {
        super(msg, t);
        this.faultInfo = new ModelObjectFault(type, id);
    }


    /**
     * @param type
     * @param id
     * @param t
     */
    public ModelObjectException ( Class<?> type, UUID id, Throwable t ) {
        super(t);
        this.faultInfo = new ModelObjectFault(type, id);
    }


    /**
     * @param type
     * @param id
     * @param msg
     */
    public ModelObjectException ( Class<?> type, UUID id, String msg ) {
        super(msg);
        this.faultInfo = new ModelObjectFault(type, id);
    }


    /**
     * @param type
     * @param id
     */
    public ModelObjectException ( Class<?> type, UUID id ) {
        super();
        this.faultInfo = new ModelObjectFault(type, id);
    }


    protected ModelObjectException ( ModelObjectFault fault, Throwable t ) {
        super(t);
        this.faultInfo = fault;
    }


    protected ModelObjectException ( ModelObjectFault fault ) {
        this.faultInfo = fault;
    }


    /**
     * @param type
     * @param id
     * @param msg
     * @param t
     */
    public ModelObjectException ( String type, String id, String msg, Throwable t ) {
        super(msg, t);
        this.faultInfo = new ModelObjectFault(type, id);
    }


    /**
     * @param type
     * @param id
     * @param t
     */
    public ModelObjectException ( String type, String id, Throwable t ) {
        super(t);
        this.faultInfo = new ModelObjectFault(type, id);
    }


    /**
     * @param type
     * @param id
     * @param msg
     */
    public ModelObjectException ( String type, String id, String msg ) {
        super(msg);
        this.faultInfo = new ModelObjectFault(type, id);
    }


    /**
     * @param type
     * @param id
     */
    public ModelObjectException ( String type, String id ) {
        super();
        this.faultInfo = new ModelObjectFault(type, id);
    }


    /**
     * 
     * @param message
     * @param faultInfo
     */
    public ModelObjectException ( String message, ModelObjectFault faultInfo ) {
        super(message);
        this.faultInfo = faultInfo;
    }


    /**
     * 
     * @param message
     * @param faultInfo
     * @param cause
     */
    public ModelObjectException ( String message, ModelObjectFault faultInfo, Throwable cause ) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }


    /**
     * 
     * @return the fault info
     */
    public ModelObjectFault getFaultInfo () {
        return this.faultInfo;
    }
}
