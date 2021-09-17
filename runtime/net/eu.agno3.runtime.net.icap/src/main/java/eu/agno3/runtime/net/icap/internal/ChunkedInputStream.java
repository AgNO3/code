/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.05.2015 by mbechler
 */
package eu.agno3.runtime.net.icap.internal;


import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
public class ChunkedInputStream extends InputStream implements AutoCloseable {

    private static final int MAX_CHUNK_HEADER_SIZE = 4096;

    private static final Logger log = Logger.getLogger(ChunkedInputStream.class);

    private InputStream delegate;

    private boolean firstChunk = true;
    private boolean eof = false;
    private boolean closed = false;

    private long posInChunk;
    private long curChunkSize = -1;

    private String curChunkComment = StringUtils.EMPTY;

    private Map<String, List<String>> trailerHeaders = Collections.EMPTY_MAP;


    /**
     * @param is
     */
    public ChunkedInputStream ( InputStream is ) {
        this.delegate = is;
    }


    /**
     * @return the curChunkComment
     */
    public String getCurChunkComment () {
        return this.curChunkComment;
    }


    /**
     * @return the curChunkSize
     */
    public long getCurChunkSize () {
        return this.curChunkSize;
    }


    /**
     * @return the trailerHeaders
     */
    public Map<String, List<String>> getTrailerHeaders () {
        return this.trailerHeaders;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.io.InputStream#read()
     */
    @Override
    public int read () throws IOException {

        if ( this.closed ) {
            throw new IOException("Stream is closed"); //$NON-NLS-1$
        }

        if ( this.eof ) {
            return -1;
        }

        if ( this.posInChunk >= this.curChunkSize ) {
            beginNextChunk();
            if ( this.eof ) {
                return -1;
            }
        }
        this.posInChunk++;
        return this.delegate.read();
    }


    /**
     * {@inheritDoc}
     *
     * @see java.io.InputStream#read(byte[], int, int)
     */
    @Override
    public int read ( byte[] b, int off, int len ) throws IOException {
        if ( this.closed ) {
            throw new IOException("Stream is closed"); //$NON-NLS-1$
        }

        if ( this.eof ) {
            return -1;
        }

        if ( this.posInChunk >= this.curChunkSize ) {
            beginNextChunk();
            if ( this.eof ) {
                return -1;
            }
        }

        // at most len and at most the remainder of the current chunk
        int length = (int) Math.min(len, this.curChunkSize - this.posInChunk);
        if ( log.isTraceEnabled() ) {
            log.trace(String.format("Reading %d at %d, chunk remain %d", length, off, this.curChunkSize - this.posInChunk)); //$NON-NLS-1$
        }
        int got = this.delegate.read(b, off, length);
        if ( log.isTraceEnabled() ) {
            log.trace("Got " + got); //$NON-NLS-1$
        }
        this.posInChunk += got;
        return got;
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
     * @see java.io.InputStream#close()
     */
    @Override
    public void close () throws IOException {
        if ( !this.closed ) {
            try {
                if ( !this.eof ) {
                    log.trace("Consuming remainder of input"); //$NON-NLS-1$
                    consumeRemainingData();
                }
            }
            finally {
                this.eof = true;
                this.closed = true;
            }
        }
    }


    /**
     * @throws IOException
     * 
     */
    private void consumeRemainingData () throws IOException {
        byte[] buffer = new byte[4096];

        try {
            while ( this.read(buffer) >= 0 ) {
                // ignore
            }
        }
        catch ( IOException e ) {
            this.delegate.close();
            throw e;
        }
    }


    /**
     * @throws IOException
     */
    private void beginNextChunk () throws IOException {
        if ( !this.firstChunk ) {
            if ( this.delegate.read() != 13 || this.delegate.read() != 10 ) {
                throw new IOException("Chunk does not end with CRLF"); //$NON-NLS-1$
            }
        }

        this.firstChunk = false;

        String header = getChunkHeader();

        int sepPos = header.indexOf(';');
        long chunkSize;

        if ( log.isTraceEnabled() ) {
            log.trace("Found chunk " + header); //$NON-NLS-1$
        }

        try {
            if ( sepPos >= 0 ) {
                chunkSize = Long.parseLong(header.substring(0, sepPos), 16);
                this.curChunkComment = header.substring(sepPos + 1);
            }
            else {
                chunkSize = Long.parseLong(header, 16);
                this.curChunkComment = StringUtils.EMPTY;
            }
        }
        catch ( NumberFormatException e ) {
            throw new IOException("Failed to parse chunk size: " + header, e); //$NON-NLS-1$
        }

        this.curChunkSize = chunkSize;
        this.posInChunk = 0;

        if ( chunkSize == 0 ) {
            this.eof = true;
            this.trailerHeaders = ICAPIOUtils.readHeaders(this.delegate);
        }

    }


    /**
     * @return
     * @throws IOException
     */
    private String getChunkHeader () throws IOException {
        int c;
        boolean quoted = false;
        boolean lastWasCR = false;
        StringBuilder sb = new StringBuilder();

        while ( true ) {
            if ( sb.length() > MAX_CHUNK_HEADER_SIZE ) {
                throw new IOException("Chunk header exceeds limit"); //$NON-NLS-1$
            }

            c = this.delegate.read();
            if ( c < 0 ) {
                throw new IOException("EOF while reading chunk header"); //$NON-NLS-1$
            }

            if ( !quoted && c == 10 && lastWasCR ) {
                break;
            }
            else if ( !quoted && c == 13 ) {
                lastWasCR = true;
                sb.append((char) c);
            }
            else if ( !quoted && c == '"' ) {
                quoted = true;
            }
            else if ( quoted && c == '"' ) {
                quoted = false;
            }
            else if ( quoted && c == '\\' ) {
                sb.append((char) this.delegate.read());
            }
            else {
                sb.append((char) c);
            }
        }

        if ( sb.length() > 1 ) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }
}
