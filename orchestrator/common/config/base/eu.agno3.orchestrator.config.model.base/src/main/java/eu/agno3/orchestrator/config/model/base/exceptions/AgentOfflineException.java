/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.04.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.exceptions;


import javax.xml.ws.WebFault;

import eu.agno3.orchestrator.config.model.base.exceptions.faults.AgentOfflineFault;


/**
 * @author mbechler
 *
 */
@WebFault (
    faultBean = "eu.agno3.orchestrator.config.model.base.exceptions.faults.AgentOfflineFault",
    targetNamespace = "urn:agno3:model:1.0:base:exceptions" )
public class AgentOfflineException extends AgentException {

    /**
     * 
     */
    private static final long serialVersionUID = 881992599112081243L;


    /**
     * 
     */
    public AgentOfflineException () {
        super();
    }


    /**
     * @param faultInfo
     */
    public AgentOfflineException ( AgentOfflineFault faultInfo ) {
        super(faultInfo);
    }


    /**
     * @param message
     * @param faultInfo
     * @param cause
     */
    public AgentOfflineException ( String message, AgentOfflineFault faultInfo, Throwable cause ) {
        super(message, faultInfo, cause);
    }


    /**
     * @param message
     * @param faultInfo
     */
    public AgentOfflineException ( String message, AgentOfflineFault faultInfo ) {
        super(message, faultInfo);
    }


    /**
     * @param msg
     * @param t
     */
    public AgentOfflineException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public AgentOfflineException ( String msg ) {
        super(msg);
    }


    /**
     * @param t
     */
    public AgentOfflineException ( Throwable t ) {
        super(t);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.base.exceptions.AgentException#getFaultInfo()
     */
    @Override
    public AgentOfflineFault getFaultInfo () {
        return (AgentOfflineFault) super.getFaultInfo();
    }
}
