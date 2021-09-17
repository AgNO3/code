/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.09.2013 by mbechler
 */
package eu.agno3.runtime.scheduler;


import org.quartz.Job;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;


/**
 * 
 * When jobs exported using this interface are triggered, the exported instance will be used for execution. Be careful,
 * as by default these may be called from multiple threads in parallel.
 * 
 * @author mbechler
 * 
 */
public interface TriggeredJob extends Job {

    /**
     * 
     * @param trigger
     *            a trigger builder with preconfigured identity
     * @return the trigger using which this job will be scheduled
     */
    Trigger buildTrigger ( TriggerBuilder<Trigger> trigger );
}
