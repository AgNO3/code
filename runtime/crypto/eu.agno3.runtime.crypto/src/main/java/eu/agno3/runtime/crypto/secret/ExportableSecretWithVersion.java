/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.09.2015 by mbechler
 */
package eu.agno3.runtime.crypto.secret;


import javax.crypto.spec.SecretKeySpec;


/**
 * @author mbechler
 *
 */
public class ExportableSecretWithVersion extends SecretKeySpec implements SecretKeyWithVersion {

    /**
     * 
     */
    public static final String UNSPECIFIED = "UNSPECIFIED"; //$NON-NLS-1$

    /**
     * 
     */
    private static final long serialVersionUID = 238561957209455662L;

    private int kvno;


    /**
     * 
     * @param key
     * @param kvno
     */
    public ExportableSecretWithVersion ( byte[] key, int kvno ) {
        this(key, kvno, UNSPECIFIED);
    }


    /**
     * @param key
     * @param kvno
     * @param algorithm
     */
    public ExportableSecretWithVersion ( byte[] key, int kvno, String algorithm ) {
        super(key, algorithm);
        this.kvno = kvno;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.secret.SecretKeyWithVersion#withAlgo(java.lang.String)
     */
    @Override
    public SecretKeyWithVersion withAlgo ( String algo ) {
        return new ExportableSecretWithVersion(getEncoded(), getVersion(), algo);
    }


    /**
     * @return the kvno
     */
    @Override
    public int getVersion () {
        return this.kvno;
    }

}
