/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 23, 2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.logs;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.primefaces.model.StreamedContent;


/**
 * @author mbechler
 *
 */
public class LogDownloadContent implements StreamedContent {

    private static final Logger log = Logger.getLogger(LogDownloadContent.class);

    String filename;
    List<String> items;


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }


    /**
     * @param filename
     * @param items
     */
    public LogDownloadContent ( String filename, List<String> items ) {
        this.filename = filename;
        this.items = items != null ? items : Collections.EMPTY_LIST;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.model.StreamedContent#getContentLength()
     */
    @Override
    public Integer getContentLength () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.model.StreamedContent#getName()
     */
    @Override
    public String getName () {
        return this.filename;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.model.StreamedContent#getStream()
     */
    @Override
    public InputStream getStream () {
        return new LogInputStream();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.model.StreamedContent#getContentType()
     */
    @Override
    public String getContentType () {
        return "application/json"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.model.StreamedContent#getContentEncoding()
     */
    @Override
    public String getContentEncoding () {
        return null;
    }

    public class LogInputStream extends InputStream {

        private Iterator<String> itemIterator = LogDownloadContent.this.items.iterator();

        private ByteArrayInputStream cur = null;


        /**
         * {@inheritDoc}
         *
         * @see java.io.InputStream#read()
         */
        @Override
        public int read () throws IOException {

            if ( this.cur == null || this.cur.available() == 0 ) {
                if ( !getNext() ) {
                    return -1;
                }
            }
            int read = this.cur.read();
            if ( read < 0 ) {
                this.cur = null;
                return read();
            }
            return read;
        }


        /**
         * {@inheritDoc}
         *
         * @see java.io.InputStream#read(byte[])
         */
        @Override
        public int read ( byte[] b ) throws IOException {
            if ( this.cur == null || this.cur.available() == 0 ) {
                if ( !getNext() ) {
                    return -1;
                }
            }
            int read = this.cur.read(b);
            if ( read < 0 ) {
                this.cur = null;
                return read();
            }
            return read;
        }


        /**
         * {@inheritDoc}
         *
         * @see java.io.InputStream#read(byte[], int, int)
         */
        @Override
        public int read ( byte[] b, int off, int len ) throws IOException {
            if ( this.cur == null || this.cur.available() == 0 ) {
                if ( !getNext() ) {
                    return -1;
                }
            }
            int read = this.cur.read(b, off, len);
            if ( read < 0 ) {
                this.cur = null;
                return read();
            }
            return read;
        }


        public boolean getNext () {
            if ( !this.itemIterator.hasNext() ) {
                return false;
            }

            String next = this.itemIterator.next() + "\n"; //$NON-NLS-1$
            this.cur = new ByteArrayInputStream(next.getBytes(StandardCharsets.UTF_8));
            return true;
        }


        /**
         * {@inheritDoc}
         *
         * @see java.io.InputStream#close()
         */
        @Override
        public void close () throws IOException {
            LogDownloadContent.this.items.clear();
            super.close();
        }
    }
}
