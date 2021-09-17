/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.04.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.exceptions;


import javax.xml.ws.WebFault;

import eu.agno3.orchestrator.config.model.base.exceptions.faults.AgentDetachedFault;


/**
 * @author mbechler
 *
 */
@WebFault (
    faultBean = "eu.agno3.orchestrator.config.model.base.exceptions.faults.AgentDetachedFault",
    targetNamespace = "urn:agno3:model:1.0:base:exceptions" )
public class AgentDetachedException extends AgentException {

    /**
     * 
     */
    private static final long serialVersionUID = 881992599112081243L;


    /**
     * 
     */
    public AgentDetachedException () {
        super();
    }


    /**
     * @param faultInfo
     */
    public AgentDetachedException ( AgentDetachedFault faultInfo ) {
        super(faultInfo);
    }


    /**
     * @param message
     * @param faultInfo
     * @param cause
     */
    public AgentDetachedException ( String message, AgentDetachedFault faultInfo, Throwable cause ) {
        super(message, faultInfo, cause);
    }


    /**
     * @param message
     * @param faultInfo
     */
    public AgentDetachedException ( String message, AgentDetachedFault faultInfo ) {
        super(message, faultInfo);
    }


    /**
     * @param msg
     * @param t
     */
    public AgentDetachedException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public AgentDetachedException ( String msg ) {
        super(msg);
    }


    /**
     * @param t
     */
    public AgentDetachedException ( Throwable t ) {
        super(t);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.base.exceptions.AgentException#getFaultInfo()
     */
    @Override
    public AgentDetachedFault getFaultInfo () {
        return (AgentDetachedFault) super.getFaultInfo();
    }
}
