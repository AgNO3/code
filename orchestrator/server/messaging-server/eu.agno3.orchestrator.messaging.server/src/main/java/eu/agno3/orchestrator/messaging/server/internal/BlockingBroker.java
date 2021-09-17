/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.12.2014 by mbechler
 */
package eu.agno3.orchestrator.messaging.server.internal;


import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerFilter;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.command.ConnectionInfo;


/**
 * @author mbechler
 *
 */
public class BlockingBroker extends BrokerFilter implements Broker {

    private boolean doReject = true;


    /**
     * @param next
     * @param blocked
     */
    public BlockingBroker ( Broker next, boolean blocked ) {
        super(next);
        this.doReject = blocked;
    }


    /**
     * 
     */
    public void block () {
        this.doReject = true;
    }


    /**
     * 
     */
    public void unblock () {
        this.doReject = false;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.activemq.broker.BrokerFilter#addConnection(org.apache.activemq.broker.ConnectionContext,
     *      org.apache.activemq.command.ConnectionInfo)
     */
    @Override
    public void addConnection ( ConnectionContext ctx, ConnectionInfo info ) throws Exception {
        if ( this.doReject && !ctx.getConnection().getRemoteAddress().startsWith("vm://") ) { //$NON-NLS-1$
            throw new SecurityException("Server is not yet available"); //$NON-NLS-1$
        }

        super.addConnection(ctx, info);
    }

}
