/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.06.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;


/**
 * @author mbechler
 *
 */
public class ChunkedInputStream extends InputStream {

    private Path contextPath;
    private int numChunks;
    private long chunkSize;
    private long lastChunkSize;
    private int curChunk;
    private long curChunkLength;
    private long posInCurChunk;

    private FileChannel currentInput;


    /**
     * @param contextPath
     * @param numChunks
     * @param chunkSize
     * @param lastChunkSize
     * @throws IOException
     */
    public ChunkedInputStream ( Path contextPath, int numChunks, long chunkSize, long lastChunkSize ) throws IOException {
        this.contextPath = contextPath;
        this.numChunks = numChunks;
        this.chunkSize = chunkSize;
        this.lastChunkSize = lastChunkSize;
        this.curChunk = -1;
        this.openNextChunk();
    }


    /**
     * @throws IOException
     * 
     */
    private void openNextChunk () throws IOException {
        if ( this.currentInput != null ) {
            this.currentInput.close();
            this.currentInput = null;
        }
        this.curChunk++;

        if ( this.curChunk == this.numChunks - 1 ) {
            this.curChunkLength = this.lastChunkSize;
        }
        else {
            this.curChunkLength = this.chunkSize;
        }
        this.posInCurChunk = 0;

        if ( this.curChunk < this.numChunks ) {
            this.currentInput = FileChannel.open(getChunkFile(this.curChunk));
        }
    }


    /**
     * @param chunkIdx
     * @return
     */
    private Path getChunkFile ( int chunkIdx ) {
        return this.contextPath.resolve("chnk-" + chunkIdx); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see java.io.InputStream#read()
     */
    @Override
    public int read () throws IOException {
        if ( this.currentInput == null ) {
            return -1;
        }

        this.posInCurChunk++;
        if ( this.posInCurChunk >= this.curChunkLength ) {
            openNextChunk();
            if ( this.currentInput == null ) {
                return -1;
            }
        }

        ByteBuffer b = ByteBuffer.allocate(1);
        int read = this.currentInput.read(b);
        if ( read < 0 ) {
            return -1;
        }
        if ( read != 1 ) {
            throw new IOException("Failed to read data"); //$NON-NLS-1$
        }
        return b.get();
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
        if ( this.currentInput == null ) {
            return -1;
        }

        if ( this.posInCurChunk >= this.curChunkLength ) {
            openNextChunk();
            if ( this.currentInput == null ) {
                return -1;
            }
        }

        // at most len and at most the remainder of the current chunk
        int length = (int) Math.min(len, this.curChunkLength - this.posInCurChunk);
        ByteBuffer buffer = ByteBuffer.wrap(b, off, length);
        int got = this.currentInput.read(buffer);
        if ( got < 0 ) {
            return -1;
        }
        this.posInCurChunk += got;
        return got;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.io.InputStream#close()
     */
    @Override
    public void close () throws IOException {
        super.close();

        if ( this.currentInput != null ) {
            this.currentInput.close();
        }
    }
}
