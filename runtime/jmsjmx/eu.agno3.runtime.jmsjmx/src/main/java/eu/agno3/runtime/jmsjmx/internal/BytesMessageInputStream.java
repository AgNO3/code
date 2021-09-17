/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.01.2014 by mbechler
 */
package eu.agno3.runtime.jmsjmx.internal;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.jms.BytesMessage;
import javax.jms.JMSException;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
class BytesMessageInputStream extends InputStream implements AutoCloseable {

    private static final Logger log = Logger.getLogger(BytesMessageInputStream.class);

    /**
     * 
     */
    private final BytesMessage m;
    private final boolean debug;
    private ByteArrayOutputStream bos;


    /**
     * @param m
     * @param debug
     * @throws JMSException
     */
    BytesMessageInputStream ( BytesMessage m, boolean debug ) throws JMSException {
        this.m = m;
        this.debug = debug;

        if ( debug ) {
            this.bos = new ByteArrayOutputStream();
        }

        m.readInt();
    }


    @Override
    public int read () throws IOException {
        try {
            int val = this.m.readByte();
            if ( this.debug && val >= 0 ) {
                this.bos.write(val);
            }
            return val;
        }
        catch ( JMSException e ) {
            throw new IOException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see java.io.InputStream#read(byte[])
     */
    @Override
    public int read ( byte[] b ) throws IOException {
        try {
            int res = this.m.readBytes(b);
            if ( this.debug ) {
                this.bos.write(b);
            }
            return res;
        }
        catch ( JMSException e ) {
            throw new IOException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see java.io.InputStream#read(byte[], int, int)
     */
    @Override
    public int read ( byte[] b, int off, int len ) throws IOException {
        try {
            int res = this.m.readBytes(b);
            if ( this.debug ) {
                this.bos.write(b, off, len);
            }
            return res;
        }
        catch ( JMSException e ) {
            throw new IOException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see java.io.InputStream#skip(long)
     */
    @Override
    public long skip ( long n ) throws IOException {
        if ( n < 0 ) {
            return 0;
        }
        try {
            for ( long i = 0; i < n; i++ ) {
                if ( this.debug ) {
                    this.bos.write(this.m.readByte());
                }
                else {
                    this.m.readByte();
                }
            }

            return n;
        }
        catch ( JMSException e ) {
            throw new IOException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see java.io.InputStream#markSupported()
     */
    @Override
    public boolean markSupported () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.io.InputStream#reset()
     */
    @Override
    public synchronized void reset () throws IOException {
        try {
            log.debug("Resetting stream"); //$NON-NLS-1$
            this.m.reset();
        }
        catch ( JMSException e ) {
            throw new IOException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see java.io.InputStream#close()
     */
    @Override
    public void close () throws IOException {
        super.close();

        if ( this.debug && this.bos != null ) {
            log.trace("In bytes " + this.bos.size()); //$NON-NLS-1$
            log.trace(Hex.encodeHexString(this.bos.toByteArray()));
            this.bos = null;
        }
    }
}