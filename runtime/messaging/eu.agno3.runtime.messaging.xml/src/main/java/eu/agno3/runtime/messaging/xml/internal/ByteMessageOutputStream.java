/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.01.2014 by mbechler
 */
package eu.agno3.runtime.messaging.xml.internal;

import java.io.IOException;
import java.io.OutputStream;

import javax.jms.BytesMessage;
import javax.jms.JMSException;

/**
 * @author mbechler
 * 
 */
class ByteMessageOutputStream extends OutputStream {

    /**
     * 
     */
    private final BytesMessage m;


    /**
     * @param m
     */
    public ByteMessageOutputStream ( BytesMessage m ) {
        this.m = m;
    }


    @Override
    public void write ( int b ) throws IOException {
        try {
            this.m.writeByte((byte) b);
        }
        catch ( JMSException e ) {
            throw new IOException("Failed to write to message:", e); //$NON-NLS-1$
        }
    }
}