/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 28, 2016 by mbechler
 */
package eu.agno3.runtime.security.dict;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;


/**
 * @author mbechler
 *
 */
public class IntBufferHolder extends BufferHolder<IntBuffer> {

    /**
     * @param size
     * @throws IOException
     */
    public IntBufferHolder ( long size ) throws IOException {
        super(size);
    }


    /**
     * 
     * @param size
     * @param tempDirectory
     * @throws IOException
     */
    public IntBufferHolder ( long size, Path tempDirectory ) throws IOException {
        super(size, tempDirectory);
    }


    /**
     * 
     * @param p
     * @param offset
     * @param size
     * @throws IOException
     */
    public IntBufferHolder ( Path p, long offset, long size ) throws IOException {
        super(p, offset, size);
    }


    @Override
    protected IntBuffer createWrapped ( ByteBuffer buf ) {
        return buf.asIntBuffer();
    }


    /**
     * 
     * @param pos
     * @return int at position
     */
    public final int get ( int pos ) {
        return getWrapped().get(pos);
    }


    /**
     * 
     * @param pos
     * @param val
     */
    public final void put ( int pos, int val ) {
        getWrapped().put(pos, val);
    }


    @Override
    protected int getAllocationUnit () {
        return 4;
    }
}
