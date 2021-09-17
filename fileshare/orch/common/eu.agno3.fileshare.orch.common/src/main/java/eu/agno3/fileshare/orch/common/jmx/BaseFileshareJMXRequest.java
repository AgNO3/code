/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.09.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.jmx;


import java.util.UUID;

import javax.jms.BytesMessage;
import javax.jms.JMSException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObjectImpl;
import eu.agno3.runtime.jmsjmx.AbstractJMXRequest;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.Message;


/**
 * @author mbechler
 *
 */
public abstract class BaseFileshareJMXRequest extends AbstractJMXRequest<@NonNull MessageSource, FileshareJMXErrorResponse> {

    private static final String SERVICE_ID = "targetServiceId"; //$NON-NLS-1$
    private static final String SERVICE_TYPE = "targetServiceType"; //$NON-NLS-1$
    private ServiceStructuralObject service;


    /**
     * 
     */
    public BaseFileshareJMXRequest () {
        super();
    }


    /**
     * @param origin
     * @param ttl
     */
    public BaseFileshareJMXRequest ( @NonNull MessageSource origin, int ttl ) {
        super(origin, ttl);
    }


    /**
     * @param origin
     * @param replyTo
     */
    public BaseFileshareJMXRequest ( @NonNull MessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * @param origin
     */
    public BaseFileshareJMXRequest ( @NonNull MessageSource origin ) {
        super(origin);
    }


    /**
     * @return the service
     */
    public ServiceStructuralObject getService () {
        return this.service;
    }


    /**
     * @param service
     *            the service to set
     */
    public void setService ( ServiceStructuralObject service ) {
        this.service = service;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getErrorResponseType()
     */
    @Override
    public Class<FileshareJMXErrorResponse> getErrorResponseType () {
        return FileshareJMXErrorResponse.class;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws JMSException
     *
     * @see eu.agno3.runtime.jmsjmx.AbstractJMXRequest#saveExtraProperties(javax.jms.BytesMessage)
     */
    @Override
    public void saveExtraProperties ( BytesMessage m ) throws JMSException {
        if ( this.service != null ) {
            m.setStringProperty(SERVICE_ID, this.service.getId().toString());
            m.setStringProperty(SERVICE_TYPE, this.service.getServiceType());
        }
        super.saveExtraProperties(m);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws JMSException
     *
     * @see eu.agno3.runtime.jmsjmx.AbstractJMXRequest#restoreExtraProperties(javax.jms.BytesMessage)
     */
    @Override
    public void restoreExtraProperties ( BytesMessage m ) throws JMSException {

        // TODO: this is only the absolute minimum required right now
        UUID serviceId = null;
        String serviceType = null;
        String serviceIdProp = m.getStringProperty(SERVICE_ID);
        String serviceTypeProp = m.getStringProperty(SERVICE_TYPE);

        if ( !StringUtils.isBlank(serviceIdProp) ) {
            serviceId = UUID.fromString(serviceIdProp);
        }

        if ( !StringUtils.isBlank(serviceTypeProp) ) {
            serviceType = serviceTypeProp;
        }

        if ( serviceId != null && serviceType != null ) {
            ServiceStructuralObjectImpl s = new ServiceStructuralObjectImpl();
            s.setId(serviceId);
            s.setServiceType(serviceType);
            this.service = s;
        }

        super.restoreExtraProperties(m);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jmsjmx.AbstractJMXRequest#createNew()
     */
    @Override
    public AbstractJMXRequest<@NonNull MessageSource, FileshareJMXErrorResponse> createNew () throws InstantiationException, IllegalAccessException {
        BaseFileshareJMXRequest cloned = (BaseFileshareJMXRequest) super.createNew();
        cloned.setService(this.getService());
        return cloned;
    }
}