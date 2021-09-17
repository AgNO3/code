/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.03.2016 by mbechler
 */
package eu.agno3.runtime.messaging.broker.impl;


import java.io.IOException;
import java.util.List;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.broker.region.DestinationStatistics;
import org.apache.activemq.broker.region.TempQueue;
import org.apache.activemq.broker.util.InsertionCountList;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.Message;
import org.apache.activemq.store.MessageStore;
import org.apache.activemq.thread.TaskRunnerFactory;
import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
public class ExpiringTempQueue extends TempQueue {

    private static final Logger log = Logger.getLogger(ExpiringTempQueue.class);

    private final Runnable expireMessagesTask = new Runnable() {

        @Override
        public void run () {
            expireMessages();
        }
    };


    /**
     * @param brokerService
     * @param destination
     * @param store
     * @param parentStats
     * @param taskFactory
     * @throws Exception
     */
    public ExpiringTempQueue ( BrokerService brokerService, ActiveMQDestination destination, MessageStore store, DestinationStatistics parentStats,
            TaskRunnerFactory taskFactory ) throws Exception {
        super(brokerService, destination, store, parentStats, taskFactory);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.activemq.broker.region.TempQueue#initialize()
     */
    @Override
    public void initialize () throws Exception {
        super.initialize();
        if ( getExpireMessagesPeriod() > 0 ) {
            this.scheduler.executePeriodically(this.expireMessagesTask, getExpireMessagesPeriod());
        }
    }


    void expireMessages () {
        if ( log.isDebugEnabled() ) {
            log.debug(getActiveMQDestination().getQualifiedName() + " expiring messages .."); //$NON-NLS-1$
        }
        // just track the insertion count
        List<Message> browsedMessages = new InsertionCountList<>();
        doBrowse(browsedMessages, this.getMaxExpirePageSize());
        this.getDestinationStatistics().getMessages().setCount(this.messages.size());
        if ( log.isDebugEnabled() ) {
            log.debug("Expired messages " + browsedMessages); //$NON-NLS-1$
        }
    }


    @Override
    public void dispose ( ConnectionContext context ) throws IOException {
        try {
            this.scheduler.cancel(this.expireMessagesTask);
            if ( log.isDebugEnabled() ) {
                log.debug("Disposing " + getActiveMQDestination()); //$NON-NLS-1$
            }
            this.getDestinationStatistics().getMessages().setCount(this.messages.size());
            purge();
            if ( log.isDebugEnabled() ) {
                log.debug("Disposed of " + getActiveMQDestination()); //$NON-NLS-1$
            }
        }
        catch ( Exception e ) {
            log.warn("Caught an exception purging Queue: " + getActiveMQDestination(), e); //$NON-NLS-1$
        }
        super.dispose(context);
    }
}
