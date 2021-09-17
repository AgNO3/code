/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.04.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.exceptions.faults;


import java.io.Serializable;
import java.util.UUID;


/**
 * @author mbechler
 *
 */
public class AgentFault implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5916715706848217048L;
    private String agentId;
    private String instanceName;


    /**
     * 
     */
    public AgentFault () {}


    /**
     * @param agentId
     * @param instanceName
     * 
     */
    public AgentFault ( UUID agentId, String instanceName ) {
        if ( agentId != null ) {
            this.agentId = agentId.toString();
        }
        this.instanceName = instanceName;
    }


    /**
     * @param agentId
     * @param instanceName
     * 
     */
    public AgentFault ( String agentId, String instanceName ) {
        this.agentId = agentId;
        this.instanceName = instanceName;
    }


    /**
     * @return the agentId
     */
    public String getAgentId () {
        return this.agentId;
    }


    /**
     * @param agentId
     *            the agentId to set
     */
    public void setAgentId ( String agentId ) {
        this.agentId = agentId;
    }


    /**
     * @return the instanceName
     */
    public String getInstanceName () {
        return this.instanceName;
    }


    /**
     * @param instanceName
     *            the instanceName to set
     */
    public void setInstanceName ( String instanceName ) {
        this.instanceName = instanceName;
    }
}
