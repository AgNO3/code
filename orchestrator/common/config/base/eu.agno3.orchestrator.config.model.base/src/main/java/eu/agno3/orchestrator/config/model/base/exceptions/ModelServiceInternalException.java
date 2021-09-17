/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.01.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.exceptions;


import javax.xml.ws.WebFault;

import eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelServiceInternalFault;


/**
 * @author mbechler
 * 
 */
@WebFault (
    faultBean = "eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelServiceInternalFault",
    targetNamespace = "urn:agno3:model:1.0:base:exceptions" )
public class ModelServiceInternalException extends ModelServiceException {

    /**
     * 
     */
    private static final long serialVersionUID = -1895865352320188926L;


    /**
     * 
     */
    public ModelServiceInternalException () {
        super();
    }


    /**
     * @param msg
     * @param t
     */
    public ModelServiceInternalException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public ModelServiceInternalException ( String msg ) {
        super(msg);
    }


    /**
     * @param t
     */
    public ModelServiceInternalException ( Throwable t ) {
        super(t);
    }


    /**
     * 
     * @param message
     * @param faultInfo
     */
    public ModelServiceInternalException ( String message, ModelServiceInternalFault faultInfo ) {
        super(message, faultInfo);
    }


    /**
     * 
     * @param message
     * @param faultInfo
     * @param cause
     */
    public ModelServiceInternalException ( String message, ModelServiceInternalFault faultInfo, Throwable cause ) {
        super(message, faultInfo, cause);
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException#getFaultInfo()
     */
    @Override
    public ModelServiceInternalFault getFaultInfo () {
        return (ModelServiceInternalFault) super.getFaultInfo();
    }

}
