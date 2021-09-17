/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 16, 2017 by mbechler
 */
package eu.agno3.runtime.security.credentials;


import java.io.Serializable;


/**
 * @author mbechler
 *
 */
public class AdaptedCryptBlobKeyEntry implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4048397710145827846L;
    private byte[] encryptedKey;
    private byte[] publicKeyBytes;


    /**
     * 
     */
    public AdaptedCryptBlobKeyEntry () {}


    /**
     * 
     * @param publicKey
     * @param keyData
     */
    public AdaptedCryptBlobKeyEntry ( byte[] publicKey, byte[] keyData ) {
        this.publicKeyBytes = publicKey;
        this.encryptedKey = keyData;
    }


    /**
     * @return public key data
     */
    public byte[] getPublicKeyBytes () {
        return this.publicKeyBytes;
    }


    /**
     * @param publicKeyBytes
     *            the publicKeyBytes to set
     */
    public void setPublicKeyBytes ( byte[] publicKeyBytes ) {
        this.publicKeyBytes = publicKeyBytes;
    }


    /**
     * @return encrypted key data
     */
    public byte[] getEncryptedKey () {
        return this.encryptedKey;
    }


    /**
     * @param encryptedKey
     *            the encryptedKey to set
     */
    public void setEncryptedKey ( byte[] encryptedKey ) {
        this.encryptedKey = encryptedKey;
    }

}
