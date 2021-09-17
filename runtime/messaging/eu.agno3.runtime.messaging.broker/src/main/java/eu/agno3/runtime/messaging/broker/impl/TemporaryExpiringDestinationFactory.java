/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.03.2016 by mbechler
 */
package eu.agno3.runtime.messaging.broker.impl;


import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.broker.region.Destination;
import org.apache.activemq.broker.region.DestinationFactoryImpl;
import org.apache.activemq.broker.region.DestinationStatistics;
import org.apache.activemq.broker.region.Queue;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQTempDestination;
import org.apache.activemq.store.PersistenceAdapter;
import org.apache.activemq.thread.TaskRunnerFactory;


/**
 * @author mbechler
 *
 */
public class TemporaryExpiringDestinationFactory extends DestinationFactoryImpl {

    private BrokerService brokerService;


    /**
     * @param brokerService
     * @param taskRunnerFactory
     * @param persistenceAdapter
     */
    public TemporaryExpiringDestinationFactory ( BrokerService brokerService, TaskRunnerFactory taskRunnerFactory,
            PersistenceAdapter persistenceAdapter ) {
        super(brokerService, taskRunnerFactory, persistenceAdapter);
        this.brokerService = brokerService;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.activemq.broker.region.DestinationFactoryImpl#createDestination(org.apache.activemq.broker.ConnectionContext,
     *      org.apache.activemq.command.ActiveMQDestination, org.apache.activemq.broker.region.DestinationStatistics)
     */
    @Override
    public Destination createDestination ( ConnectionContext context, ActiveMQDestination destination, DestinationStatistics destinationStatistics )
            throws Exception {
        if ( destination.isQueue() && destination.isTemporary() && ( destination instanceof ActiveMQTempDestination ) ) {
            Queue queue = new ExpiringTempQueue(this.brokerService, destination, null, destinationStatistics, this.taskRunnerFactory);
            configureQueue(queue, destination);
            queue.initialize();
            return queue;
        }
        return super.createDestination(context, destination, destinationStatistics);
    }
}
