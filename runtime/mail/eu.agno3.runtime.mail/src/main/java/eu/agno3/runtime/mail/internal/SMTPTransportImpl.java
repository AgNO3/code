/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.02.2015 by mbechler
 */
package eu.agno3.runtime.mail.internal;


import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.net.SocketFactory;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;

import org.apache.commons.pool2.ObjectPool;
import org.apache.log4j.Logger;

import com.sun.mail.smtp.SMTPTransport;
import com.sun.mail.util.MailConnectException;
import com.sun.mail.util.PropUtil;
import com.sun.mail.util.SocketConnectException;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.TLSContext;
import eu.agno3.runtime.mail.SMTPClientTransport;
import eu.agno3.runtime.mail.SMTPConfiguration;
import eu.agno3.runtime.validation.email.EMailValidator;


/**
 * @author mbechler
 *
 */
public class SMTPTransportImpl extends SMTPTransport implements SMTPClientTransport {

    private static MethodHandle INIT_STREAMS;
    private static MethodHandle CLOSE_CONNECTION;

    private static MethodHandle GET_SERVER_SOCKET;
    private static MethodHandle SET_SERVER_SOCKET;
    private static MethodHandle GET_NAME;

    static {
        Lookup lookup = MethodHandles.lookup();

        try {
            Method initStreams = SMTPTransport.class.getDeclaredMethod("initStreams"); //$NON-NLS-1$
            initStreams.setAccessible(true);
            Method closeConnection = SMTPTransport.class.getDeclaredMethod("closeConnection"); //$NON-NLS-1$
            closeConnection.setAccessible(true);

            INIT_STREAMS = lookup.unreflect(initStreams);
            CLOSE_CONNECTION = lookup.unreflect(closeConnection);

            Field ss = SMTPTransport.class.getDeclaredField("serverSocket"); //$NON-NLS-1$
            ss.setAccessible(true);
            Field host = SMTPTransport.class.getDeclaredField("host"); //$NON-NLS-1$
            host.setAccessible(true);
            Field name = SMTPTransport.class.getDeclaredField("name"); //$NON-NLS-1$
            name.setAccessible(true);

            GET_SERVER_SOCKET = lookup.unreflectGetter(ss);
            SET_SERVER_SOCKET = lookup.unreflectSetter(ss);
            GET_NAME = lookup.unreflectGetter(name);
        }
        catch (
            NoSuchMethodException |
            IllegalAccessException |
            NoSuchFieldException |
            SecurityException e ) {
            Logger.getLogger(SMTPTransportImpl.class).error("Failed to find internal references", e); //$NON-NLS-1$
        }
    }

    private static final Logger log = Logger.getLogger(SMTPTransportImpl.class);

    private static final RecipientType RECIPIENT_TYPES[] = new RecipientType[] {
        RecipientType.TO, RecipientType.CC, RecipientType.BCC
    };

    private final ObjectPool<SMTPTransportImpl> pool;

    private final SMTPConfiguration cfg;
    private final TLSContext tlsContext;

    private String host;
    private boolean startTLSEstablished;


    /**
     * @param sess
     * @param cfg
     * @param tc
     * @param pool
     * @throws MessagingException
     */
    public SMTPTransportImpl ( Session sess, SMTPConfiguration cfg, TLSContext tc, ObjectPool<SMTPTransportImpl> pool ) throws MessagingException {
        super(sess, makeURLName(cfg), cfg.getInstanceId(), cfg.isSSL());
        checkTLS(cfg, tc);
        this.cfg = cfg;
        this.tlsContext = tc;
        this.pool = pool;
    }


    /**
     * @param sess
     * @param cfg
     * @param tc
     * @throws MessagingException
     */
    public SMTPTransportImpl ( Session sess, SMTPConfiguration cfg, TLSContext tc ) throws MessagingException {
        super(sess, makeURLName(cfg), cfg.getInstanceId(), cfg.isSSL());
        checkTLS(cfg, tc);
        this.cfg = cfg;
        this.tlsContext = tc;
        this.pool = null;
    }


    /**
     * @param sess
     * @param cfg
     * @throws MessagingException
     */
    protected SMTPTransportImpl ( Session sess, SMTPConfiguration cfg ) throws MessagingException {
        super(sess, makeURLName(cfg), cfg.getInstanceId(), cfg.isSSL());
        this.cfg = cfg;
        this.tlsContext = null;
        this.pool = null;
    }


    /**
     * @param tc
     * @throws MessagingException
     */
    private static void checkTLS ( SMTPConfiguration sc, TLSContext tc ) throws MessagingException {
        if ( ( sc.isSSL() || sc.isStartTLS() ) && tc == null ) {
            throw new MessagingException("SSL/TLS is enable but no TLSContext configured"); //$NON-NLS-1$
        }
    }


    protected SMTPConfiguration getConfig () {
        return this.cfg;
    }


    /**
     * @return the startTLSEstablished
     */
    @Override
    public boolean isStartTLSEstablished () {
        return this.startTLSEstablished;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPTransportProvider#createMimeMessage()
     */
    @Override
    public MimeMessage createMimeMessage () throws MessagingException {
        return new MimeMessage(this.session);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPTransportProvider#createMimeMessage(java.io.InputStream)
     */
    @Override
    public MimeMessage createMimeMessage ( InputStream is ) throws MessagingException {
        return new MimeMessage(this.session, is);
    }


    /**
     * {@inheritDoc}
     *
     * @see com.sun.mail.smtp.SMTPTransport#startTLS()
     */
    @SuppressWarnings ( "resource" )
    @Override
    protected void startTLS () throws MessagingException {
        log.debug("Establishing StartTLS transport"); //$NON-NLS-1$
        issueCommand("STARTTLS", 220); //$NON-NLS-1$
        try {
            Socket curSocket = (Socket) GET_SERVER_SOCKET.invoke(this);
            SSLSocket tlsSocket = (SSLSocket) this.tlsContext.getSocketFactory().createSocket(curSocket, this.host, curSocket.getPort(), true);
            enableTLS(tlsSocket);
            SET_SERVER_SOCKET.invoke(this, tlsSocket);
            INIT_STREAMS.invoke(this);
            this.startTLSEstablished = true;
        }
        catch ( Throwable t ) {
            try {
                CLOSE_CONNECTION.invoke(this);
            }
            catch ( Throwable e1 ) {
                t.addSuppressed(e1);
            }
            throw wrapException("StartTLS setup failed", t); //$NON-NLS-1$
        }
    }


    /**
     * @param tlsSocket
     * @throws IOException
     * @throws Exception
     */
    void enableTLS ( SSLSocket tlsSocket ) throws IOException, CryptoException {
        try {
            tlsSocket.startHandshake();
            if ( !this.tlsContext.getHostnameVerifier().verify(this.host, tlsSocket.getSession()) ) {
                throw new SSLPeerUnverifiedException("Certificate name mismatch on " + this.host); //$NON-NLS-1$
            }
        }
        catch ( IOException e ) {
            tlsSocket.close();
            tlsSocket.getSession().invalidate();
            throw e;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see com.sun.mail.smtp.SMTPTransport#protocolConnect(java.lang.String, int, java.lang.String, java.lang.String)
     */
    @Override
    protected synchronized boolean protocolConnect ( String h, int port, String user, String password ) throws MessagingException {
        // setup socket before actual protocol connect, it will be used
        Properties props = this.session.getProperties();
        try {
            String prefix = "mail." + (String) GET_NAME.invoke(this); //$NON-NLS-1$
            this.host = h;
            SET_SERVER_SOCKET.invoke(this, createClientSocket(props, prefix, h, port));
        }
        catch ( Throwable e ) {
            throw wrapException("Failed to create connection", e); //$NON-NLS-1$
        }

        return super.protocolConnect(h, port, user, password);
    }


    /**
     * @param e
     * @return
     */
    private static MessagingException wrapException ( String msg, Throwable e ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Wrapped exception", e); //$NON-NLS-1$
        }
        Throwable t = e;
        if ( t instanceof UndeclaredThrowableException ) {
            t = ( (UndeclaredThrowableException) t ).getUndeclaredThrowable();
        }
        if ( t instanceof InvocationTargetException ) {
            t = ( (InvocationTargetException) t ).getTargetException();
        }

        if ( t instanceof MessagingException ) {
            return (MessagingException) t;
        }
        else if ( t instanceof Exception ) {
            return new MessagingException(msg, (Exception) t);
        }
        return new MessagingException(msg, new RuntimeException(t));
    }


    /**
     * @return
     * @throws MessagingException
     * @throws CryptoException
     */
    private Socket createClientSocket ( Properties props, String prefix, String h, int port ) throws MessagingException, CryptoException {
        Socket s = null;
        int cto = PropUtil.getIntProperty(props, prefix + ".connectiontimeout", -1); //$NON-NLS-1$
        int to = PropUtil.getIntProperty(props, prefix + ".timeout", -1); //$NON-NLS-1$
        try {
            String localaddrstr = props.getProperty(prefix + ".localaddress", null); //$NON-NLS-1$
            InetAddress localaddr = null;
            if ( localaddrstr != null ) {
                localaddr = InetAddress.getByName(localaddrstr);
            }
            int localport = PropUtil.getIntProperty(props, prefix + ".localport", 0); //$NON-NLS-1$
            SocketFactory sf;
            boolean ssl = this.cfg.isSSL();
            if ( ssl ) {
                sf = this.tlsContext.getSocketFactory();
            }
            else {
                sf = SocketFactory.getDefault();
            }

            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Connecting to %s:%d (ssl: %s, cto: %d, sto: %d)", h, port, ssl, cto, to)); //$NON-NLS-1$
            }

            s = sf.createSocket();
            if ( to >= 0 ) {
                s.setSoTimeout(to);
            }
            if ( localaddr != null ) {
                s.bind(new InetSocketAddress(localaddr, localport));
            }

            if ( cto >= 0 ) {
                s.connect(new InetSocketAddress(h, port), cto);
            }
            else {
                s.connect(new InetSocketAddress(h, port));
            }

            if ( ssl ) {
                enableTLS((SSLSocket) s);
            }

            return s;
        }
        catch ( IOException e ) {
            if ( s != null ) {
                try {
                    s.close();
                }
                catch ( Exception e1 ) {
                    e.addSuppressed(e1);
                }
            }
            throw new MailConnectException(new SocketConnectException("SMTP Connection failed", e, h, port, cto)); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPClientTransport#sendMessage(javax.mail.internet.MimeMessage)
     */
    @Override
    public void sendMessage ( MimeMessage msg ) throws MessagingException {
        sendMessage(msg, getRecipients(msg));
    }


    /**
     * @param msg
     * @return
     * @throws MessagingException
     */
    static List<InternetAddress> getRecipients ( MimeMessage msg ) throws MessagingException {
        List<InternetAddress> addrs = new ArrayList<>();
        for ( RecipientType t : RECIPIENT_TYPES ) {
            Address[] recipients = msg.getRecipients(t);
            if ( recipients == null ) {
                continue;
            }
            for ( Address addr : recipients ) {
                if ( ! ( addr instanceof InternetAddress ) ) {
                    throw new MessagingException("Only InternetAddress is supported"); //$NON-NLS-1$
                }
                addrs.add((InternetAddress) addr);
            }
        }
        return addrs;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPClientTransport#sendMessage(javax.mail.internet.MimeMessage, java.util.List)
     */
    @Override
    public void sendMessage ( MimeMessage msg, List<InternetAddress> addresses ) throws MessagingException {
        ClassLoader oldTTCL = Thread.currentThread().getContextClassLoader();
        try {
            log.debug("Preparing message for sending"); //$NON-NLS-1$
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            prepareMessage(this.cfg, msg, addresses);

            this.sendMessage(msg, addresses.toArray(new Address[] {}));
        }
        finally {
            Thread.currentThread().setContextClassLoader(oldTTCL);
        }
    }


    /**
     * @param msg
     * @param addresses
     * @throws MessagingException
     */
    static void prepareMessage ( SMTPConfiguration cfg, MimeMessage msg, List<InternetAddress> addresses ) throws MessagingException {
        if ( msg.getFrom() == null || msg.getFrom().length == 0 ) {
            String defaultFromAddress = cfg.getDefaultFromAddress();

            if ( defaultFromAddress == null ) {

            }

            if ( log.isDebugEnabled() ) {
                log.debug("Setting from address to default " + defaultFromAddress); //$NON-NLS-1$
            }
            try {
                msg.setFrom(new InternetAddress(defaultFromAddress, cfg.getDefaultFromName()));
            }
            catch ( UnsupportedEncodingException e ) {
                log.warn("Failed to encode from name", e); //$NON-NLS-1$
            }
        }

        // check addresses to prevent command injection
        for ( InternetAddress addr : addresses ) {
            if ( !EMailValidator.checkEMailValid(addr.getAddress(), true, false, false, true) ) {
                throw new MessagingException("Invalid address " + addr.getAddress()); //$NON-NLS-1$
            }
        }

        // make sure a sent date is set, some mail clients (yes thunderbird, I'm talking about you) do funny stuff
        // without it
        if ( msg.getSentDate() == null ) {
            msg.setSentDate(new Date());
        }
    }


    /**
     * @param cfg
     * @return
     */
    private static URLName makeURLName ( SMTPConfiguration cfg ) {
        return new URLName(
            cfg.getInstanceId(),
            cfg.getSMTPHost(),
            cfg.getSMTPPort(),
            null,
            cfg.isAuthEnabled() ? cfg.getSMTPUser() : null,
            cfg.isAuthEnabled() ? cfg.getSMTPPassword() : null);
    }


    /**
     * {@inheritDoc}
     *
     * @see com.sun.mail.smtp.SMTPTransport#close()
     */
    @Override
    public synchronized void close () throws MessagingException {
        if ( this.pool == null ) {
            this.closeInternal();
            return;
        }
        try {
            this.pool.returnObject(this);
        }
        catch ( Exception e ) {
            try {
                this.pool.invalidateObject(this);
            }
            catch ( Exception e2 ) {
                log.warn("Failed to invalidate the object", e2); //$NON-NLS-1$
                super.close();
            }

            throw new MessagingException("Failed to return transport to pool", e); //$NON-NLS-1$
        }
    }


    /**
     * @throws MessagingException
     */
    public synchronized void closeInternal () throws MessagingException {
        super.close();
    }
}
