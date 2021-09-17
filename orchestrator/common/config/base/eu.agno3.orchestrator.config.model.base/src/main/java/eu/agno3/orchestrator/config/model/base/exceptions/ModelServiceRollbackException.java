/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.01.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.exceptions;


import javax.xml.ws.WebFault;

import eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelServiceRollbackFault;


/**
 * @author mbechler
 * 
 */
@WebFault (
    faultBean = "eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelServiceRollbackFault",
    targetNamespace = "urn:agno3:model:1.0:base:exceptions" )
public class ModelServiceRollbackException extends ModelServiceException {

    /**
     * 
     */
    private static final long serialVersionUID = -7461754954256337056L;


    /**
     * 
     */
    public ModelServiceRollbackException () {}


    /**
     * @param msg
     * @param t
     */
    public ModelServiceRollbackException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public ModelServiceRollbackException ( String msg ) {
        super(msg);
    }


    /**
     * @param t
     */
    public ModelServiceRollbackException ( Throwable t ) {
        super(t);
    }


    /**
     * @param message
     * @param faultInfo
     * @param cause
     */
    public ModelServiceRollbackException ( String message, ModelServiceRollbackFault faultInfo, Throwable cause ) {
        super(message, faultInfo, cause);
    }


    /**
     * @param message
     * @param faultInfo
     */
    public ModelServiceRollbackException ( String message, ModelServiceRollbackFault faultInfo ) {
        super(message, faultInfo);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException#getFaultInfo()
     */
    @Override
    public ModelServiceRollbackFault getFaultInfo () {
        return (ModelServiceRollbackFault) super.getFaultInfo();
    }

}
