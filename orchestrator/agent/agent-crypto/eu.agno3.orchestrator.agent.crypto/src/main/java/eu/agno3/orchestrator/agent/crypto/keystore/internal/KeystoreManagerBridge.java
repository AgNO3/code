/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.04.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.keystore.internal;


import javax.management.MBeanServer;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.crypto.keystore.KeystoreManagerJMXErrorResponse;
import eu.agno3.orchestrator.crypto.keystore.KeystoreManagerJMXRequest;
import eu.agno3.runtime.jmsjmx.AbstractJMSJMXBridge;
import eu.agno3.runtime.jmsjmx.JMXErrorResponse;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.listener.RequestEndpoint;


/**
 * @author mbechler
 *
 */
@Component ( service = RequestEndpoint.class, property = "msgType=eu.agno3.orchestrator.crypto.keystore.KeystoreManagerJMXRequest" )
public class KeystoreManagerBridge extends AbstractJMSJMXBridge<KeystoreManagerJMXRequest, KeystoreManagerJMXErrorResponse> {

    @Reference
    protected synchronized void setKeystoreManagementBean ( KeystoreManagementBean kmb ) {
        // dep only
    }


    protected synchronized void unsetKeystoreManagementBean ( KeystoreManagementBean kmb ) {
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
    public Class<KeystoreManagerJMXRequest> getMessageType () {
        return KeystoreManagerJMXRequest.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jmsjmx.AbstractJMSJMXBridge#createErrorResponse(eu.agno3.runtime.jmsjmx.AbstractJMXRequest)
     */
    @Override
    protected JMXErrorResponse createErrorResponse ( KeystoreManagerJMXRequest msg ) throws MessagingException {
        return new KeystoreManagerJMXErrorResponse(getMessageSource(), msg);
    }

}
