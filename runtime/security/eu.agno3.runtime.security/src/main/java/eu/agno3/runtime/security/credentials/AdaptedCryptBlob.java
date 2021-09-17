/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 16, 2017 by mbechler
 */
package eu.agno3.runtime.security.credentials;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import eu.agno3.runtime.crypto.wrap.CryptBlob;


/**
 * @author mbechler
 *
 */
public class AdaptedCryptBlob implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6324279108075926463L;
    private byte[] iv;
    private byte[] data;
    private List<AdaptedCryptBlobKeyEntry> encKeyEntries = new ArrayList<>();


    /**
     * @return crypto blob
     */
    public CryptBlob toCryptBlob () {
        return new CryptBlob(this.iv, this.data, getKeys());
    }


    /**
     * @return the iv
     */
    public byte[] getIv () {
        return this.iv;
    }


    /**
     * @param iv
     *            the iv to set
     */
    public void setIv ( byte[] iv ) {
        this.iv = iv;
    }


    /**
     * @return the data
     */
    public byte[] getData () {
        return this.data;
    }


    /**
     * @param data
     *            the data to set
     */
    public void setData ( byte[] data ) {
        this.data = data;
    }


    /**
     * @return the encKeyEntries
     */
    public List<AdaptedCryptBlobKeyEntry> getEncKeyEntries () {
        return this.encKeyEntries;
    }


    /**
     * @param encKeyEntries
     *            the encKeyEntries to set
     */
    public void setEncKeyEntries ( List<AdaptedCryptBlobKeyEntry> encKeyEntries ) {
        this.encKeyEntries = encKeyEntries;
    }


    /**
     * @return
     */
    private Map<byte[], byte[]> getKeys () {
        Map<byte[], byte[]> keys = new HashMap<>();
        for ( AdaptedCryptBlobKeyEntry ke : this.encKeyEntries ) {
            keys.put(ke.getPublicKeyBytes(), ke.getEncryptedKey());
        }
        return keys;
    }


    /**
     * 
     * @param cb
     * @return adapted crypto blob
     */
    public static AdaptedCryptBlob fromCryptBlob ( CryptBlob cb ) {
        AdaptedCryptBlob ad = new AdaptedCryptBlob();
        fromBlob(cb, ad);
        return ad;
    }


    /**
     * @param cb
     * @param ad
     */
    static void fromBlob ( CryptBlob cb, AdaptedCryptBlob ad ) {
        ad.iv = cb.getIv();
        ad.data = cb.getEncrypted();
        for ( Entry<byte[], byte[]> ke : cb.getKeys().entrySet() ) {
            ad.encKeyEntries.add(new AdaptedCryptBlobKeyEntry(ke.getKey(), ke.getValue()));
        }
    }

}
