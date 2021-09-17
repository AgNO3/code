/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 14, 2017 by mbechler
 */
package eu.agno3.runtime.crypto.wrap;


import java.util.Map;


/**
 * @author mbechler
 *
 */
public class CryptBlob {

    private byte[] iv;
    private byte[] encrypted;
    private Map<byte[], byte[]> encKeys;


    /**
     * @param iv
     * @param encrypted
     * @param encKeys
     */
    public CryptBlob ( byte[] iv, byte[] encrypted, Map<byte[], byte[]> encKeys ) {
        this.iv = iv;
        this.encrypted = encrypted;
        this.encKeys = encKeys;
    }


    /**
     * @return the iv
     */
    public byte[] getIv () {
        return this.iv;
    }


    /**
     * @return the encrypted
     */
    public byte[] getEncrypted () {
        return this.encrypted;
    }


    /**
     * @return the encKeys
     */
    public Map<byte[], byte[]> getKeys () {
        return this.encKeys;
    }

}
