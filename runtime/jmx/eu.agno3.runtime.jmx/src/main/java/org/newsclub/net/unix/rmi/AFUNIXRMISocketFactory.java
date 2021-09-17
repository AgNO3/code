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


import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.RMISocketFactory;

import org.apache.log4j.Logger;
import org.newsclub.net.unix.AFUNIXServerSocket;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;


/**
 * An {@link RMISocketFactory} that supports {@link AFUNIXSocket}s.
 * 
 * @author Christian KohlschÃ¼tter
 */
public class AFUNIXRMISocketFactory extends RMISocketFactory implements Externalizable {

    private static final long serialVersionUID = 1L;

    private static final Logger log = Logger.getLogger(AFUNIXRMISocketFactory.class);

    static final String DEFAULT_SOCKET_FILE_PREFIX = ""; //$NON-NLS-1$
    static final String DEFAULT_SOCKET_FILE_SUFFIX = ".rmi"; //$NON-NLS-1$

    private File socketDir;
    private AFUNIXNaming naming;
    private String socketPrefix = DEFAULT_SOCKET_FILE_PREFIX;
    private String socketSuffix = DEFAULT_SOCKET_FILE_SUFFIX;
    private int serverMaxBacklog = 50;
    private InetAddress serverBindAddress = InetAddress.getLoopbackAddress();


    /**
     * Constructor required per definition.
     * 
     * @see RMISocketFactory
     * 
     */
    public AFUNIXRMISocketFactory () {}


    /**
     * @param socketDir
     * @param server
     * @throws IOException
     */
    public AFUNIXRMISocketFactory ( final File socketDir, boolean server ) throws IOException {
        this(socketDir, AFUNIXRMIPorts.DEFAULT_REGISTRY_PORT, null, null, server);
    }


    /**
     * @param socketDir
     * @param registryPort
     * @param socketPrefix
     * @param socketSuffix
     * @param server
     * @throws IOException
     */
    public AFUNIXRMISocketFactory ( final File socketDir, final int registryPort, final String socketPrefix, final String socketSuffix,
            boolean server ) throws IOException {
        this.registryPort = registryPort;
        this.server = server;
        this.socketDir = socketDir;
        this.socketPrefix = socketPrefix == null ? DEFAULT_SOCKET_FILE_PREFIX : socketPrefix;
        this.socketSuffix = socketSuffix == null ? DEFAULT_SOCKET_FILE_SUFFIX : socketSuffix;
    }


    /**
     * @param afunixNaming
     * @param port
     * @param socketDir2
     * @param socketPrefix2
     * @param socketSuffix2
     */
    AFUNIXRMISocketFactory ( AFUNIXNaming afunixNaming, int registryPort, File socketDir, String socketPrefix, String socketSuffix, boolean server ) {
        this.naming = afunixNaming;
        this.socketDir = socketDir;
        this.server = server;
        this.socketPrefix = socketPrefix == null ? DEFAULT_SOCKET_FILE_PREFIX : socketPrefix;
        this.socketSuffix = socketSuffix == null ? DEFAULT_SOCKET_FILE_SUFFIX : socketSuffix;
    }


    @Override
    public int hashCode () {
        return this.socketDir.hashCode();
    }


    @Override
    public boolean equals ( Object other ) {
        if ( ! ( other instanceof AFUNIXRMISocketFactory ) ) {
            return false;
        }
        AFUNIXRMISocketFactory sf = (AFUNIXRMISocketFactory) other;
        return sf.socketDir.equals(this.socketDir);
    }


    /**
     * @return the socket directory
     */
    public File getSocketDir () {
        return this.socketDir;
    }


    private File getFile ( int port ) throws RemoteException {
        if ( getNaming().getRegistryPort() == AFUNIXRMIPorts.PLAIN_FILE_SOCKET ) {
            return this.socketDir;
        }

        return new File(this.socketDir, this.socketPrefix + port + this.socketSuffix);
    }


    @Override
    public Socket createSocket ( String host, int port ) throws IOException {
        if ( log.isDebugEnabled() ) {
            log.debug("Creating socket on port " + port); //$NON-NLS-1$
        }

        if ( port < AFUNIXRMIPorts.AF_PORT_BASE ) {
            return new Socket(host, port);
        }

        final AFUNIXSocketAddress addr = new AFUNIXSocketAddress(getFile(port), 0);
        return AFUNIXSocket.connectTo(addr);
    }


    @Override
    public ServerSocket createServerSocket ( int port ) throws IOException {
        if ( port == 0 ) {
            final int anonPort = newPort();
            if ( log.isDebugEnabled() ) {
                log.debug("Binding anonymous unix anonPort " + port); //$NON-NLS-1$
            }
            File file = getFile(anonPort);
            file.delete();
            final AFUNIXSocketAddress addr = new AFUNIXSocketAddress(file, 0);
            final AnonymousServerSocket ass = new AnonymousServerSocket(anonPort);
            ass.bind(addr);
            return ass;
        }

        if ( port < AFUNIXRMIPorts.AF_PORT_BASE ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Binding network port " + port); //$NON-NLS-1$
            }
            return new ServerSocket(port, this.serverMaxBacklog, this.serverBindAddress);
        }

        File sockFile = getFile(port);
        final AFUNIXSocketAddress addr = new AFUNIXSocketAddress(sockFile, 0);
        if ( log.isDebugEnabled() ) {
            log.debug("Binding unix socket " + addr); //$NON-NLS-1$
        }

        sockFile.delete();
        return AFUNIXServerSocket.bindOn(addr);
    }


    /**
     * 
     */
    public void close () {}

    private PortAssigner generator = null;

    private boolean server;

    private int registryPort;


    protected int newPort () throws IOException {
        if ( this.generator == null ) {
            try {
                this.generator = getNaming().getPortAssigner();
            }
            catch ( NotBoundException e ) {
                throw (IOException) new IOException(e.getMessage()).initCause(e);
            }
        }
        return this.generator.newPort();
    }


    private AFUNIXNaming getNaming () throws RemoteException {
        if ( this.naming == null ) {
            this.naming = AFUNIXNaming.getInstance(this.socketDir, this.registryPort, this.server);
        }
        return this.naming;
    }


    protected void returnPort ( int port ) throws IOException {
        if ( this.generator == null ) {
            try {
                this.generator = getNaming().getPortAssigner();
            }
            catch ( NotBoundException e ) {
                throw (IOException) new IOException(e.getMessage()).initCause(e);
            }
        }
        this.generator.returnPort(port);
    }


    @Override
    public void readExternal ( ObjectInput in ) throws IOException, ClassNotFoundException {
        this.socketDir = new File(in.readUTF());
        this.registryPort = in.readInt();
        this.server = in.readBoolean();

        this.socketPrefix = in.readUTF();
        this.socketSuffix = in.readUTF();

        this.serverMaxBacklog = in.readInt();

        this.serverBindAddress = (InetAddress) in.readObject();
    }


    @Override
    public void writeExternal ( ObjectOutput out ) throws IOException {
        out.writeUTF(this.socketDir.getAbsolutePath());
        out.writeInt(getNaming().getRegistryPort());
        out.writeBoolean(this.server);

        out.writeUTF(this.socketPrefix);
        out.writeUTF(this.socketSuffix);

        out.writeInt(this.serverMaxBacklog);

        out.writeObject(this.serverBindAddress);

    }

    private final class AnonymousServerSocket extends AFUNIXServerSocket {

        private final int returnPort;


        protected AnonymousServerSocket ( int returnPort ) throws IOException {
            super();
            this.returnPort = returnPort;
        }


        /**
         * {@inheritDoc}
         *
         * @see java.net.ServerSocket#getLocalPort()
         */
        @Override
        public int getLocalPort () {
            return this.returnPort;
        }


        @Override
        public void close () throws IOException {
            super.close();
            returnPort(this.returnPort);
        }
    }

}
