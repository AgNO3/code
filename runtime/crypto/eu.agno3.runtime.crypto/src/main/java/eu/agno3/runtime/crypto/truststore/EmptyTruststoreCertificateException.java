/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.01.2017 by mbechler
 */
package eu.agno3.runtime.crypto.truststore;


import java.security.cert.CertificateException;


/**
 * @author mbechler
 *
 */
public class EmptyTruststoreCertificateException extends CertificateException {

    /**
     * 
     */
    private static final long serialVersionUID = 6431517919442339962L;

    private String trustStore;


    /**
     * 
     */
    public EmptyTruststoreCertificateException () {
        super();
    }


    /**
     * @param message
     * @param ts
     * @param cause
     */
    public EmptyTruststoreCertificateException ( String message, String ts, Throwable cause ) {
        super(message, cause);
        this.trustStore = ts;
    }


    /**
     * @param msg
     * @param ts
     */
    public EmptyTruststoreCertificateException ( String msg, String ts ) {
        super(msg);
        this.trustStore = ts;
    }


    /**
     * @param ts
     * @param cause
     */
    public EmptyTruststoreCertificateException ( String ts, Throwable cause ) {
        super(cause);
        this.trustStore = ts;
    }


    /**
     * @return the trustStore
     */
    public String getTrustStore () {
        return this.trustStore;
    }
}
