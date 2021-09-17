/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.11.2014 by mbechler
 */
package eu.agno3.runtime.scheduler.internal;


import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


/**
 * @author mbechler
 *
 */
public class InvalidJob implements Job {

    /**
     * {@inheritDoc}
     *
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    @Override
    public void execute ( JobExecutionContext arg0 ) throws JobExecutionException {

    }

}
