/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.coord.output;


import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.JobProgressInfo;
import eu.agno3.orchestrator.jobs.coord.OutputHandlerFactory;
import eu.agno3.orchestrator.jobs.exec.JobOutputHandler;
import eu.agno3.orchestrator.jobs.msg.JobOutputEvent;
import eu.agno3.orchestrator.jobs.msg.JobOutputLevel;
import eu.agno3.orchestrator.jobs.msg.JobProgressEvent;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.client.MessagingClient;


/**
 * @author mbechler
 * 
 */
@Component ( service = OutputHandlerFactory.class )
public class OutputManager implements OutputHandlerFactory {

    private static final Logger log = Logger.getLogger(OutputManager.class);

    private MessagingClient<MessageSource> msgClient;


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindMessageClient ( MessagingClient<MessageSource> cl ) {
        this.msgClient = cl;
    }


    protected synchronized void unbindMessageClient ( MessagingClient<MessageSource> cl ) {
        if ( this.msgClient == cl ) {
            this.msgClient = null;
        }
    }


    /**
     * 
     * @return whether output can be sent
     */
    public boolean isAvailable () {
        return this.msgClient != null;
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.OutputHandlerFactory#forJob(eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    public JobOutputHandler forJob ( Job j ) {
        return new OutputEventProducer(j, this);
    }


    /**
     * Publish a progress event, silently discarding if there is no messaging client available
     * 
     * @param j
     * @param progressInfo
     * @throws MessagingException
     * @throws InterruptedException
     */
    public synchronized void publishProgressEvent ( Job j, JobProgressInfo progressInfo ) throws MessagingException, InterruptedException {
        MessagingClient<MessageSource> cl = this.msgClient;

        if ( cl != null ) {
            JobProgressEvent ev = new JobProgressEvent(j.getJobId(), cl.getMessageSource());
            ev.setProgressInfo(progressInfo);
            log.trace("Publishing progress event"); //$NON-NLS-1$
            cl.publishEvent(ev);
            log.trace("Published progress event"); //$NON-NLS-1$
        }
    }


    /**
     * @param j
     * @param msg
     * @param level
     * @param pos
     * @return whether the event has been published
     * @throws MessagingException
     * @throws InterruptedException
     */
    public boolean publishOutputEvent ( Job j, String msg, JobOutputLevel level, long pos ) throws MessagingException, InterruptedException {
        MessagingClient<MessageSource> cl = this.msgClient;

        if ( cl != null ) {
            log.trace("Publishing output event"); //$NON-NLS-1$
            JobOutputEvent ev = new JobOutputEvent(j.getJobId(), level, pos, cl.getMessageSource());
            ev.setText(msg);
            cl.publishEvent(ev);
            return true;
        }
        log.debug("Messaging client not available"); //$NON-NLS-1$
        return false;
    }


    /**
     * @param j
     * @return whether the event has been published
     * @throws InterruptedException
     * @throws MessagingException
     */
    public boolean publishEof ( Job j ) throws MessagingException, InterruptedException {
        MessagingClient<MessageSource> cl = this.msgClient;

        if ( cl != null ) {
            log.debug("Publishing EOF event"); //$NON-NLS-1$
            JobOutputEvent ev = new JobOutputEvent(j.getJobId(), cl.getMessageSource());
            cl.publishEvent(ev);
            return true;
        }
        log.debug("Messaging client not available"); //$NON-NLS-1$
        return false;
    }

}
