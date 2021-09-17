/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.02.2015 by mbechler
 */
package eu.agno3.runtime.jmx.internal;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
import javax.management.MBeanServerConnection;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.remote.rmi.RMIConnector;

import org.apache.log4j.Logger;

import eu.agno3.runtime.jmx.JMXClient;


/**
 * @author mbechler
 *
 */
public class JMXClientWrapper implements JMXClient {

    private static final Logger log = Logger.getLogger(JMXClientWrapper.class);

    private MBeanServerConnection conn;
    private RMIConnector rmiConn;

    private boolean autoClose;


    /**
     * @param rmiConn
     * @param autoClose
     * @throws IOException
     */
    public JMXClientWrapper ( RMIConnector rmiConn, boolean autoClose ) throws IOException {
        this.rmiConn = rmiConn;
        this.autoClose = autoClose;
        connect();
    }


    /**
     * @throws IOException
     */
    void connect () throws IOException {
        Map<String, Object> env = new HashMap<>();
        env.put("jmx.remote.x.check.stub", Boolean.FALSE.toString()); //$NON-NLS-1$
        this.rmiConn.connect(env);
        this.conn = this.rmiConn.getMBeanServerConnection();
    }


    @Override
    public boolean isValid () {
        try {
            this.rmiConn.getConnectionId();
            return true;
        }
        catch ( IOException e ) {
            log.debug("Connection is no longer valid", e); //$NON-NLS-1$
            return false;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close () throws IOException {
        if ( this.autoClose ) {
            this.rmiConn.close();
        }
    }


    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @param arg3
     * @throws InstanceNotFoundException
     * @throws IOException
     * @see javax.management.MBeanServerConnection#addNotificationListener(javax.management.ObjectName,
     *      javax.management.NotificationListener, javax.management.NotificationFilter, java.lang.Object)
     */
    @Override
    public void addNotificationListener ( ObjectName arg0, NotificationListener arg1, NotificationFilter arg2, Object arg3 )
            throws InstanceNotFoundException, IOException {
        this.conn.addNotificationListener(arg0, arg1, arg2, arg3);
    }


    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @param arg3
     * @throws InstanceNotFoundException
     * @throws IOException
     * @see javax.management.MBeanServerConnection#addNotificationListener(javax.management.ObjectName,
     *      javax.management.ObjectName, javax.management.NotificationFilter, java.lang.Object)
     */
    @Override
    public void addNotificationListener ( ObjectName arg0, ObjectName arg1, NotificationFilter arg2, Object arg3 )
            throws InstanceNotFoundException, IOException {
        this.conn.addNotificationListener(arg0, arg1, arg2, arg3);
    }


    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @param arg3
     * @return instance
     * @throws ReflectionException
     * @throws InstanceAlreadyExistsException
     * @throws MBeanRegistrationException
     * @throws MBeanException
     * @throws NotCompliantMBeanException
     * @throws IOException
     * @see javax.management.MBeanServerConnection#createMBean(java.lang.String, javax.management.ObjectName,
     *      java.lang.Object[], java.lang.String[])
     */
    @Override
    public ObjectInstance createMBean ( String arg0, ObjectName arg1, Object[] arg2, String[] arg3 ) throws ReflectionException,
            InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, IOException {
        return this.conn.createMBean(arg0, arg1, arg2, arg3);
    }


    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @param arg3
     * @param arg4
     * @return instance
     * @throws ReflectionException
     * @throws InstanceAlreadyExistsException
     * @throws MBeanRegistrationException
     * @throws MBeanException
     * @throws NotCompliantMBeanException
     * @throws InstanceNotFoundException
     * @throws IOException
     * @see javax.management.MBeanServerConnection#createMBean(java.lang.String, javax.management.ObjectName,
     *      javax.management.ObjectName, java.lang.Object[], java.lang.String[])
     */
    @Override
    public ObjectInstance createMBean ( String arg0, ObjectName arg1, ObjectName arg2, Object[] arg3, String[] arg4 )
            throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException,
            InstanceNotFoundException, IOException {
        return this.conn.createMBean(arg0, arg1, arg2, arg3, arg4);
    }


    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @return object instance
     * @throws ReflectionException
     * @throws InstanceAlreadyExistsException
     * @throws MBeanRegistrationException
     * @throws MBeanException
     * @throws NotCompliantMBeanException
     * @throws InstanceNotFoundException
     * @throws IOException
     * @see javax.management.MBeanServerConnection#createMBean(java.lang.String, javax.management.ObjectName,
     *      javax.management.ObjectName)
     */
    @Override
    public ObjectInstance createMBean ( String arg0, ObjectName arg1, ObjectName arg2 ) throws ReflectionException, InstanceAlreadyExistsException,
            MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException, IOException {
        return this.conn.createMBean(arg0, arg1, arg2);
    }


    /**
     * @param arg0
     * @param arg1
     * @return instance
     * @throws ReflectionException
     * @throws InstanceAlreadyExistsException
     * @throws MBeanRegistrationException
     * @throws MBeanException
     * @throws NotCompliantMBeanException
     * @throws IOException
     * @see javax.management.MBeanServerConnection#createMBean(java.lang.String, javax.management.ObjectName)
     */
    @Override
    public ObjectInstance createMBean ( String arg0, ObjectName arg1 ) throws ReflectionException, InstanceAlreadyExistsException,
            MBeanRegistrationException, MBeanException, NotCompliantMBeanException, IOException {
        return this.conn.createMBean(arg0, arg1);
    }


    /**
     * @param arg0
     * @param arg1
     * @return object
     * @throws MBeanException
     * @throws AttributeNotFoundException
     * @throws InstanceNotFoundException
     * @throws ReflectionException
     * @throws IOException
     * @see javax.management.MBeanServerConnection#getAttribute(javax.management.ObjectName, java.lang.String)
     */
    @Override
    public Object getAttribute ( ObjectName arg0, String arg1 )
            throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException, IOException {
        return this.conn.getAttribute(arg0, arg1);
    }


    /**
     * @param arg0
     * @param arg1
     * @return attribute list
     * @throws InstanceNotFoundException
     * @throws ReflectionException
     * @throws IOException
     * @see javax.management.MBeanServerConnection#getAttributes(javax.management.ObjectName, java.lang.String[])
     */
    @Override
    public AttributeList getAttributes ( ObjectName arg0, String[] arg1 ) throws InstanceNotFoundException, ReflectionException, IOException {
        return this.conn.getAttributes(arg0, arg1);
    }


    /**
     * @return default domain
     * @throws IOException
     * @see javax.management.MBeanServerConnection#getDefaultDomain()
     */
    @Override
    public String getDefaultDomain () throws IOException {
        return this.conn.getDefaultDomain();
    }


    /**
     * @return domains
     * @throws IOException
     * @see javax.management.MBeanServerConnection#getDomains()
     */
    @Override
    public String[] getDomains () throws IOException {
        return this.conn.getDomains();
    }


    /**
     * @return mbean count
     * @throws IOException
     * @see javax.management.MBeanServerConnection#getMBeanCount()
     */
    @Override
    public Integer getMBeanCount () throws IOException {
        return this.conn.getMBeanCount();
    }


    /**
     * @param arg0
     * @return mbean info
     * @throws InstanceNotFoundException
     * @throws IntrospectionException
     * @throws ReflectionException
     * @throws IOException
     * @see javax.management.MBeanServerConnection#getMBeanInfo(javax.management.ObjectName)
     */
    @Override
    public MBeanInfo getMBeanInfo ( ObjectName arg0 ) throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException {
        return this.conn.getMBeanInfo(arg0);
    }


    /**
     * @param arg0
     * @return object instance
     * @throws InstanceNotFoundException
     * @throws IOException
     * @see javax.management.MBeanServerConnection#getObjectInstance(javax.management.ObjectName)
     */
    @Override
    public ObjectInstance getObjectInstance ( ObjectName arg0 ) throws InstanceNotFoundException, IOException {
        return this.conn.getObjectInstance(arg0);
    }


    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @param arg3
     * @return return value
     * @throws InstanceNotFoundException
     * @throws MBeanException
     * @throws ReflectionException
     * @throws IOException
     * @see javax.management.MBeanServerConnection#invoke(javax.management.ObjectName, java.lang.String,
     *      java.lang.Object[], java.lang.String[])
     */
    @Override
    public Object invoke ( ObjectName arg0, String arg1, Object[] arg2, String[] arg3 )
            throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
        return this.conn.invoke(arg0, arg1, arg2, arg3);
    }


    /**
     * @param arg0
     * @param arg1
     * @return instanceof
     * @throws InstanceNotFoundException
     * @throws IOException
     * @see javax.management.MBeanServerConnection#isInstanceOf(javax.management.ObjectName, java.lang.String)
     */
    @Override
    public boolean isInstanceOf ( ObjectName arg0, String arg1 ) throws InstanceNotFoundException, IOException {
        return this.conn.isInstanceOf(arg0, arg1);
    }


    /**
     * @param arg0
     * @return whether the object is registered
     * @throws IOException
     * @see javax.management.MBeanServerConnection#isRegistered(javax.management.ObjectName)
     */
    @Override
    public boolean isRegistered ( ObjectName arg0 ) throws IOException {
        return this.conn.isRegistered(arg0);
    }


    /**
     * @param arg0
     * @param arg1
     * @return results
     * @throws IOException
     * @see javax.management.MBeanServerConnection#queryMBeans(javax.management.ObjectName, javax.management.QueryExp)
     */
    @Override
    public Set<ObjectInstance> queryMBeans ( ObjectName arg0, QueryExp arg1 ) throws IOException {
        return this.conn.queryMBeans(arg0, arg1);
    }


    /**
     * @param arg0
     * @param arg1
     * @return instance
     * @throws IOException
     * @see javax.management.MBeanServerConnection#queryNames(javax.management.ObjectName, javax.management.QueryExp)
     */
    @Override
    public Set<ObjectName> queryNames ( ObjectName arg0, QueryExp arg1 ) throws IOException {
        return this.conn.queryNames(arg0, arg1);
    }


    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @param arg3
     * @throws InstanceNotFoundException
     * @throws ListenerNotFoundException
     * @throws IOException
     * @see javax.management.MBeanServerConnection#removeNotificationListener(javax.management.ObjectName,
     *      javax.management.NotificationListener, javax.management.NotificationFilter, java.lang.Object)
     */
    @Override
    public void removeNotificationListener ( ObjectName arg0, NotificationListener arg1, NotificationFilter arg2, Object arg3 )
            throws InstanceNotFoundException, ListenerNotFoundException, IOException {
        this.conn.removeNotificationListener(arg0, arg1, arg2, arg3);
    }


    /**
     * @param arg0
     * @param arg1
     * @throws InstanceNotFoundException
     * @throws ListenerNotFoundException
     * @throws IOException
     * @see javax.management.MBeanServerConnection#removeNotificationListener(javax.management.ObjectName,
     *      javax.management.NotificationListener)
     */
    @Override
    public void removeNotificationListener ( ObjectName arg0, NotificationListener arg1 )
            throws InstanceNotFoundException, ListenerNotFoundException, IOException {
        this.conn.removeNotificationListener(arg0, arg1);
    }


    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @param arg3
     * @throws InstanceNotFoundException
     * @throws ListenerNotFoundException
     * @throws IOException
     * @see javax.management.MBeanServerConnection#removeNotificationListener(javax.management.ObjectName,
     *      javax.management.ObjectName, javax.management.NotificationFilter, java.lang.Object)
     */
    @Override
    public void removeNotificationListener ( ObjectName arg0, ObjectName arg1, NotificationFilter arg2, Object arg3 )
            throws InstanceNotFoundException, ListenerNotFoundException, IOException {
        this.conn.removeNotificationListener(arg0, arg1, arg2, arg3);
    }


    /**
     * @param arg0
     * @param arg1
     * @throws InstanceNotFoundException
     * @throws ListenerNotFoundException
     * @throws IOException
     * @see javax.management.MBeanServerConnection#removeNotificationListener(javax.management.ObjectName,
     *      javax.management.ObjectName)
     */
    @Override
    public void removeNotificationListener ( ObjectName arg0, ObjectName arg1 )
            throws InstanceNotFoundException, ListenerNotFoundException, IOException {
        this.conn.removeNotificationListener(arg0, arg1);
    }


    /**
     * @param arg0
     * @param arg1
     * @throws InstanceNotFoundException
     * @throws AttributeNotFoundException
     * @throws InvalidAttributeValueException
     * @throws MBeanException
     * @throws ReflectionException
     * @throws IOException
     * @see javax.management.MBeanServerConnection#setAttribute(javax.management.ObjectName, javax.management.Attribute)
     */
    @Override
    public void setAttribute ( ObjectName arg0, Attribute arg1 ) throws InstanceNotFoundException, AttributeNotFoundException,
            InvalidAttributeValueException, MBeanException, ReflectionException, IOException {
        this.conn.setAttribute(arg0, arg1);
    }


    /**
     * @param arg0
     * @param arg1
     * @return new attributes
     * @throws InstanceNotFoundException
     * @throws ReflectionException
     * @throws IOException
     * @see javax.management.MBeanServerConnection#setAttributes(javax.management.ObjectName,
     *      javax.management.AttributeList)
     */
    @Override
    public AttributeList setAttributes ( ObjectName arg0, AttributeList arg1 ) throws InstanceNotFoundException, ReflectionException, IOException {
        return this.conn.setAttributes(arg0, arg1);
    }


    /**
     * @param arg0
     * @throws InstanceNotFoundException
     * @throws MBeanRegistrationException
     * @throws IOException
     * @see javax.management.MBeanServerConnection#unregisterMBean(javax.management.ObjectName)
     */
    @Override
    public void unregisterMBean ( ObjectName arg0 ) throws InstanceNotFoundException, MBeanRegistrationException, IOException {
        this.conn.unregisterMBean(arg0);
    }

}
