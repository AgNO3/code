/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.12.2014 by mbechler
 */
package eu.agno3.orchestrator.messaging.server.internal;


import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

import org.apache.activemq.broker.Broker;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.messaging.broker.PrioritizedBrokerPlugin;
import eu.agno3.runtime.update.PlatformState;
import eu.agno3.runtime.update.PlatformStateListener;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    PrioritizedBrokerPlugin.class
} )
public class BlockingPluginImpl implements PrioritizedBrokerPlugin, PlatformStateListener {

    private Set<WeakReference<BlockingBroker>> brokers = new HashSet<>();

    private boolean blocked = false;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.PlatformStateListener#stateChanged(eu.agno3.runtime.update.PlatformState)
     */
    @Override
    public void stateChanged ( PlatformState state ) {
        if ( state == PlatformState.STARTED ) {
            this.unblock();
        }
        else if ( state == PlatformState.STOPPING ) {
            this.block();
        }
    }


    /**
     * 
     */
    public synchronized void block () {
        this.blocked = true;
        Set<WeakReference<BlockingBroker>> toRemove = new HashSet<>();
        for ( WeakReference<BlockingBroker> ref : this.brokers ) {
            BlockingBroker b = ref.get();
            if ( b != null ) {
                b.block();
            }
            else {
                toRemove.add(ref);
            }
        }
        this.brokers.removeAll(toRemove);
    }


    /**
     * 
     */
    public synchronized void unblock () {
        this.blocked = false;
        Set<WeakReference<BlockingBroker>> toRemove = new HashSet<>();
        for ( WeakReference<BlockingBroker> ref : this.brokers ) {
            BlockingBroker b = ref.get();
            if ( b != null ) {
                b.block();
            }
            else {
                toRemove.add(ref);
            }
        }
        this.brokers.removeAll(toRemove);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.activemq.broker.BrokerPlugin#installPlugin(org.apache.activemq.broker.Broker)
     */
    @Override
    public synchronized Broker installPlugin ( Broker b ) throws Exception {
        BlockingBroker blockingBroker = new BlockingBroker(b, this.blocked);
        this.brokers.add(new WeakReference<>(blockingBroker));
        return blockingBroker;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.broker.PrioritizedBrokerPlugin#getPriority()
     */
    @Override
    public int getPriority () {
        return 1000;
    }

}
