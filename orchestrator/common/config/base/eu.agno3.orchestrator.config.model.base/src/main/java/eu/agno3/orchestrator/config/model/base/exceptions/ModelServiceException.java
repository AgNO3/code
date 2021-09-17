/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.01.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.exceptions;


import javax.xml.ws.WebFault;

import eu.agno3.orchestrator.config.model.base.AbstractModelException;
import eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelServiceFault;


/**
 * @author mbechler
 * 
 */
@WebFault (
    faultBean = "eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelServiceFault",
    targetNamespace = "urn:agno3:model:1.0:base:exceptions" )
public class ModelServiceException extends AbstractModelException {

    /**
     * 
     */
    private static final long serialVersionUID = -7726577669128129507L;
    private final ModelServiceFault faultInfo;


    /**
     * 
     */
    public ModelServiceException () {
        super();
        this.faultInfo = null;
    }


    /**
     * @param msg
     * @param t
     */
    public ModelServiceException ( String msg, Throwable t ) {
        super(msg, t);
        this.faultInfo = null;
    }


    /**
     * @param msg
     */
    public ModelServiceException ( String msg ) {
        super(msg);
        this.faultInfo = null;
    }


    /**
     * @param t
     */
    public ModelServiceException ( Throwable t ) {
        super(t);
        this.faultInfo = null;
    }


    /**
     * 
     * @param message
     * @param faultInfo
     */
    public ModelServiceException ( String message, ModelServiceFault faultInfo ) {
        super(message);
        this.faultInfo = faultInfo;
    }


    /**
     * 
     * @param message
     * @param faultInfo
     * @param cause
     */
    public ModelServiceException ( String message, ModelServiceFault faultInfo, Throwable cause ) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }


    /**
     * 
     * @return the fault info
     */
    public ModelServiceFault getFaultInfo () {
        return this.faultInfo;
    }

}
