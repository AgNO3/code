/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.08.2014 by mbechler
 */
package eu.agno3.runtime.logging.syslog.internal;


import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.graylog2.syslog4j.SyslogRuntimeException;
import org.graylog2.syslog4j.impl.AbstractSyslog;
import org.graylog2.syslog4j.impl.AbstractSyslogWriter;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;


/**
 * @author mbechler
 * 
 */
public class UnixSyslogImpl extends AbstractSyslog {

    private static final Logger log = Logger.getLogger(UnixSyslogImpl.class);

    /**
     * 
     */
    private static final long serialVersionUID = 3303352973901876781L;

    private UnixSyslogConfiguration unixSyslogConfig;

    private transient AFUNIXSocket sock;


    /**
     * {@inheritDoc}
     * 
     * @see org.graylog2.syslog4j.impl.AbstractSyslog#initialize()
     */
    @Override
    protected void initialize () throws SyslogRuntimeException {
        this.unixSyslogConfig = (UnixSyslogConfiguration) this.syslogConfig;

        if ( !AFUNIXSocket.isSupported() ) {
            throw new SyslogRuntimeException("UNIX Socket support is not avaialble"); //$NON-NLS-1$
        }
    }


    protected synchronized void connect () {
        try {
            // DGRAM socket
            AFUNIXSocketAddress addr = new AFUNIXSocketAddress(new File(this.unixSyslogConfig.getSocketPath()), 0, false, true);
            this.sock = AFUNIXSocket.connectTo(addr);

        }
        catch ( IOException e ) {
            log.trace("Failed to connect to syslog", e); //$NON-NLS-1$
            System.err.println(e.getMessage());
            this.sock = null;
        }

    }


    /**
     * {@inheritDoc}
     * 
     * @see org.graylog2.syslog4j.SyslogIF#shutdown()
     */
    @Override
    public synchronized void shutdown () throws SyslogRuntimeException {
        if ( this.sock != null ) {
            try {
                this.sock.close();
            }
            catch ( IOException e ) {
                log.trace("Failed to close syslog socket", e); //$NON-NLS-1$
            }
            this.sock = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.graylog2.syslog4j.impl.AbstractSyslog#write(int, byte[])
     */
    @Override
    protected synchronized void write ( int level, byte[] msg ) throws SyslogRuntimeException {
        if ( this.sock == null || this.sock.isClosed() ) {
            this.connect();
        }

        if ( this.sock == null || this.sock.isClosed() ) {
            return;
        }

        try {
            this.sock.getOutputStream().write(msg);
        }
        catch ( IOException e ) {
            log.trace("Failed to write to syslog", e); //$NON-NLS-1$
            this.shutdown();
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.graylog2.syslog4j.SyslogIF#flush()
     */
    @Override
    public synchronized void flush () throws SyslogRuntimeException {
        this.shutdown();
        this.connect();
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.graylog2.syslog4j.impl.AbstractSyslog#getWriter()
     */
    @Override
    public AbstractSyslogWriter getWriter () {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.graylog2.syslog4j.impl.AbstractSyslog#returnWriter(org.graylog2.syslog4j.impl.AbstractSyslogWriter)
     */
    @Override
    public void returnWriter ( AbstractSyslogWriter arg0 ) {}

}
