/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.broker.auth.impl;


import java.security.AccessControlException;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerFilter;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.command.ConnectionInfo;
import org.apache.log4j.Logger;


/**
 * @author mbechler
 * 
 */
public class NOPAuthenticationBroker extends BrokerFilter {

    private static final Logger log = Logger.getLogger(NOPAuthenticationBroker.class);


    /**
     * @param next
     */
    public NOPAuthenticationBroker ( Broker next ) {
        super(next);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws Exception
     * 
     * @see org.apache.activemq.broker.BrokerFilter#addConnection(org.apache.activemq.broker.ConnectionContext,
     *      org.apache.activemq.command.ConnectionInfo)
     */
    @Override
    public void addConnection ( ConnectionContext context, ConnectionInfo info ) throws Exception {

        log.trace("addConnection() called"); //$NON-NLS-1$

        if ( context.getSecurityContext() == null ) {

            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Login request for %s from %s", info.getUserName(), info.getClientIp())); //$NON-NLS-1$
            }

            if ( info.getUserName() == null ) {
                throw new AccessControlException("Anonymous access disallowed"); //$NON-NLS-1$
            }

            context.setSecurityContext(new NOPSecurityContext(info));
        }

        super.addConnection(context, info);
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.apache.activemq.broker.BrokerFilter#removeConnection(org.apache.activemq.broker.ConnectionContext,
     *      org.apache.activemq.command.ConnectionInfo, java.lang.Throwable)
     */
    @Override
    public void removeConnection ( ConnectionContext context, ConnectionInfo info, Throwable error ) throws Exception {
        context.setSecurityContext(null);
        super.removeConnection(context, info, error);
    }
}
