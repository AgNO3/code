/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.03.2017 by mbechler
 */
package eu.agno3.runtime.jmx.internal;


import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.rmi.MarshalledObject;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

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
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.remote.NotificationResult;
import javax.management.remote.rmi.RMIConnection;
import javax.management.remote.rmi.RMIConnectionImpl;
import javax.management.remote.rmi.RMIServerImpl;
import javax.security.auth.Subject;

import org.apache.log4j.Logger;

import eu.agno3.runtime.jmx.JMXPermissions;
import eu.agno3.runtime.jmx.MethodEntry;
import eu.agno3.runtime.jmx.MethodPermissionMapper;
import eu.agno3.runtime.util.serialization.SerializationFilter;
import eu.agno3.runtime.util.serialization.UnsafeSerializableException;

import sun.misc.ObjectInputFilter;


/**
 * @author mbechler
 *
 */
public class JMXConnectionWrapper extends RMIConnectionImpl implements RMIConnection, ObjectInputFilter {

    private static final Logger log = Logger.getLogger(JMXConnectionWrapper.class);
    private final Set<JMXPermissions> permissions;
    private final MethodPermissionMapper methodPermissionMapper;

    private static int MAX_DEPTH = 5;
    private static int MAX_CLIENT_ARRAY_SIZE = 10000;

    private static final MethodHandle OBJECT_FILTER;
    private static final Set<Class<?>> PRIMITIVE_WRAPPER_TYPES = new HashSet<>(
        Arrays.asList(Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Void.class));


    static {
        MethodHandle omh = null;
        try {
            Field of = MarshalledObject.class.getDeclaredField("objectInputFilter"); //$NON-NLS-1$
            of.setAccessible(true);
            omh = MethodHandles.lookup().unreflectGetter(of);
        }
        catch (
            NoSuchFieldException |
            SecurityException |
            IllegalAccessException e ) {
            log.error("Incompatible VM", e); //$NON-NLS-1$
        }
        OBJECT_FILTER = omh;
    }


    /**
     * @param rmiServer
     * @param connectionId
     * @param defaultClassLoader
     * @param subject
     * @param env
     * @param permissions
     * @param permissionMapper
     */
    public JMXConnectionWrapper ( RMIServerImpl rmiServer, String connectionId, ClassLoader defaultClassLoader, Subject subject, Map<String, ?> env,
            Set<JMXPermissions> permissions, MethodPermissionMapper permissionMapper ) {
        super(rmiServer, connectionId, defaultClassLoader, subject, env);
        this.permissions = permissions;
        this.methodPermissionMapper = permissionMapper;
    }


    protected final void checkPermisson ( JMXPermissions perm ) {
        if ( !this.permissions.contains(perm) ) {
            log.debug("Rejecting permission " + perm); //$NON-NLS-1$
            throw new SecurityException("Do not have permission " + perm); //$NON-NLS-1$
        }
    }


    /**
     * @param name
     * @param operationName
     * @param signature
     */
    private void checkCallPermisson ( ObjectName name, String operationName, String[] signature ) {
        if ( this.permissions.contains(JMXPermissions.CALL) ) {
            return;
        }

        MethodEntry me = new MethodEntry(name, operationName, signature);

        if ( this.methodPermissionMapper != null ) {
            JMXPermissions mapped = this.methodPermissionMapper.map(me);
            if ( mapped != null ) {
                checkPermisson(mapped);
                return;
            }
        }

        String msg = String.format("Do not have permission to call %s:%s[%s]", name, operationName, Arrays.toString(signature)); //$NON-NLS-1$
        log.debug(msg);
        throw new SecurityException(msg);
    }


    @Override
    public ObjectInputFilter.Status checkInput ( ObjectInputFilter.FilterInfo filterInfo ) {
        if ( filterInfo.depth() > MAX_DEPTH ) {
            return ObjectInputFilter.Status.REJECTED;
        }
        Class<?> clazz = filterInfo.serialClass();
        if ( clazz != null ) {
            if ( clazz.isArray() ) {
                if ( filterInfo.arrayLength() >= 0 && filterInfo.arrayLength() > MAX_CLIENT_ARRAY_SIZE ) {
                    return ObjectInputFilter.Status.REJECTED;
                }
                do {
                    // Arrays are allowed depending on the component type
                    clazz = clazz.getComponentType();
                }
                while ( clazz.isArray() );
            }
            if ( clazz.isPrimitive() || PRIMITIVE_WRAPPER_TYPES.contains(clazz) ) {
                // primitives are allowed
                return ObjectInputFilter.Status.ALLOWED;
            }
            return checkObjectType(clazz);
        }
        return ObjectInputFilter.Status.UNDECIDED;
    }


    protected Status checkObjectType ( Class<?> clazz ) {
        if ( String.class == clazz || Number.class.isAssignableFrom(clazz) || ObjectName.class == clazz || Subject.class == clazz
                || MarshalledObject.class == clazz || Vector.class == clazz || Object.class == clazz
                || ( QueryExp.class.isAssignableFrom(clazz) && QueryExp.class.getPackage() == clazz.getPackage() ) || Attribute.class == clazz
                || AttributeList.class == clazz ) {
            return ObjectInputFilter.Status.ALLOWED;
        }

        if ( this.permissions.contains(JMXPermissions.NOTIFY)
                && ( javax.management.NotificationFilterSupport.class == clazz || javax.management.AttributeChangeNotificationFilter.class == clazz
                        || javax.management.relation.MBeanServerNotificationFilter.class == clazz ) ) {
            return ObjectInputFilter.Status.ALLOWED;
        }

        if ( this.permissions.contains(JMXPermissions.WRITE) || this.permissions.contains(JMXPermissions.CALL) ) {
            try {
                SerializationFilter.checkWhitelist(clazz);
            }
            catch ( UnsafeSerializableException e ) {
                log.debug("Serialization filter rejected class", e); //$NON-NLS-1$
                return ObjectInputFilter.Status.REJECTED;
            }
            return ObjectInputFilter.Status.ALLOWED;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Rejecting client object " + clazz.getName()); //$NON-NLS-1$
        }
        return ObjectInputFilter.Status.REJECTED;
    }


    protected final MarshalledObject<?> checkObject ( MarshalledObject<?> obj ) {
        try {
            if ( OBJECT_FILTER.invoke(obj) != this ) {
                throw new SecurityException("Object filter not set up properly"); //$NON-NLS-1$
            }
        }
        catch ( SecurityException e ) {
            throw e;
        }
        catch ( Throwable e ) {
            throw new SecurityException("Failed to check marshalled object", e); //$NON-NLS-1$
        }

        return obj;
    }


    protected final MarshalledObject<?>[] checkObject ( MarshalledObject<?>[] objs ) {
        if ( objs == null || objs.length == 0 ) {
            return objs;
        }

        for ( MarshalledObject<?> obj : objs ) {
            checkObject(obj);
        }
        return objs;
    }


    // internals
    @Override
    public String getDefaultDomain ( Subject delegationSubject ) throws IOException {
        checkPermisson(JMXPermissions.READ);
        return super.getDefaultDomain(delegationSubject);
    }


    @Override
    public String[] getDomains ( Subject delegationSubject ) throws IOException {
        checkPermisson(JMXPermissions.READ);
        return super.getDomains(delegationSubject);
    }


    @Override
    public Integer getMBeanCount ( Subject delegationSubject ) throws IOException {
        checkPermisson(JMXPermissions.READ);
        return super.getMBeanCount(delegationSubject);
    }


    @Override
    public MBeanInfo getMBeanInfo ( ObjectName name, Subject delegationSubject )
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException {
        checkPermisson(JMXPermissions.READ);
        return super.getMBeanInfo(name, delegationSubject);
    }


    @Override
    public String getConnectionId () throws IOException {
        checkPermisson(JMXPermissions.READ);
        return super.getConnectionId();
    }


    @Override
    public ObjectInstance getObjectInstance ( ObjectName name, Subject delegationSubject ) throws InstanceNotFoundException, IOException {
        checkPermisson(JMXPermissions.READ);
        return super.getObjectInstance(name, delegationSubject);
    }


    @Override
    public boolean isInstanceOf ( ObjectName name, String className, Subject delegationSubject ) throws InstanceNotFoundException, IOException {
        checkPermisson(JMXPermissions.READ);
        return super.isInstanceOf(name, className, delegationSubject);
    }


    @Override
    public boolean isRegistered ( ObjectName name, Subject delegationSubject ) throws IOException {
        checkPermisson(JMXPermissions.READ);
        return super.isRegistered(name, delegationSubject);
    }


    @Override
    public Set<ObjectInstance> queryMBeans ( ObjectName name, MarshalledObject query, Subject delegationSubject ) throws IOException {
        checkPermisson(JMXPermissions.READ);
        return super.queryMBeans(name, checkObject(query), delegationSubject);
    }


    @Override
    public Set<ObjectName> queryNames ( ObjectName name, MarshalledObject query, Subject delegationSubject ) throws IOException {
        checkPermisson(JMXPermissions.READ);
        return super.queryNames(name, checkObject(query), delegationSubject);
    }


    // read
    @Override
    public Object getAttribute ( ObjectName name, String attribute, Subject delegationSubject )
            throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException, IOException {
        checkPermisson(JMXPermissions.READ);
        return super.getAttribute(name, attribute, delegationSubject);
    }


    @Override
    public AttributeList getAttributes ( ObjectName name, String[] attributes, Subject delegationSubject )
            throws InstanceNotFoundException, ReflectionException, IOException {
        checkPermisson(JMXPermissions.READ);
        return super.getAttributes(name, attributes, delegationSubject);
    }


    // write
    @Override
    public void setAttribute ( ObjectName name, MarshalledObject attribute, Subject delegationSubject ) throws InstanceNotFoundException,
            AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException, IOException {
        checkPermisson(JMXPermissions.WRITE);
        super.setAttribute(name, checkObject(attribute), delegationSubject);
    }


    @Override
    public AttributeList setAttributes ( ObjectName name, MarshalledObject attributes, Subject delegationSubject )
            throws InstanceNotFoundException, ReflectionException, IOException {
        checkPermisson(JMXPermissions.WRITE);
        return super.setAttributes(name, checkObject(attributes), delegationSubject);
    }


    // call
    @Override
    public Object invoke ( ObjectName name, String operationName, MarshalledObject params, String[] signature, Subject delegationSubject )
            throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
        checkCallPermisson(name, operationName, signature);
        return super.invoke(name, operationName, checkObject(params), signature, delegationSubject);
    }


    // notifications

    @Override
    public void addNotificationListener ( ObjectName name, ObjectName listener, MarshalledObject filter, MarshalledObject handback,
            Subject delegationSubject ) throws InstanceNotFoundException, IOException {
        checkPermisson(JMXPermissions.NOTIFY);
        super.addNotificationListener(name, listener, checkObject(filter), checkObject(handback), delegationSubject);
    }


    @Override
    public Integer[] addNotificationListeners ( ObjectName[] names, MarshalledObject[] filters, Subject[] delegationSubjects )
            throws InstanceNotFoundException, IOException {
        checkPermisson(JMXPermissions.NOTIFY);
        return super.addNotificationListeners(names, checkObject(filters), delegationSubjects);
    }


    @Override
    public void removeNotificationListener ( ObjectName name, ObjectName listener, MarshalledObject filter, MarshalledObject handback,
            Subject delegationSubject ) throws InstanceNotFoundException, ListenerNotFoundException, IOException {
        checkPermisson(JMXPermissions.NOTIFY);
        super.removeNotificationListener(name, listener, checkObject(filter), checkObject(handback), delegationSubject);
    }


    @Override
    public void removeNotificationListener ( ObjectName name, ObjectName listener, Subject delegationSubject )
            throws InstanceNotFoundException, ListenerNotFoundException, IOException {
        checkPermisson(JMXPermissions.NOTIFY);
        super.removeNotificationListener(name, listener, delegationSubject);
    }


    @Override
    public void removeNotificationListeners ( ObjectName name, Integer[] listenerIDs, Subject delegationSubject )
            throws InstanceNotFoundException, ListenerNotFoundException, IOException {
        checkPermisson(JMXPermissions.NOTIFY);
        super.removeNotificationListeners(name, listenerIDs, delegationSubject);
    }


    @Override
    public NotificationResult fetchNotifications ( long clientSequenceNumber, int maxNotifications, long timeout ) throws IOException {
        checkPermisson(JMXPermissions.NOTIFY);
        return super.fetchNotifications(clientSequenceNumber, maxNotifications, timeout);
    }


    // MBean creation

    @Override
    public ObjectInstance createMBean ( String className, ObjectName name, MarshalledObject params, String[] signature, Subject delegationSubject )
            throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException,
            IOException {
        checkPermisson(JMXPermissions.CREATE);
        return super.createMBean(className, name, checkObject(params), signature, delegationSubject);
    }


    @Override
    public ObjectInstance createMBean ( String className, ObjectName name, ObjectName loaderName, MarshalledObject params, String[] signature,
            Subject delegationSubject ) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException,
                    NotCompliantMBeanException, InstanceNotFoundException, IOException {
        checkPermisson(JMXPermissions.CREATE);
        return super.createMBean(className, name, loaderName, checkObject(params), signature, delegationSubject);
    }


    @Override
    public ObjectInstance createMBean ( String className, ObjectName name, ObjectName loaderName, Subject delegationSubject )
            throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException,
            InstanceNotFoundException, IOException {
        checkPermisson(JMXPermissions.CREATE);
        return super.createMBean(className, name, loaderName, delegationSubject);
    }


    @Override
    public ObjectInstance createMBean ( String className, ObjectName name, Subject delegationSubject ) throws ReflectionException,
            InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, IOException {
        checkPermisson(JMXPermissions.CREATE);
        return super.createMBean(className, name, delegationSubject);
    }


    @Override
    public void unregisterMBean ( ObjectName name, Subject delegationSubject )
            throws InstanceNotFoundException, MBeanRegistrationException, IOException {
        checkPermisson(JMXPermissions.CREATE);
        super.unregisterMBean(name, delegationSubject);
    }

}
