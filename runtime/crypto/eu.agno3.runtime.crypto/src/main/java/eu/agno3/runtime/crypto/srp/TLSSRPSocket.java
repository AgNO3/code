/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.01.2017 by mbechler
 */
package eu.agno3.runtime.crypto.srp;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.bouncycastle.crypto.tls.TlsProtocol;


/**
 * @author mbechler
 *
 */
public class TLSSRPSocket implements AutoCloseable {

    private Socket delegate;
    private InputStream input;
    private OutputStream output;
    private TlsProtocol proto;


    /**
     * @param sock
     * @param proto
     */
    public TLSSRPSocket ( Socket sock, TlsProtocol proto ) {
        this.delegate = sock;
        this.input = proto.getInputStream();
        this.output = proto.getOutputStream();
        this.proto = proto;
    }


    /**
     * @return the input
     */
    public InputStream getInput () {
        return this.input;
    }


    /**
     * @return the output
     */
    public OutputStream getOutput () {
        return this.output;
    }


    /**
     * @return the proto
     */
    public TlsProtocol getProto () {
        return this.proto;
    }


    /**
     * @return the delegate
     */
    public Socket getDelegate () {
        return this.delegate;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.net.Socket#close()
     */
    @Override
    public synchronized void close () throws IOException {

        if ( this.output != null ) {
            this.output.close();
            this.output = null;
        }

        if ( this.input != null ) {
            this.input.close();
            this.input = null;
        }

        if ( this.proto != null ) {
            this.proto.close();
            this.proto = null;
        }

        if ( this.delegate != null ) {
            this.delegate.close();
            this.delegate = null;
        }

    }
}
