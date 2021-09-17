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
 * Using this interface jobs can be registered where a new instance is created whenever the scheduler desires it.
 * 
 * @author mbechler
 * @param <T>
 * 
 */
public interface TriggeredJobFactory <T extends Job> {

    /**
     * 
     * @return the job class type
     */
    Class<T> getJobType ();


    /**
     * @return a new instance of the job
     */
    T createJobInstance ();


    /**
     * 
     * @param trigger
     *            a trigger builder with preconfigured identity
     * @return the trigger using which this job will be scheduled
     */
    Trigger buildTrigger ( TriggerBuilder<Trigger> trigger );
}
