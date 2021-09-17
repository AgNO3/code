/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.04.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.exceptions;


import javax.xml.ws.WebFault;

import eu.agno3.orchestrator.config.model.base.AbstractModelException;
import eu.agno3.orchestrator.config.model.base.exceptions.faults.AgentFault;


/**
 * @author mbechler
 *
 */
@WebFault (
    faultBean = "eu.agno3.orchestrator.config.model.base.exceptions.faults.AgentFault",
    targetNamespace = "urn:agno3:model:1.0:base:exceptions" )
public class AgentException extends AbstractModelException {

    /**
     * 
     */
    private static final long serialVersionUID = 881992599112081243L;
    private AgentFault faultInfo;


    /**
     * 
     */
    public AgentException () {
        super();
    }


    /**
     * @param msg
     * @param t
     */
    public AgentException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public AgentException ( String msg ) {
        super(msg);
    }


    /**
     * @param t
     */
    public AgentException ( Throwable t ) {
        super(t);
    }


    /**
     * 
     * @param faultInfo
     */
    public AgentException ( AgentFault faultInfo ) {
        super();
        this.faultInfo = faultInfo;
    }


    /**
     * 
     * @param message
     * @param faultInfo
     */
    public AgentException ( String message, AgentFault faultInfo ) {
        super(message);
        this.faultInfo = faultInfo;
    }


    /**
     * 
     * @param message
     * @param faultInfo
     * @param cause
     */
    public AgentException ( String message, AgentFault faultInfo, Throwable cause ) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }


    /**
     * 
     * @return the fault info
     */
    public AgentFault getFaultInfo () {
        return this.faultInfo;
    }

}
