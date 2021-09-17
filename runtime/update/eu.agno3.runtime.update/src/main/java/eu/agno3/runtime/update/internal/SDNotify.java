/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: May 10, 2017 by mbechler
 */
package eu.agno3.runtime.update.internal;


import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;


/**
 * @author mbechler
 *
 */
public class SDNotify {

    private static final String ONE = "1"; //$NON-NLS-1$
    private static final String STATUS = "STATUS"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(SDNotify.class);
    private static final AFUNIXSocket sock;
    private static final MethodHandle sendmsg;


    static {
        MethodHandle mh = null;
        try {
            // TODO: this should be exposed from junixsocket
            Class<?> cl = Class.forName("org.newsclub.net.unix.AFUNIXSocketImpl$AFUNIXOutputStream"); //$NON-NLS-1$
            Method method = cl.getMethod("sendmsg", byte[].class, boolean.class); //$NON-NLS-1$
            mh = MethodHandles.lookup().unreflect(method);
        }
        catch ( ClassNotFoundException e ) {
            log.debug("No AFUnixSocket support", e); //$NON-NLS-1$
        }
        catch ( Exception e ) {
            log.error("Incompatible junixsocket library", e); //$NON-NLS-1$
        }

        String notifySock = System.getenv("NOTIFY_SOCKET"); //$NON-NLS-1$

        AFUNIXSocket s = null;
        try {
            if ( mh != null && notifySock != null && AFUNIXSocket.isSupported() ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Notify socket is " + notifySock); //$NON-NLS-1$
                }
                AFUNIXSocketAddress addr;
                if ( notifySock.charAt(0) == '@' ) {
                    addr = new AFUNIXSocketAddress(new File(notifySock.substring(1)), 0, true, true);
                }
                else {
                    addr = new AFUNIXSocketAddress(new File(notifySock), 0, false, true);
                }

                s = AFUNIXSocket.connectTo(addr);
                s.setPassCred(true);
            }
            else {
                log.debug("Preconditions not met, no socket support or no notify socket"); //$NON-NLS-1$
            }
        }
        catch ( Exception e ) {
            log.error("Failed to connect to SD notify socket", e); //$NON-NLS-1$
        }
        sock = s;
        sendmsg = mh;
    }


    private static void write ( byte[] buffer ) throws IOException {
        if ( sock == null ) {
            return;
        }
        try {
            @SuppressWarnings ( "resource" )
            OutputStream os = sock.getOutputStream();
            sendmsg.invoke(os, buffer, true);
        }
        catch ( Throwable e ) {
            if ( e instanceof RuntimeException ) {
                throw (RuntimeException) e;
            }
            else if ( e instanceof IOException ) {
                throw (IOException) e;
            }
            throw new IOException("sendmsg failed", e); //$NON-NLS-1$
        }

    }


    protected static void write ( Map<String, String> vars ) throws IOException {
        if ( sock == null ) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for ( Entry<String, String> entry : vars.entrySet() ) {
            sb.append(entry.getKey());
            sb.append('=');
            sb.append(entry.getValue());
            sb.append('\n');
        }
        write(sb.toString().getBytes(StandardCharsets.UTF_8));
    }


    /**
     * Send ready notice
     * 
     * @param message
     * @param pid
     * @throws IOException
     */
    public static void ready ( String message, int pid ) throws IOException {
        if ( sock == null ) {
            return;
        }
        Map<String, String> m = new LinkedHashMap<>();
        m.put("READY", ONE); //$NON-NLS-1$
        if ( pid != 0 ) {
            m.put("MAINPID", String.valueOf(pid)); //$NON-NLS-1$
        }
        if ( message != null ) {
            m.put(STATUS, message);
        }
        write(m);
    }


    /**
     * Send failure notice
     * 
     * @param error
     * @param message
     * @throws IOException
     */
    public static void fail ( String error, String message ) throws IOException {
        if ( sock == null ) {
            return;
        }
        Map<String, String> m = new LinkedHashMap<>();
        m.put("BUSERROR", error); //$NON-NLS-1$
        if ( message != null ) {
            m.put(STATUS, message);
        }
        write(m);
    }


    /**
     * Send reloading notice
     * 
     * @param message
     * @throws IOException
     */
    public static void reloading ( String message ) throws IOException {
        if ( sock == null ) {
            return;
        }
        Map<String, String> m = new LinkedHashMap<>();
        m.put("RELOADING", ONE); //$NON-NLS-1$
        if ( message != null ) {
            m.put(STATUS, message);
        }
        write(m);
    }


    /**
     * Send stopping notice
     * 
     * @param message
     * @throws IOException
     */
    public static void stopping ( String message ) throws IOException {
        if ( sock == null ) {
            return;
        }
        Map<String, String> m = new LinkedHashMap<>();
        m.put("STOPPING", ONE); //$NON-NLS-1$
        if ( message != null ) {
            m.put(STATUS, message);
        }
        write(m);
    }


    /**
     * Send watchdog notice
     * 
     * @throws IOException
     */
    public static void watchdog () throws IOException {
        if ( sock == null ) {
            return;
        }
        Map<String, String> m = new LinkedHashMap<>();
        m.put("WATCHDOG", ONE); //$NON-NLS-1$
        write(m);
    }
}
