/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.02.2017 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;


/**
 * @author mbechler
 *
 */
public class NoCloseChannel implements SeekableByteChannel {

    private SeekableByteChannel stream;


    /**
     * @param stream
     */
    public NoCloseChannel ( SeekableByteChannel stream ) {
        this.stream = stream;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.nio.channels.Channel#isOpen()
     */
    @Override
    public boolean isOpen () {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.nio.channels.Channel#close()
     */
    @Override
    public void close () throws IOException {
        position(0);
    }


    @Override
    public int read ( ByteBuffer dst ) throws IOException {
        return this.stream.read(dst);
    }


    @Override
    public int write ( ByteBuffer src ) throws IOException {
        return this.stream.write(src);
    }


    @Override
    public long position () throws IOException {
        return this.stream.position();
    }


    @Override
    public SeekableByteChannel position ( long newPosition ) throws IOException {
        return this.stream.position(newPosition);
    }


    @Override
    public long size () throws IOException {
        return this.stream.size();
    }


    @Override
    public SeekableByteChannel truncate ( long size ) throws IOException {
        return this.stream.truncate(size);
    }

}
