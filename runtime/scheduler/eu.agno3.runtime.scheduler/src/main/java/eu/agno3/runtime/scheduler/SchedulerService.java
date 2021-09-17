/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.09.2013 by mbechler
 */
package eu.agno3.runtime.scheduler;


import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;


/**
 * @author mbechler
 * 
 */
public interface SchedulerService {

    /**
     * Schedule a job
     * 
     * @param job
     * @param trigger
     * @throws SchedulerException
     */
    void scheduleJob ( JobDetail job, Trigger trigger ) throws SchedulerException;


    /**
     * Unschedule a job
     * 
     * @param trigger
     * @throws SchedulerException
     */
    void unscheduleJob ( TriggerKey trigger ) throws SchedulerException;
}
