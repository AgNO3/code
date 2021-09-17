/**
 * junixsocket
 *
 * Copyright (c) 2009 NewsClub, Christian KohlschÃ¼tter
 *
 * The author licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.newsclub.net.unix.rmi;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.newsclub.net.unix.AFUNIXSocket;


/**
 * The {@link AFUNIXSocket}-compatible equivalent of {@link Naming}. Use this
 * class for accessing RMI registries that are reachable by {@link AFUNIXSocket} s.
 * 
 * @author Christian KohlschÃ¼tter
 */
public final class AFUNIXNaming {

    private static final String PORT_ASSIGNER_ID = PortAssigner.class.getName();

    private static final Map<SocketDirAndPort, AFUNIXNaming> instances = new HashMap<>();

    private static final Logger log = Logger.getLogger(AFUNIXNaming.class);


    /**
     * Returns an {@link AFUNIXNaming} instance which only supports one file.
     * (Probably only useful when you want/can access the exported {@link UnicastRemoteObject} directly)
     * 
     * @param socketFile
     * @param server
     * @return AFUNIXNaming
     * @throws IOException
     */
    public static AFUNIXNaming getSingleFileInstance ( final File socketFile, boolean server ) throws IOException {
        return getInstance(socketFile, AFUNIXRMIPorts.PLAIN_FILE_SOCKET, server);
    }


    /**
     * Returns the
     * 
     * @param socketDir
     * @param server
     * @return AFUNIXNaming
     * @throws RemoteException
     */
    public static AFUNIXNaming getInstance ( final File socketDir, boolean server ) throws RemoteException {
        return getInstance(socketDir, AFUNIXRMIPorts.DEFAULT_REGISTRY_PORT, server);
    }


    /**
     * @param socketDir
     * @param registryPort
     * @param server
     * @return AFUNIXNaming
     * @throws RemoteException
     */
    public static AFUNIXNaming getInstance ( File socketDir, final int registryPort, boolean server ) throws RemoteException {

        String socketPrefix = null;
        String socketSuffix = null;
        if ( socketDir == null ) {
            throw new IllegalArgumentException("socketDir may not be null"); //$NON-NLS-1$
        }
        final SocketDirAndPort sap = new SocketDirAndPort(socketDir, registryPort);
        AFUNIXNaming instance;
        synchronized ( AFUNIXNaming.class ) {
            instance = instances.get(sap);
            if ( instance == null ) {
                instance = new AFUNIXNaming(sap.socketDir, registryPort, socketPrefix, socketSuffix, server);
                instances.put(sap, instance);
            }
        }
        return instance;
    }

    private Registry registry = null;
    private PortAssigner portAssigner = null;
    private final File registrySocketDir;
    private final int registryPort;
    private AFUNIXRMISocketFactory socketFactory;


    private AFUNIXNaming ( final File socketDir, final int port, final String socketPrefix, final String socketSuffix, boolean server )
            throws RemoteException {
        this.registrySocketDir = socketDir;
        this.registryPort = port;
        this.socketFactory = new AFUNIXRMISocketFactory(this, port, socketDir, socketPrefix, socketSuffix, server);
        if ( server ) {
            this.createRegistry();
        }
    }


    /**
     * @return the socket factory
     */
    public AFUNIXRMISocketFactory getSocketFactory () {
        return this.socketFactory;
    }


    /**
     * @return the registry socket dir
     */
    public File getRegistrySocketDir () {
        return this.registrySocketDir;
    }


    /**
     * @return the registry port
     */
    public int getRegistryPort () {
        return this.registryPort;
    }


    /**
     * @return the port assigner
     * @throws RemoteException
     * @throws NotBoundException
     */
    public PortAssigner getPortAssigner () throws RemoteException, NotBoundException {
        if ( this.portAssigner != null ) {
            return this.portAssigner;
        }
        this.portAssigner = getPortAssignerFromRegistry();
        return this.portAssigner;
    }


    PortAssigner getPortAssignerFromRegistry () throws RemoteException, NotBoundException {
        PortAssigner assigner;
        synchronized ( PortAssigner.class ) {
            try {
                assigner = (PortAssigner) lookup(PORT_ASSIGNER_ID);
            }
            catch ( MalformedURLException e ) {
                throw (RemoteException) new RemoteException(e.getMessage()).initCause(e);
            }
            return assigner;
        }
    }


    private void rebindPortAssigner ( final PortAssigner assigner ) throws RemoteException {
        this.portAssigner = assigner;
        getRegistry().rebind(PORT_ASSIGNER_ID, assigner);
    }


    /**
     * @return Registry
     * @throws RemoteException
     */
    public Registry getRegistry () throws RemoteException {
        if ( this.registry == null ) {
            this.registry = LocateRegistry.getRegistry(null, this.registryPort, this.socketFactory);
        }
        return this.registry;
    }


    /**
     * @param name
     * @return Remote
     * @throws NotBoundException
     * @throws MalformedURLException
     * @throws RemoteException
     */
    public Remote lookup ( String name ) throws NotBoundException, java.net.MalformedURLException, RemoteException {
        return getRegistry().lookup(name);
    }


    /**
     * @param name
     * @throws RemoteException
     * @throws NotBoundException
     * @throws MalformedURLException
     */
    public void unbind ( String name ) throws RemoteException, NotBoundException, java.net.MalformedURLException {
        getRegistry().unbind(name);
    }


    /**
     * @param name
     * @param obj
     * @throws AlreadyBoundException
     * @throws MalformedURLException
     * @throws RemoteException
     */
    public void bind ( String name, Remote obj ) throws AlreadyBoundException, java.net.MalformedURLException, RemoteException {
        getRegistry().bind(name, obj);
    }


    /**
     * @param name
     * @param obj
     * @throws MalformedURLException
     * @throws RemoteException
     */
    public void rebind ( String name, Remote obj ) throws java.net.MalformedURLException, RemoteException {
        getRegistry().rebind(name, obj);
    }


    /**
     * Shuts this RMI Registry down. Before calling this method, you have to
     * unexport all existing bindings, otherwise the "RMI Reaper" thread will
     * not be closed.
     * 
     * @throws AccessException
     * @throws RemoteException
     * @throws IOException
     */
    public void shutdownRegistry () throws IOException {
        try {
            getRegistry().unbind(PORT_ASSIGNER_ID);
            UnicastRemoteObject.unexportObject(this.portAssigner, true);
        }
        catch ( NotBoundException e ) {
            log.debug("Not bound during shutdown", e); //$NON-NLS-1$
        }
        this.portAssigner = null;

        this.socketFactory.close();
        this.socketFactory = null;
    }


    /**
     * @return the registry
     * @throws RemoteException
     */
    public Registry createRegistry () throws RemoteException {
        if ( this.registry != null ) {
            throw new RemoteException("The Registry is already created: " + this.registry); //$NON-NLS-1$
        }

        this.registry = LocateRegistry.createRegistry(this.registryPort, this.socketFactory, this.socketFactory);
        final PortAssigner ass = new PortAssignerImpl();
        UnicastRemoteObject.exportObject(ass, AFUNIXRMIPorts.PORT_ASSIGNER_PORT, this.socketFactory, this.socketFactory);
        rebindPortAssigner(ass);
        return this.registry;
    }

    private static final class SocketDirAndPort {

        File socketDir;
        int port;


        /**
         * 
         * @param socketDir
         * @param port
         * @throws RemoteException
         */
        public SocketDirAndPort ( File socketDir, int port ) throws RemoteException {
            try {
                this.socketDir = socketDir.getCanonicalFile();
            }
            catch ( IOException e ) {
                throw (RemoteException) new RemoteException(e.getMessage()).initCause(e);
            }
            this.port = port;
        }


        @Override
        public int hashCode () {
            return this.socketDir == null ? this.port : this.socketDir.hashCode() ^ this.port;
        }


        @Override
        public boolean equals ( Object o ) {
            if ( o instanceof SocketDirAndPort ) {
                SocketDirAndPort other = (SocketDirAndPort) o;
                if ( this.port != other.port ) {
                    return false;
                }
                if ( this.socketDir == null ) {
                    return other.socketDir == null;
                }

                return this.socketDir.equals(other.socketDir);
            }

            return false;
        }
    }

}
