/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.12.2014 by mbechler
 */
package eu.agno3.orchestrator.system.logsink.internal;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.newsclub.net.unix.AFUNIXServerSocket;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;

import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;


/**
 * @author mbechler
 *
 */
@Component ( service = LoggerSocket.class, configurationPid = LoggerSocket.PID, configurationPolicy = ConfigurationPolicy.REQUIRE, immediate = true )
public class LoggerSocket implements Runnable {

    private static final Logger log = Logger.getLogger(LoggerSocket.class);

    /**
     * 
     */
    public static final String PID = "logsink"; //$NON-NLS-1$

    private static final String DEFAULT_SOCKET_PATH = "/run/orchagent/logging/log.sock"; //$NON-NLS-1$

    private static final Charset CHARSET = Charset.forName("UTF-8"); //$NON-NLS-1$

    private AFUNIXServerSocket socket;
    private Thread listenerThread;
    private boolean exitListenerThread;
    private AFUNIXSocketAddress socketAddress;
    private File socketFile;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {

        if ( !prepareSocket(ctx) ) {
            return;
        }

        try {
            this.socketFile.delete();
            this.socketAddress = new AFUNIXSocketAddress(this.socketFile, 0, false, true);
            this.socket = AFUNIXServerSocket.bindOn(this.socketAddress);
            if ( log.isDebugEnabled() ) {
                log.debug("Bound to " + this.socketAddress); //$NON-NLS-1$
            }
        }
        catch ( IOException e ) {
            log.error("Failed to bind to log socket", e); //$NON-NLS-1$
            this.socketFile.delete();
            return;
        }
        this.listenerThread = new Thread(this, "System log listener"); //$NON-NLS-1$
        this.listenerThread.start();
        log.debug("Listener thread started"); //$NON-NLS-1$
    }


    /**
     * @param ctx
     * @return
     */
    private boolean prepareSocket ( ComponentContext ctx ) {
        String socketSpec = (String) ctx.getProperties().get("socketPath"); //$NON-NLS-1$

        if ( !StringUtils.isBlank(socketSpec) ) {
            this.socketFile = new File(socketSpec);
        }
        else {
            this.socketFile = new File(DEFAULT_SOCKET_PATH);
        }

        File socketDir = this.socketFile.getParentFile();
        if ( !socketDir.exists() ) {
            try {
                Files.createDirectories(socketDir.toPath(), PosixFilePermissions.asFileAttribute(FileSecurityUtils.getOwnerOnlyDirPermissions()));
            }
            catch ( IOException e ) {
                log.error("Failed to create socket directory permissions", e); //$NON-NLS-1$
                return false;
            }
        }

        if ( !socketDir.canWrite() ) {
            log.error("Cannot write socket directory " + socketDir.getPath()); //$NON-NLS-1$
            return false;
        }

        return true;
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        exitListenerThread();
    }


    /**
     * @return
     */
    private AFUNIXSocketAddress getSocketAddress () {
        return this.socketAddress;
    }


    /**
     * 
     */
    private void exitListenerThread () {
        if ( this.listenerThread != null ) {
            this.exitListenerThread = true;
            try {
                sendInterruptToSocket();
            }
            catch ( IOException e ) {

            }
            this.listenerThread.interrupt();

            try {
                this.listenerThread.join();
            }
            catch ( InterruptedException e ) {
                log.error("Interrupted while joining listener thread", e); //$NON-NLS-1$
            }

            this.listenerThread = null;
        }
    }


    /**
     * @throws IOException
     * 
     */
    private void sendInterruptToSocket () throws IOException {
        try ( AFUNIXSocket sock = AFUNIXSocket.connectTo(this.getSocketAddress()) ) {
            // do nothing, only trigger an read
            sock.getOutputStream().write(System.lineSeparator().getBytes(Charset.forName("ASCII"))); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run () {
        while ( !this.exitListenerThread ) {
            log.trace("Log socket accept()"); //$NON-NLS-1$
            try ( AFUNIXSocket s = this.socket.accept();
                  InputStream in = s.getInputStream();
                  BufferedReader br = new BufferedReader(new InputStreamReader(in, CHARSET)) ) {
                String line = null;

                log.trace("accept() returned"); //$NON-NLS-1$

                while ( ( line = br.readLine() ) != null ) {
                    if ( StringUtils.isBlank(line) ) {
                        if ( this.exitListenerThread ) {
                            break;
                        }
                        continue;
                    }
                    if ( log.isDebugEnabled() ) {
                        log.debug("Recieved line: " + line); //$NON-NLS-1$
                    }
                }
            }
            catch ( IOException e ) {
                log.warn("Error reading from log socket", e); //$NON-NLS-1$
            }

        }

        log.debug("Exiting log listener thread"); //$NON-NLS-1$
    }
}
