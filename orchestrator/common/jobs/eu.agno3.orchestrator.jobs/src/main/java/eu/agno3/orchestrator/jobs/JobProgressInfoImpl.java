/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.06.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs;


import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTime;


/**
 * @author mbechler
 * 
 */
public class JobProgressInfoImpl implements JobProgressInfo {

    /**
     * 
     */
    private static final long serialVersionUID = 3244945481819324746L;


    /**
     * 
     */
    public JobProgressInfoImpl () {}


    /**
     * @param obj
     */
    public JobProgressInfoImpl ( JobProgressInfo obj ) {
        this.lastUpdate = obj.getLastUpdate();
        this.progress = obj.getProgress();
        this.stateMessage = obj.getStateMessage();
        this.state = obj.getState();
        this.stateMessageContext = obj.getStateMessageContext();
    }

    private DateTime lastUpdate;
    private float progress = 0.0f;
    private String stateMessage;
    private JobState state;
    private Set<MessageContextEntry> stateMessageContext = new HashSet<>();


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.JobProgressInfo#getLastUpdate()
     */
    @Override
    public DateTime getLastUpdate () {
        return this.lastUpdate;
    }


    /**
     * @param lastUpdate
     *            the lastUpdate to set
     */
    public void setLastUpdate ( DateTime lastUpdate ) {
        this.lastUpdate = lastUpdate;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.JobProgressInfo#getProgress()
     */
    @Override
    public float getProgress () {
        return this.progress;
    }


    /**
     * @param progress
     *            the progress to set
     */
    public void setProgress ( float progress ) {
        this.lastUpdate = DateTime.now();
        this.progress = progress;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.JobProgressInfo#getState()
     */
    @Override
    public JobState getState () {
        return this.state;
    }


    /**
     * @param state
     *            the state to set
     */
    public void setState ( JobState state ) {
        this.state = state;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.JobProgressInfo#getStateMessage()
     */
    @Override
    public String getStateMessage () {
        return this.stateMessage;
    }


    /**
     * @param stateMessage
     *            the stateMessage to set
     */
    public void setStateMessage ( String stateMessage ) {
        this.stateMessage = stateMessage;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.JobProgressInfo#getStateMessageContext()
     */
    @Override
    public Set<MessageContextEntry> getStateMessageContext () {
        return this.stateMessageContext;
    }


    /**
     * @param stateMessageContext
     *            the stateMessageContext to set
     */
    public void setStateMessageContext ( Set<MessageContextEntry> stateMessageContext ) {
        this.stateMessageContext = stateMessageContext;
    }

}