/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.03.2016 by mbechler
 */
package eu.agno3.runtime.jmx.internal;


import java.io.IOException;
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
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;

import eu.agno3.runtime.jmx.JMXClient;


/**
 * @author mbechler
 *
 */
public class JMXPooledClientWrapper implements JMXClient {

    private JMXConnectionPoolImpl pool;
    private JMXClient cl;


    /**
     * @param cl
     * @throws IOException
     */
    public JMXPooledClientWrapper ( JMXClient cl ) throws IOException {
        this.cl = cl;
    }


    /**
     * @param pool
     */
    void setPool ( JMXConnectionPoolImpl pool ) {
        this.pool = pool;
    }


    /**
     * @return the delegate connection
     */
    JMXClient getDelegate () {
        return this.cl;
    }


    @Override
    public void close () throws IOException {
        if ( this.pool == null ) {
            this.cl.close();
        }
        else {
            this.pool.release(this);
        }
    }


    @Override
    public boolean isValid () {
        return this.cl.isValid();
    }


    @Override
    public ObjectInstance createMBean ( String className, ObjectName name ) throws ReflectionException, InstanceAlreadyExistsException,
            MBeanRegistrationException, MBeanException, NotCompliantMBeanException, IOException {
        return this.cl.createMBean(className, name);
    }


    @Override
    public ObjectInstance createMBean ( String className, ObjectName name, ObjectName loaderName )
            throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException,
            InstanceNotFoundException, IOException {
        return this.cl.createMBean(className, name, loaderName);
    }


    @Override
    public ObjectInstance createMBean ( String className, ObjectName name, Object[] params, String[] signature ) throws ReflectionException,
            InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, IOException {
        return this.cl.createMBean(className, name, params, signature);
    }


    @Override
    public ObjectInstance createMBean ( String className, ObjectName name, ObjectName loaderName, Object[] params, String[] signature )
            throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException,
            InstanceNotFoundException, IOException {
        return this.cl.createMBean(className, name, loaderName, params, signature);
    }


    @Override
    public void unregisterMBean ( ObjectName name ) throws InstanceNotFoundException, MBeanRegistrationException, IOException {
        this.cl.unregisterMBean(name);
    }


    @Override
    public ObjectInstance getObjectInstance ( ObjectName name ) throws InstanceNotFoundException, IOException {
        return this.cl.getObjectInstance(name);
    }


    @Override
    public Set<ObjectInstance> queryMBeans ( ObjectName name, QueryExp query ) throws IOException {
        return this.cl.queryMBeans(name, query);
    }


    @Override
    public Set<ObjectName> queryNames ( ObjectName name, QueryExp query ) throws IOException {
        return this.cl.queryNames(name, query);
    }


    @Override
    public boolean isRegistered ( ObjectName name ) throws IOException {
        return this.cl.isRegistered(name);
    }


    @Override
    public Integer getMBeanCount () throws IOException {
        return this.cl.getMBeanCount();
    }


    @Override
    public Object getAttribute ( ObjectName name, String attribute )
            throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException, IOException {
        return this.cl.getAttribute(name, attribute);
    }


    @Override
    public AttributeList getAttributes ( ObjectName name, String[] attributes ) throws InstanceNotFoundException, ReflectionException, IOException {
        return this.cl.getAttributes(name, attributes);
    }


    @Override
    public void setAttribute ( ObjectName name, Attribute attribute ) throws InstanceNotFoundException, AttributeNotFoundException,
            InvalidAttributeValueException, MBeanException, ReflectionException, IOException {
        this.cl.setAttribute(name, attribute);
    }


    @Override
    public AttributeList setAttributes ( ObjectName name, AttributeList attributes )
            throws InstanceNotFoundException, ReflectionException, IOException {
        return this.cl.setAttributes(name, attributes);
    }


    @Override
    public Object invoke ( ObjectName name, String operationName, Object[] params, String[] signature )
            throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
        return this.cl.invoke(name, operationName, params, signature);
    }


    @Override
    public String getDefaultDomain () throws IOException {
        return this.cl.getDefaultDomain();
    }


    @Override
    public String[] getDomains () throws IOException {
        return this.cl.getDomains();
    }


    @Override
    public void addNotificationListener ( ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback )
            throws InstanceNotFoundException, IOException {
        this.cl.addNotificationListener(name, listener, filter, handback);
    }


    @Override
    public void addNotificationListener ( ObjectName name, ObjectName listener, NotificationFilter filter, Object handback )
            throws InstanceNotFoundException, IOException {
        this.cl.addNotificationListener(name, listener, filter, handback);
    }


    @Override
    public void removeNotificationListener ( ObjectName name, ObjectName listener )
            throws InstanceNotFoundException, ListenerNotFoundException, IOException {
        this.cl.removeNotificationListener(name, listener);
    }


    @Override
    public void removeNotificationListener ( ObjectName name, ObjectName listener, NotificationFilter filter, Object handback )
            throws InstanceNotFoundException, ListenerNotFoundException, IOException {
        this.cl.removeNotificationListener(name, listener, filter, handback);
    }


    @Override
    public void removeNotificationListener ( ObjectName name, NotificationListener listener )
            throws InstanceNotFoundException, ListenerNotFoundException, IOException {
        this.cl.removeNotificationListener(name, listener);
    }


    @Override
    public void removeNotificationListener ( ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback )
            throws InstanceNotFoundException, ListenerNotFoundException, IOException {
        this.cl.removeNotificationListener(name, listener, filter, handback);
    }


    @Override
    public MBeanInfo getMBeanInfo ( ObjectName name ) throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException {
        return this.cl.getMBeanInfo(name);
    }


    @Override
    public boolean isInstanceOf ( ObjectName name, String className ) throws InstanceNotFoundException, IOException {
        return this.cl.isInstanceOf(name, className);
    }

}
