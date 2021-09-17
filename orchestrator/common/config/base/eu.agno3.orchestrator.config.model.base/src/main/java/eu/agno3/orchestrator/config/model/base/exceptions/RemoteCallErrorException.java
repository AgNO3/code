/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.04.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.exceptions;


import javax.xml.ws.WebFault;

import eu.agno3.orchestrator.config.model.base.exceptions.faults.RemoteCallErrorFault;


/**
 * @author mbechler
 *
 */
@WebFault (
    faultBean = "eu.agno3.orchestrator.config.model.base.exceptions.faults.RemoteCallErrorFault",
    targetNamespace = "urn:agno3:model:1.0:base:exceptions" )
public class RemoteCallErrorException extends AgentCommunicationErrorException {

    /**
     * 
     */
    private static final long serialVersionUID = 881992599112081243L;


    /**
     * 
     */
    public RemoteCallErrorException () {
        super();
    }


    /**
     * @param faultInfo
     */
    public RemoteCallErrorException ( RemoteCallErrorFault faultInfo ) {
        super(faultInfo);
    }


    /**
     * @param message
     * @param faultInfo
     * @param cause
     */
    public RemoteCallErrorException ( String message, RemoteCallErrorFault faultInfo, Throwable cause ) {
        super(message, faultInfo, cause);
    }


    /**
     * @param message
     * @param faultInfo
     */
    public RemoteCallErrorException ( String message, RemoteCallErrorFault faultInfo ) {
        super(message, faultInfo);
    }


    /**
     * @param msg
     * @param t
     */
    public RemoteCallErrorException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public RemoteCallErrorException ( String msg ) {
        super(msg);
    }


    /**
     * @param t
     */
    public RemoteCallErrorException ( Throwable t ) {
        super(t);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.base.exceptions.AgentException#getFaultInfo()
     */
    @Override
    public RemoteCallErrorFault getFaultInfo () {
        return (RemoteCallErrorFault) super.getFaultInfo();
    }
}
