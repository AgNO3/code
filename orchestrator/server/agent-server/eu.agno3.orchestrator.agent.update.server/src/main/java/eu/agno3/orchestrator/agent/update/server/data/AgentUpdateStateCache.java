/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.12.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.update.server.data;


import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Version;

import org.joda.time.DateTime;

import eu.agno3.orchestrator.system.update.UpdateState;


/**
 * @author mbechler
 *
 */
@PersistenceUnit ( unitName = "orchestrator" )
@Entity
@Table ( name = "agent_update_cache" )
public class AgentUpdateStateCache {

    private UUID agentId;
    private long version;

    private UpdateState currentState;

    private String currentStream;
    private Long currentSequence;
    private boolean rebootIndicated;

    private DateTime lastUpdated;


    /**
     * 
     * @return the object id
     */
    @Id
    @Column ( length = 16 )
    public UUID getAgentId () {
        return this.agentId;
    }


    /**
     * @param id
     *            the id to set
     */
    public void setAgentId ( UUID id ) {
        this.agentId = id;
    }


    /**
     * 
     * @return the version
     */
    @Version
    public long getVersion () {
        return this.version;
    }


    /**
     * @param version
     *            the version to set
     */
    public void setVersion ( long version ) {
        this.version = version;
    }


    /**
     * @return the currentState
     */
    @Enumerated ( EnumType.STRING )
    public UpdateState getCurrentState () {
        return this.currentState;
    }


    /**
     * @param currentState
     *            the currentState to set
     */
    public void setCurrentState ( UpdateState currentState ) {
        this.currentState = currentState;
    }


    /**
     * @return the currentSequence
     */
    public Long getCurrentSequence () {
        return this.currentSequence;
    }


    /**
     * @param currentSequence
     *            the currentSequence to set
     */
    public void setCurrentSequence ( Long currentSequence ) {
        this.currentSequence = currentSequence;
    }


    /**
     * @return the currentStream
     */
    public String getCurrentStream () {
        return this.currentStream;
    }


    /**
     * @param currentStream
     *            the currentStream to set
     */
    public void setCurrentStream ( String currentStream ) {
        this.currentStream = currentStream;
    }


    /**
     * @return the rebootIndicated
     */
    public boolean getRebootIndicated () {
        return this.rebootIndicated;
    }


    /**
     * @param rebootIndicated
     *            the rebootIndicated to set
     */
    public void setRebootIndicated ( boolean rebootIndicated ) {
        this.rebootIndicated = rebootIndicated;
    }


    /**
     * @return the lastUpdated
     */
    public DateTime getLastUpdated () {
        return this.lastUpdated;
    }


    /**
     * @param lastUpdated
     *            the lastUpdated to set
     */
    public void setLastUpdated ( DateTime lastUpdated ) {
        this.lastUpdated = lastUpdated;
    }
}
