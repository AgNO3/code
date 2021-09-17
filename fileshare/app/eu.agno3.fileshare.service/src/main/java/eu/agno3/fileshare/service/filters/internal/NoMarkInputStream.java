/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.06.2015 by mbechler
 */
package eu.agno3.fileshare.service.filters.internal;


import java.io.IOException;
import java.io.InputStream;


/**
 * @author mbechler
 *
 */
public class NoMarkInputStream extends InputStream {

    private InputStream delegate;


    /**
     * @param delegate
     * 
     */
    public NoMarkInputStream ( InputStream delegate ) {
        this.delegate = delegate;
    }


    @Override
    public int read () throws IOException {
        return this.delegate.read();
    }


    @Override
    public int hashCode () {
        return this.delegate.hashCode();
    }


    @Override
    public int read ( byte[] b ) throws IOException {
        return this.delegate.read(b);
    }


    @Override
    public boolean equals ( Object obj ) {
        return this.delegate.equals(obj);
    }


    @Override
    public int read ( byte[] b, int off, int len ) throws IOException {
        return this.delegate.read(b, off, len);
    }


    @Override
    public long skip ( long n ) throws IOException {
        return this.delegate.skip(n);
    }


    @Override
    public String toString () {
        return this.delegate.toString();
    }


    @Override
    public int available () throws IOException {
        return this.delegate.available();
    }


    @Override
    public void close () throws IOException {
        this.delegate.close();
    }


    @Override
    public synchronized void mark ( int readlimit ) {}


    /**
     * @throws IOException
     * @see java.io.InputStream#reset()
     */
    @Override
    public synchronized void reset () throws IOException {}


    @Override
    public boolean markSupported () {
        return false;
    }

}
