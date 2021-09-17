/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.03.2015 by mbechler
 */
package eu.agno3.runtime.crypto.tls;


/**
 * @author mbechler
 *
 */
public class TLSCipherSuiteSpec {

    private TLSKeyAlgorithm keyAlgo;

    private TLSEncryptionAlgorithm encAlgo;

    private TLSHashAlgorithm hash;


    /**
     * @param keyAlgo
     * @param encAlgo
     * @param hash
     * 
     */
    public TLSCipherSuiteSpec ( TLSKeyAlgorithm keyAlgo, TLSEncryptionAlgorithm encAlgo, TLSHashAlgorithm hash ) {
        this.keyAlgo = keyAlgo;
        this.encAlgo = encAlgo;
        this.hash = hash;
    }


    /**
     * @return the keyAlgo
     */
    public TLSKeyAlgorithm getKeyAlgo () {
        return this.keyAlgo;
    }


    /**
     * @return the encAlgo
     */
    public TLSEncryptionAlgorithm getEncAlgo () {
        return this.encAlgo;
    }


    /**
     * @return the hash
     */
    public TLSHashAlgorithm getHash () {
        return this.hash;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format("TLS_%s_WITH_%s_%s", //$NON-NLS-1$
            TLSKeyAlgorithm.toString(this.keyAlgo),
            TLSEncryptionAlgorithm.toString(this.encAlgo),
            TLSHashAlgorithm.toString(this.hash));
    }
}
