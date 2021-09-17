/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.coord.internal;


import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.jobs.JobCoordinator;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.jobs.exceptions.JobQueueException;
import eu.agno3.orchestrator.jobs.msg.JobQueueRequestMessage;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.listener.RequestEndpoint;
import eu.agno3.runtime.transaction.TransactionService;


/**
 * @author mbechler
 * 
 */
@Component ( service = RequestEndpoint.class, property = "msgType=eu.agno3.orchestrator.jobs.msg.JobQueueRequestMessage" )
public class JobQueueRequestEndpoint extends AbstractJobCoordinatorRequestEndpoint<JobQueueRequestMessage> {

    private TransactionService transactionService;


    @Override
    @Reference
    protected synchronized void setCoordinator ( JobCoordinator coord ) {
        super.setCoordinator(coord);
    }


    @Override
    protected synchronized void unsetCoordinator ( JobCoordinator coord ) {
        super.unsetCoordinator(coord);
    }


    @Override
    @Reference
    protected synchronized void setMessageSource ( @NonNull MessageSource source ) {
        super.setMessageSource(source);
    }


    @Override
    protected synchronized void unsetMessageSource ( MessageSource source ) {
        super.unsetMessageSource(source);
    }


    @Reference
    protected synchronized void setTransactionManager ( TransactionService ts ) {
        this.transactionService = ts;
    }


    protected synchronized void unsetTransactionManager ( TransactionService ts ) {
        if ( this.transactionService == ts ) {
            this.transactionService = null;
        }
    }


    @Override
    protected JobInfo handle ( @NonNull JobQueueRequestMessage msg, JobInfo info ) throws JobQueueException {

        if ( info != null ) {
            throw new JobQueueException(String.format("Job with ID '%s' does already exist", info.getJobId())); //$NON-NLS-1$
        }

        JobInfo newInfo = this.getCoordinator().queueJob(msg.getJob());
        try {
            // need to commit to assure correct ordering of status updates
            // also the request is complete when the job is queued
            TransactionManager transactionManager = this.transactionService.getTransactionManager();
            if ( transactionManager.getStatus() == Status.STATUS_ACTIVE ) {
                transactionManager.commit();
                transactionManager.begin();
            }
        }
        catch (
            SystemException |
            SecurityException |
            IllegalStateException |
            RollbackException |
            HeuristicMixedException |
            HeuristicRollbackException |
            NotSupportedException e ) {
            throw new JobQueueException("Failed to get transaction manager", e); //$NON-NLS-1$
        }
        this.getCoordinator().run();
        return newInfo;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.coord.internal.AbstractJobCoordinatorRequestEndpoint#requireJobInfo()
     */
    @Override
    protected boolean requireJobInfo () {
        return false;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.listener.RequestEndpoint#getMessageType()
     */
    @Override
    public Class<JobQueueRequestMessage> getMessageType () {
        return JobQueueRequestMessage.class;
    }

}
