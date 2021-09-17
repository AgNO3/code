/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.04.2015 by mbechler
 */
package eu.agno3.runtime.jmsjmx.internal;


import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.runtime.jmsjmx.AbstractJMXRequest;
import eu.agno3.runtime.jmsjmx.JMSJMXClient;
import eu.agno3.runtime.jmsjmx.JMXErrorResponse;
import eu.agno3.runtime.jmsjmx.JMXResponse;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.client.MessagingClient;


/**
 * @author mbechler
 *
 */
public class JMSJMXClientImpl implements JMSJMXClient {

    private static final Logger log = Logger.getLogger(JMSJMXClientImpl.class);

    private MessagingClient<MessageSource> msgClient;
    private final AbstractJMXRequest<@NonNull MessageSource, ? extends JMXErrorResponse> prototype;
    private final ObjectName objectName;


    /**
     * @param msgClient
     * @param prototype
     * @throws MalformedObjectNameException
     */
    public JMSJMXClientImpl ( MessagingClient<MessageSource> msgClient,
            AbstractJMXRequest<@NonNull MessageSource, ? extends JMXErrorResponse> prototype ) throws MalformedObjectNameException {
        super();
        this.msgClient = msgClient;
        this.prototype = prototype;
        this.objectName = prototype.getObjectName();
    }


    /**
     * @return
     * @throws IOException
     */
    protected @NonNull AbstractJMXRequest<@NonNull MessageSource, ? extends JMXErrorResponse> createJMXInvokeRequest () throws IOException {

        try {
            AbstractJMXRequest<@NonNull MessageSource, ? extends JMXErrorResponse> invokeRequest = this.prototype.createNew();
            invokeRequest.setOrigin(this.msgClient.getMessageSource());
            return invokeRequest;
        }
        catch (
            InstantiationException |
            IllegalAccessException e ) {
            throw new IOException("Failed to create message target", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jmsjmx.JMSJMXClient#getProxy(java.lang.Class)
     */
    @Override
    @SuppressWarnings ( "unchecked" )
    public <T> T getProxy ( Class<T> type ) {
        MBeanServerInvocationHandler mBeanServerInvocationHandler = new MBeanServerInvocationHandler(this, this.objectName, true);
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[] {
            type
        }, mBeanServerInvocationHandler);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jmsjmx.JMSJMXClient#getProxyMBean(java.lang.Class)
     */
    @Override
    @SuppressWarnings ( "unchecked" )
    public <T> T getProxyMBean ( Class<T> type ) {
        MBeanServerInvocationHandler mBeanServerInvocationHandler = new MBeanServerInvocationHandler(this, this.objectName, false);
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[] {
            type
        }, mBeanServerInvocationHandler);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.management.MBeanServerConnection#createMBean(java.lang.String, javax.management.ObjectName)
     */
    @Override
    public ObjectInstance createMBean ( String className, ObjectName name ) throws ReflectionException, InstanceAlreadyExistsException,
            MBeanRegistrationException, MBeanException, NotCompliantMBeanException, IOException {
        try {
            return this.createMBean(className, name, null, new Object[] {}, new String[] {});
        }
        catch ( InstanceNotFoundException e ) {
            throw new MBeanRegistrationException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.management.MBeanServerConnection#createMBean(java.lang.String, javax.management.ObjectName,
     *      javax.management.ObjectName)
     */
    @Override
    public ObjectInstance createMBean ( String className, ObjectName name, ObjectName loaderName )
            throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException,
            InstanceNotFoundException, IOException {
        return this.createMBean(className, name, loaderName, new Object[] {}, new String[] {});
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.management.MBeanServerConnection#createMBean(java.lang.String, javax.management.ObjectName,
     *      java.lang.Object[], java.lang.String[])
     */
    @Override
    public ObjectInstance createMBean ( String className, ObjectName name, Object[] params, String[] signature ) throws ReflectionException,
            InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, IOException {
        try {
            return this.createMBean(className, name, null, params, signature);
        }
        catch ( InstanceNotFoundException e ) {
            throw new MBeanRegistrationException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.management.MBeanServerConnection#createMBean(java.lang.String, javax.management.ObjectName,
     *      javax.management.ObjectName, java.lang.Object[], java.lang.String[])
     */
    @Override
    public ObjectInstance createMBean ( String className, ObjectName name, ObjectName loaderName, Object[] params, String[] signature )
            throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException,
            InstanceNotFoundException, IOException {

        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.management.MBeanServerConnection#unregisterMBean(javax.management.ObjectName)
     */
    @Override
    public void unregisterMBean ( ObjectName name ) throws InstanceNotFoundException, MBeanRegistrationException, IOException {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.management.MBeanServerConnection#getObjectInstance(javax.management.ObjectName)
     */
    @Override
    public ObjectInstance getObjectInstance ( ObjectName name ) throws InstanceNotFoundException, IOException {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.management.MBeanServerConnection#queryMBeans(javax.management.ObjectName, javax.management.QueryExp)
     */
    @Override
    public Set<ObjectInstance> queryMBeans ( ObjectName name, QueryExp query ) throws IOException {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.management.MBeanServerConnection#queryNames(javax.management.ObjectName, javax.management.QueryExp)
     */
    @Override
    public Set<ObjectName> queryNames ( ObjectName name, QueryExp query ) throws IOException {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.management.MBeanServerConnection#isRegistered(javax.management.ObjectName)
     */
    @Override
    public boolean isRegistered ( ObjectName name ) throws IOException {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.management.MBeanServerConnection#getMBeanCount()
     */
    @Override
    public Integer getMBeanCount () throws IOException {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.management.MBeanServerConnection#getAttribute(javax.management.ObjectName, java.lang.String)
     */
    @Override
    public Object getAttribute ( ObjectName name, String attribute )
            throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException, IOException {
        @NonNull
        AbstractJMXRequest<@NonNull MessageSource, ? extends JMXErrorResponse> getAttrRequest = createJMXInvokeRequest();
        getAttrRequest.setType(AbstractJMXRequest.GET_ATTR);
        getAttrRequest.setName(attribute);

        if ( log.isDebugEnabled() ) {
            log.debug("getAttribute " + attribute); //$NON-NLS-1$
        }

        try {
            @Nullable
            JMXResponse resp = this.msgClient.sendMessage(getAttrRequest);

            if ( resp == null ) {
                throw new IOException("Did not recieve result"); //$NON-NLS-1$
            }

            return resp.getResponseObject();
        }
        catch ( MessagingException e ) {
            throw new IOException("Failed to invoke method", e); //$NON-NLS-1$
        }
        catch ( InterruptedException e ) {
            throw new IOException("Interrupted", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.management.MBeanServerConnection#getAttributes(javax.management.ObjectName, java.lang.String[])
     */
    @Override
    public AttributeList getAttributes ( ObjectName name, String[] attributes ) throws InstanceNotFoundException, ReflectionException, IOException {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.management.MBeanServerConnection#setAttribute(javax.management.ObjectName, javax.management.Attribute)
     */
    @Override
    public void setAttribute ( ObjectName name, Attribute attribute ) throws InstanceNotFoundException, AttributeNotFoundException,
            InvalidAttributeValueException, MBeanException, ReflectionException, IOException {

        @NonNull
        AbstractJMXRequest<@NonNull MessageSource, ? extends JMXErrorResponse> setAttrRequest = createJMXInvokeRequest();
        setAttrRequest.setType(AbstractJMXRequest.SET_ATTR);
        setAttrRequest.setName(attribute.getName());
        setAttrRequest.setParams(new Object[] {
            attribute.getValue()
        });

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("setAttribute %s: %s", attribute.getName(), attribute.getValue())); //$NON-NLS-1$
        }

        if ( attribute.getValue() != null ) {
            setAttrRequest.setSignature(new String[] {
                attribute.getValue().getClass().getName()
            });
        }

        try {
            @Nullable
            JMXResponse resp = this.msgClient.sendMessage(setAttrRequest);

            if ( resp == null ) {
                throw new IOException("Did not recieve result"); //$NON-NLS-1$
            }
        }
        catch ( MessagingException e ) {
            throw new IOException("Failed to set attribute method", e); //$NON-NLS-1$
        }
        catch ( InterruptedException e ) {
            throw new IOException("Interrupted", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.management.MBeanServerConnection#setAttributes(javax.management.ObjectName,
     *      javax.management.AttributeList)
     */
    @Override
    public AttributeList setAttributes ( ObjectName name, AttributeList attributes )
            throws InstanceNotFoundException, ReflectionException, IOException {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.management.MBeanServerConnection#invoke(javax.management.ObjectName, java.lang.String,
     *      java.lang.Object[], java.lang.String[])
     */
    @Override
    public Object invoke ( ObjectName name, String operationName, Object[] params, String[] signature )
            throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {

        @NonNull
        AbstractJMXRequest<@NonNull MessageSource, ? extends JMXErrorResponse> invokeRequest = createJMXInvokeRequest();
        invokeRequest.setType(AbstractJMXRequest.INVOKE);
        invokeRequest.setName(operationName);
        invokeRequest.setParams(params);
        invokeRequest.setSignature(signature);

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("setAttribute %s: %s %s", operationName, Arrays.toString(params))); //$NON-NLS-1$
        }

        try {
            @Nullable
            JMXResponse resp = this.msgClient.sendMessage(invokeRequest);

            if ( resp == null ) {
                throw new IOException("Did not recieve result"); //$NON-NLS-1$
            }

            return resp.getResponseObject();
        }
        catch ( MessagingException e ) {
            throw new IOException("Failed to invoke method", e); //$NON-NLS-1$
        }
        catch ( InterruptedException e ) {
            throw new IOException("Interrupted", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.management.MBeanServerConnection#getDefaultDomain()
     */
    @Override
    public String getDefaultDomain () throws IOException {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.management.MBeanServerConnection#getDomains()
     */
    @Override
    public String[] getDomains () throws IOException {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.management.MBeanServerConnection#addNotificationListener(javax.management.ObjectName,
     *      javax.management.NotificationListener, javax.management.NotificationFilter, java.lang.Object)
     */
    @Override
    public void addNotificationListener ( ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback )
            throws InstanceNotFoundException, IOException {

    }


    /**
     * {@inheritDoc}
     *
     * @see javax.management.MBeanServerConnection#addNotificationListener(javax.management.ObjectName,
     *      javax.management.ObjectName, javax.management.NotificationFilter, java.lang.Object)
     */
    @Override
    public void addNotificationListener ( ObjectName name, ObjectName listener, NotificationFilter filter, Object handback )
            throws InstanceNotFoundException, IOException {

    }


    /**
     * {@inheritDoc}
     *
     * @see javax.management.MBeanServerConnection#removeNotificationListener(javax.management.ObjectName,
     *      javax.management.ObjectName)
     */
    @Override
    public void removeNotificationListener ( ObjectName name, ObjectName listener )
            throws InstanceNotFoundException, ListenerNotFoundException, IOException {

    }


    /**
     * {@inheritDoc}
     *
     * @see javax.management.MBeanServerConnection#removeNotificationListener(javax.management.ObjectName,
     *      javax.management.ObjectName, javax.management.NotificationFilter, java.lang.Object)
     */
    @Override
    public void removeNotificationListener ( ObjectName name, ObjectName listener, NotificationFilter filter, Object handback )
            throws InstanceNotFoundException, ListenerNotFoundException, IOException {

    }


    /**
     * {@inheritDoc}
     *
     * @see javax.management.MBeanServerConnection#removeNotificationListener(javax.management.ObjectName,
     *      javax.management.NotificationListener)
     */
    @Override
    public void removeNotificationListener ( ObjectName name, NotificationListener listener )
            throws InstanceNotFoundException, ListenerNotFoundException, IOException {}


    /**
     * {@inheritDoc}
     *
     * @see javax.management.MBeanServerConnection#removeNotificationListener(javax.management.ObjectName,
     *      javax.management.NotificationListener, javax.management.NotificationFilter, java.lang.Object)
     */
    @Override
    public void removeNotificationListener ( ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback )
            throws InstanceNotFoundException, ListenerNotFoundException, IOException {

    }


    /**
     * {@inheritDoc}
     *
     * @see javax.management.MBeanServerConnection#getMBeanInfo(javax.management.ObjectName)
     */
    @Override
    public MBeanInfo getMBeanInfo ( ObjectName name ) throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.management.MBeanServerConnection#isInstanceOf(javax.management.ObjectName, java.lang.String)
     */
    @Override
    public boolean isInstanceOf ( ObjectName name, String className ) throws InstanceNotFoundException, IOException {
        return false;
    }

}
