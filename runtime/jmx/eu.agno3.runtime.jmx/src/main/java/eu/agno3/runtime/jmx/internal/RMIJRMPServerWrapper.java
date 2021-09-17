/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.03.2017 by mbechler
 */
package eu.agno3.runtime.jmx.internal;


import java.io.IOException;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ObjID;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import javax.management.remote.rmi.RMIConnection;
import javax.management.remote.rmi.RMIJRMPServerImpl;
import javax.security.auth.Subject;

import org.apache.log4j.Logger;

import eu.agno3.runtime.jmx.JMXPermissions;
import eu.agno3.runtime.jmx.MethodPermissionMapper;

import sun.misc.ObjectInputFilter;
import sun.rmi.server.UnicastServerRef2;
import sun.rmi.transport.Endpoint;
import sun.rmi.transport.LiveRef;


/**
 * @author mbechler
 *
 */
public class RMIJRMPServerWrapper extends RMIJRMPServerImpl {

    private static final Logger log = Logger.getLogger(RMIJRMPServerWrapper.class);

    private static int MAX_DEPTH = 5;
    private static int MAX_SERVER_ARRAY_SIZE = 100;

    private final Map<String, ?> env;
    private final Endpoint endpoint;
    private final MethodPermissionMapper methodPermissionMapper;
    private final Set<JMXPermissions> defaultPermissions;


    /**
     * @param host
     * @param port
     * @param csf
     * @param ssf
     * @param env
     * @throws IOException
     */
    public RMIJRMPServerWrapper ( String host, int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf, Map<String, ?> env )
            throws IOException {
        this(host, port, csf, ssf, env, EnumSet.of(JMXPermissions.READ, JMXPermissions.NOTIFY), new DefaultMethodPermissionMapper());
    }


    /**
     * @param host
     * @param port
     * @param csf
     * @param ssf
     * @param env
     * @param defaultPermissions
     * @param methodPermMapper
     * @throws IOException
     */
    public RMIJRMPServerWrapper ( String host, int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf, Map<String, ?> env,
            Set<JMXPermissions> defaultPermissions, MethodPermissionMapper methodPermMapper ) throws IOException {
        super(port, csf, ssf, env);
        this.env = env;
        this.endpoint = TransportUtil.getOrCreateTransport(host, port, csf, ssf);
        this.defaultPermissions = defaultPermissions;
        this.methodPermissionMapper = methodPermMapper;
    }


    @Override
    protected RMIConnection makeClient ( String connectionId, Subject subject ) throws IOException {
        if ( connectionId == null )
            throw new NullPointerException("Null connectionId"); //$NON-NLS-1$

        Set<JMXPermissions> perms = getPermissionsFor(subject);

        if ( log.isDebugEnabled() ) {
            log.debug("Have new client " + connectionId); //$NON-NLS-1$
        }

        JMXConnectionWrapper client = new JMXConnectionWrapper(
            this,
            connectionId,
            getDefaultClassLoader(),
            subject,
            this.env,
            perms,
            this.methodPermissionMapper);
        export(client, client);
        return client;
    }


    /**
     * @param subject
     * @return
     */
    private Set<JMXPermissions> getPermissionsFor ( Subject subject ) {
        return Collections.unmodifiableSet(this.defaultPermissions);
    }


    @Override
    protected void closeClient ( RMIConnection client ) throws IOException {
        if ( log.isDebugEnabled() ) {
            log.debug("Removing client " + client.getConnectionId()); //$NON-NLS-1$
        }
        unexport(client, true);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.management.remote.rmi.RMIJRMPServerImpl#export()
     */
    @Override
    protected void export () throws IOException {
        export(this, RMIJRMPServerWrapper::filterServerInput);
    }


    protected void export ( Remote obj, ObjectInputFilter filter ) throws RemoteException {
        LiveRef r = new LiveRef(new ObjID(), this.endpoint, true);
        UnicastServerRef2 uniref = new UnicastServerRef2(r, filter);
        uniref.exportObject(obj, null, true);
    }


    protected void unexport ( Remote obj, boolean force ) throws NoSuchObjectException {
        UnicastRemoteObject.unexportObject(obj, force);
    }


    @Override
    protected void closeServer () throws IOException {
        unexport(this, true);
    }


    private static ObjectInputFilter.Status filterServerInput ( ObjectInputFilter.FilterInfo filterInfo ) {
        if ( filterInfo.depth() > MAX_DEPTH ) {
            return ObjectInputFilter.Status.REJECTED;
        }
        Class<?> clazz = filterInfo.serialClass();
        if ( clazz != null ) {
            if ( clazz.isArray() ) {
                if ( filterInfo.arrayLength() >= 0 && filterInfo.arrayLength() > MAX_SERVER_ARRAY_SIZE ) {
                    return ObjectInputFilter.Status.REJECTED;
                }
                do {
                    // Arrays are allowed depending on the component type
                    clazz = clazz.getComponentType();
                }
                while ( clazz.isArray() );
            }
            if ( clazz.isPrimitive() ) {
                // Arrays of primitives are allowed
                return ObjectInputFilter.Status.ALLOWED;
            }
            if ( String.class == clazz ) {
                return ObjectInputFilter.Status.ALLOWED;
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Rejecting server " + clazz.getName()); //$NON-NLS-1$
            }
            return ObjectInputFilter.Status.REJECTED;
        }
        return ObjectInputFilter.Status.UNDECIDED;
    }

}
