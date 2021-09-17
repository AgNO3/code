/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.09.2016 by mbechler
 */
package eu.agno3.runtime.security;


import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.shiro.util.ByteSource;


/**
 * 
 * 
 * Externalizable wrapper around BytesSource
 * 
 * @author mbechler
 *
 */
public class SerializableByteSourceWrapper implements ByteSource, Externalizable {

    private ByteSource delegate;


    /**
     * 
     */
    public SerializableByteSourceWrapper () {}


    /**
     * @param bs
     * 
     */
    public SerializableByteSourceWrapper ( ByteSource bs ) {
        this.delegate = bs;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.util.ByteSource#getBytes()
     */
    @Override
    public byte[] getBytes () {
        return this.delegate.getBytes();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.util.ByteSource#isEmpty()
     */
    @Override
    public boolean isEmpty () {
        return this.delegate.isEmpty();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.util.ByteSource#toBase64()
     */
    @Override
    public String toBase64 () {
        return this.delegate.toBase64();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.util.ByteSource#toHex()
     */
    @Override
    public String toHex () {
        return this.delegate.toHex();
    }


    /**
     * {@inheritDoc}
     *
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    @Override
    public void writeExternal ( ObjectOutput out ) throws IOException {
        if ( this.delegate == null || this.delegate.isEmpty() ) {
            out.writeInt(0);
        }
        else {
            byte[] bytes = this.delegate.getBytes();
            out.writeInt(bytes.length);
            out.write(bytes);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    @Override
    public void readExternal ( ObjectInput in ) throws IOException, ClassNotFoundException {
        int len = in.readInt();
        byte bytes[] = new byte[len];
        if ( len > 0 ) {
            in.readFully(bytes);
        }
        this.delegate = ByteSource.Util.bytes(bytes);
    }

}
