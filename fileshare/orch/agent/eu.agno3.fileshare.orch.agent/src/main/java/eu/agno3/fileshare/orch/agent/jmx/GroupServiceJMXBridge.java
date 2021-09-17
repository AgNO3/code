/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.09.2015 by mbechler
 */
package eu.agno3.fileshare.orch.agent.jmx;


import javax.management.MBeanServerConnection;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.orch.common.jmx.FileshareGroupJMXRequest;
import eu.agno3.fileshare.orch.common.jmx.FileshareJMXErrorResponse;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReferenceImpl;
import eu.agno3.orchestrator.jobs.agent.service.RuntimeServiceManager;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManager;
import eu.agno3.runtime.jmsjmx.AbstractJMSJMXBridge;
import eu.agno3.runtime.jmsjmx.JMXErrorResponse;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.listener.RequestEndpoint;


/**
 * @author mbechler
 *
 */
@Component ( service = RequestEndpoint.class, property = "msgType=eu.agno3.fileshare.orch.common.jmx.FileshareGroupJMXRequest" )
public class GroupServiceJMXBridge extends AbstractJMSJMXBridge<FileshareGroupJMXRequest, FileshareJMXErrorResponse> {

    private static final Logger log = Logger.getLogger(GroupServiceJMXBridge.class);
    private ServiceManager serviceManager;


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


    @Reference
    protected synchronized void setServiceManager ( ServiceManager sm ) {
        this.serviceManager = sm;
    }


    protected synchronized void unsetServiceManager ( ServiceManager sm ) {
        if ( this.serviceManager == sm ) {
            this.serviceManager = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.RequestEndpoint#getMessageType()
     */
    @Override
    public Class<FileshareGroupJMXRequest> getMessageType () {
        return FileshareGroupJMXRequest.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jmsjmx.AbstractJMSJMXBridge#createErrorResponse(eu.agno3.runtime.jmsjmx.AbstractJMXRequest)
     */
    @Override
    protected JMXErrorResponse createErrorResponse ( FileshareGroupJMXRequest msg ) throws MessagingException {
        return new FileshareJMXErrorResponse(getMessageSource(), msg);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ServiceManagementException
     *
     * @see eu.agno3.runtime.jmsjmx.AbstractJMSJMXBridge#getMBeanServerConnection(eu.agno3.runtime.jmsjmx.AbstractJMXRequest)
     */
    @Override
    protected MBeanServerConnection getMBeanServerConnection ( @NonNull FileshareGroupJMXRequest msg ) throws MessagingException {
        try {
            RuntimeServiceManager rsm = this.serviceManager
                    .getServiceManager(StructuralObjectReferenceImpl.fromObject(msg.getService()), RuntimeServiceManager.class);
            return rsm.getUnpooledJMXConnection();
        }
        catch ( ServiceManagementException e ) {
            log.warn("Service communication failed", e); //$NON-NLS-1$
            throw new MessagingException("Failed to establish connection with service"); //$NON-NLS-1$
        }

    }
}
