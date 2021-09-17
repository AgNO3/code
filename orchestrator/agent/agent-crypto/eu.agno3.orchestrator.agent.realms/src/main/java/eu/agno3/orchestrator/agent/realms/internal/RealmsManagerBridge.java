/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.04.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.realms.internal;


import javax.management.MBeanServer;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.realms.RealmManagerJMXErrorResponse;
import eu.agno3.orchestrator.realms.RealmManagerJMXRequest;
import eu.agno3.runtime.jmsjmx.AbstractJMSJMXBridge;
import eu.agno3.runtime.jmsjmx.JMXErrorResponse;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.listener.RequestEndpoint;


/**
 * @author mbechler
 *
 */
@Component ( service = RequestEndpoint.class, property = "msgType=eu.agno3.orchestrator.realms.RealmManagerJMXRequest" )
public class RealmsManagerBridge extends AbstractJMSJMXBridge<RealmManagerJMXRequest, RealmManagerJMXErrorResponse> {

    @Reference
    protected synchronized void setRealmManagementBean ( RealmManagementBean rmb ) {
        // dep only
    }


    protected synchronized void unsetRealmManagementBean ( RealmManagementBean rmb ) {
        // dep only
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jmsjmx.AbstractJMSJMXBridge#setMbeanServer(javax.management.MBeanServer)
     */
    @Reference
    @Override
    protected synchronized void setMbeanServer ( MBeanServer mbs ) {
        super.setMbeanServer(mbs);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jmsjmx.AbstractJMSJMXBridge#unsetMbeanServer(javax.management.MBeanServer)
     */
    @Override
    protected synchronized void unsetMbeanServer ( MBeanServer mbs ) {
        super.unsetMbeanServer(mbs);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jmsjmx.AbstractJMSJMXBridge#setMessageSource(eu.agno3.runtime.messaging.addressing.MessageSource)
     */
    @Reference
    @Override
    protected synchronized void setMessageSource ( MessageSource ms ) {
        super.setMessageSource(ms);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jmsjmx.AbstractJMSJMXBridge#unsetMessageSource(eu.agno3.runtime.messaging.addressing.MessageSource)
     */
    @Override
    protected synchronized void unsetMessageSource ( MessageSource ms ) {
        super.unsetMessageSource(ms);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.RequestEndpoint#getMessageType()
     */
    @Override
    public Class<RealmManagerJMXRequest> getMessageType () {
        return RealmManagerJMXRequest.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jmsjmx.AbstractJMSJMXBridge#createErrorResponse(eu.agno3.runtime.jmsjmx.AbstractJMXRequest)
     */
    @Override
    protected JMXErrorResponse createErrorResponse ( RealmManagerJMXRequest msg ) throws MessagingException {
        return new RealmManagerJMXErrorResponse(getMessageSource(), msg);
    }
}
