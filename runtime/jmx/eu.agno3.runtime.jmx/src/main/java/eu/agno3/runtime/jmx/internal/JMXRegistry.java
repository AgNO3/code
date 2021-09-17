/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Mar 3, 2017 by mbechler
 */
package eu.agno3.runtime.jmx.internal;


import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RemoteServer;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import sun.misc.ObjectInputFilter;
import sun.rmi.registry.RegistryImpl;
import sun.rmi.registry.RegistryImpl_Stub;
import sun.rmi.server.UnicastServerRef2;
import sun.rmi.transport.LiveRef;
import sun.rmi.transport.Target;


/**
 * @author mbechler
 *
 */
public class JMXRegistry extends RemoteServer implements Registry {

    private static final Logger log = Logger.getLogger(JMXRegistry.class);

    private final Map<String, Remote> objects = new HashMap<>();

    /** Registry max depth of remote invocations. **/
    private static int REGISTRY_MAX_DEPTH = 5;

    /** Registry maximum array size in remote invocations. **/
    private static int REGISTRY_MAX_ARRAY_SIZE = 100;


    /**
     * @param host
     * @param port
     * @param csf
     * @param ssf
     * @param objects
     * @throws IOException
     */
    public JMXRegistry ( String host, int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf, Map<String, Remote> objects )
            throws IOException {
        super(
            new UnicastServerRef2(
                new LiveRef(RegistryImpl.getID(), TransportUtil.getOrCreateTransport(host, port, csf, ssf), true),
                JMXRegistry::filterInput));
        this.objects.putAll(objects);

        UnicastServerRef2 sr = (UnicastServerRef2) this.ref;
        Target target = new Target(this, sr, new RegistryImpl_Stub(sr), sr.getLiveRef().getObjID(), true);
        sr.setSkeleton(this);
        sr.getLiveRef().exportObject(target);

    }


    private static ObjectInputFilter.Status filterInput ( ObjectInputFilter.FilterInfo filterInfo ) {
        if ( filterInfo.depth() > REGISTRY_MAX_DEPTH ) {
            return ObjectInputFilter.Status.REJECTED;
        }
        Class<?> clazz = filterInfo.serialClass();
        if ( clazz != null ) {
            if ( clazz.isArray() ) {
                if ( filterInfo.arrayLength() >= 0 && filterInfo.arrayLength() > REGISTRY_MAX_ARRAY_SIZE ) {
                    return ObjectInputFilter.Status.REJECTED;
                }
                do {
                    clazz = clazz.getComponentType();
                }
                while ( clazz.isArray() );
            }
            if ( clazz.isPrimitive() ) {
                return ObjectInputFilter.Status.ALLOWED;
            }

            if ( String.class == clazz ) {
                return ObjectInputFilter.Status.ALLOWED;
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Rejecting class " + clazz.getName()); //$NON-NLS-1$
            }
            return ObjectInputFilter.Status.REJECTED;
        }
        return ObjectInputFilter.Status.UNDECIDED;
    }


    @Override
    public String[] list () {
        return this.objects.keySet().toArray(new String[0]);
    }


    @Override
    public Remote lookup ( String name ) throws NotBoundException {
        Remote remote = this.objects.get(name);
        if ( remote != null ) {
            return remote;
        }
        throw new NotBoundException(name);
    }


    @Override
    public void bind ( String name, Remote obj ) throws AccessException {
        throw new AccessException("Cannot modify this registry"); //$NON-NLS-1$
    }


    @Override
    public void rebind ( String name, Remote obj ) throws AccessException {
        throw new AccessException("Cannot modify this registry"); //$NON-NLS-1$
    }


    @Override
    public void unbind ( String name ) throws AccessException {
        throw new AccessException("Cannot modify this registry"); //$NON-NLS-1$
    }

    private static final long serialVersionUID = -4897238949499730950L;
}
