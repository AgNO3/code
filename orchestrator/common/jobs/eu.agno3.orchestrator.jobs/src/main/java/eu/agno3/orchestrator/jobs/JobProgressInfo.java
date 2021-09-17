/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.06.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs;


import java.io.Serializable;
import java.util.Set;

import org.joda.time.DateTime;


/**
 * @author mbechler
 * 
 */
public interface JobProgressInfo extends Serializable {

    /**
     * @return the job state
     */
    JobState getState ();


    /**
     * @return the lastUpdate
     */
    DateTime getLastUpdate ();


    /**
     * @return the progress [0.0, 100.0]
     */
    float getProgress ();


    /**
     * @return the stateMessage
     */
    String getStateMessage ();


    /**
     * @return the stateMessageContext
     */
    Set<MessageContextEntry> getStateMessageContext ();

}