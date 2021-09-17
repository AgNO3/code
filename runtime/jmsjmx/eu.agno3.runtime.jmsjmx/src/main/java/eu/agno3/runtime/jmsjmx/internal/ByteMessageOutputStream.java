/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.01.2014 by mbechler
 */
package eu.agno3.runtime.jmsjmx.internal;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.jms.BytesMessage;
import javax.jms.JMSException;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;


/**
 * @author mbechler
 * 
 */
class ByteMessageOutputStream extends OutputStream implements AutoCloseable {

    private static final Logger log = Logger.getLogger(ByteMessageOutputStream.class);

    /**
     * 
     */
    private final BytesMessage m;
    private long length;
    private ByteArrayOutputStream bos;
    private final boolean debug;


    /**
     * @param m
     * @param debug
     */
    public ByteMessageOutputStream ( BytesMessage m, boolean debug ) {
        this.m = m;
        this.debug = debug;
        if ( debug ) {
            this.bos = new ByteArrayOutputStream();
        }
    }


    @Override
    public void write ( int b ) throws IOException {

        try {
            this.m.writeByte((byte) b);
            if ( this.debug ) {
                this.bos.write(b);
            }
            this.length++;
        }
        catch ( JMSException e ) {
            throw new IOException("Failed to write to message:", e); //$NON-NLS-1$
        }
    }


    /**
     * @return the length
     */
    public long getLength () {
        return this.length;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.io.OutputStream#close()
     */
    @Override
    public void close () throws IOException {

        super.close();

        if ( this.debug && this.bos != null ) {
            log.debug("Out bytes " + this.bos.size()); //$NON-NLS-1$
            log.debug(Hex.encodeHexString(this.bos.toByteArray()));
            this.bos = null;
        }
    }
}