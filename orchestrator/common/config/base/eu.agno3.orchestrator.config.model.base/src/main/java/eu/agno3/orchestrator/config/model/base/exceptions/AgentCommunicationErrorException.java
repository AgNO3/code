/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.04.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.exceptions;


import javax.xml.ws.WebFault;

import eu.agno3.orchestrator.config.model.base.exceptions.faults.AgentCommunicationErrorFault;


/**
 * @author mbechler
 *
 */
@WebFault (
    faultBean = "eu.agno3.orchestrator.config.model.base.exceptions.faults.AgentCommunicationErrorFault",
    targetNamespace = "urn:agno3:model:1.0:base:exceptions" )
public class AgentCommunicationErrorException extends AgentException {

    /**
     * 
     */
    private static final long serialVersionUID = 881992599112081243L;


    /**
     * 
     */
    public AgentCommunicationErrorException () {
        super();
    }


    /**
     * @param faultInfo
     */
    public AgentCommunicationErrorException ( AgentCommunicationErrorFault faultInfo ) {
        super(faultInfo);
    }


    /**
     * @param message
     * @param faultInfo
     * @param cause
     */
    public AgentCommunicationErrorException ( String message, AgentCommunicationErrorFault faultInfo, Throwable cause ) {
        super(message, faultInfo, cause);
    }


    /**
     * @param message
     * @param faultInfo
     */
    public AgentCommunicationErrorException ( String message, AgentCommunicationErrorFault faultInfo ) {
        super(message, faultInfo);
    }


    /**
     * @param msg
     * @param t
     */
    public AgentCommunicationErrorException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public AgentCommunicationErrorException ( String msg ) {
        super(msg);
    }


    /**
     * @param t
     */
    public AgentCommunicationErrorException ( Throwable t ) {
        super(t);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.base.exceptions.AgentException#getFaultInfo()
     */
    @Override
    public AgentCommunicationErrorFault getFaultInfo () {
        return (AgentCommunicationErrorFault) super.getFaultInfo();
    }
}
