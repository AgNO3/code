/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.01.2014 by mbechler
 */
package eu.agno3.runtime.messaging.xml.internal;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.jms.BytesMessage;
import javax.jms.JMSException;


/**
 * @author mbechler
 * 
 */
class DebuggingBytesMessageStreamReader extends InputStream {

    /**
     * 
     */
    private final BytesMessage m;
    /**
     * 
     */
    private final OutputStream debugBuf;
    private long alreadyRead = 0;

    private byte[] tmpbuf;
    private long length;


    /**
     * @param m
     * @param debugBuf
     *            clone read data into another stream, disabled if null
     * @throws JMSException
     */
    public DebuggingBytesMessageStreamReader ( BytesMessage m, OutputStream debugBuf ) throws JMSException {
        this.m = m;
        this.length = m.getBodyLength();
        this.debugBuf = debugBuf;
    }


    @Override
    public int read () throws IOException {

        try {
            if ( this.alreadyRead == this.length ) {
                return -1;
            }

            int b;

            b = this.m.readUnsignedByte();

            if ( this.debugBuf != null ) {
                this.debugBuf.write(b);
            }
            this.alreadyRead++;
            return b;
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
        return this.read(b, 0, b.length);
    }


    /**
     * {@inheritDoc}
     *
     * @see java.io.InputStream#read(byte[], int, int)
     */
    @Override
    public int read ( byte[] b, int off, int len ) throws IOException {
        try {
            if ( this.alreadyRead == this.length ) {
                return -1;
            }

            if ( this.tmpbuf == null || len > this.tmpbuf.length ) {
                this.tmpbuf = new byte[len];
            }
            int cnt;

            cnt = this.m.readBytes(this.tmpbuf, len);

            System.arraycopy(this.tmpbuf, 0, b, off, cnt);
            this.alreadyRead += cnt;
            if ( this.debugBuf != null ) {
                this.debugBuf.write(this.tmpbuf, 0, cnt);
            }

            return cnt;
        }
        catch ( JMSException e ) {
            throw new IOException(e);
        }
    }
}