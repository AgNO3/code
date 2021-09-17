/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 28, 2016 by mbechler
 */
package eu.agno3.runtime.security.dict;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Path;


/**
 * @author mbechler
 *
 */
public class ShortBufferHolder extends BufferHolder<ShortBuffer> {

    /**
     * @param size
     * @throws IOException
     */
    public ShortBufferHolder ( long size ) throws IOException {
        super(size);
    }


    /**
     * @param size
     * @param tempDirectory
     * @throws IOException
     */
    public ShortBufferHolder ( long size, Path tempDirectory ) throws IOException {
        super(size, tempDirectory);
    }


    /**
     * @param p
     * @param offset
     * @param size
     * @throws IOException
     */
    public ShortBufferHolder ( Path p, long offset, long size ) throws IOException {
        super(p, offset, size);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.dict.BufferHolder#createWrapped(java.nio.ByteBuffer)
     */
    @Override
    protected ShortBuffer createWrapped ( ByteBuffer buf ) {
        return buf.asShortBuffer();
    }


    /**
     * 
     * @param pos
     * @return int at position
     */
    public final short get ( int pos ) {
        return getWrapped().get(pos);
    }


    /**
     * 
     * @param pos
     * @param val
     */
    public final void put ( int pos, short val ) {
        getWrapped().put(pos, val);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.dict.BufferHolder#getAllocationUnit()
     */
    @Override
    protected int getAllocationUnit () {
        return 2;
    }

}
