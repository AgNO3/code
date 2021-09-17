/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.02.2015 by mbechler
 */
package eu.agno3.runtime.crypto.secret;


import eu.agno3.runtime.crypto.CryptoException;


/**
 * @author mbechler
 *
 */
public interface SecretKeyProvider {

    /**
     * 
     * @param keyId
     * @param kvno
     * @return an existing,exportable persistent secret
     * @throws CryptoException
     */
    public SecretKeyWithVersion getExportableSecret ( String keyId, int kvno ) throws CryptoException;


    /**
     * 
     * @param keyId
     * @param kvno
     * @param algo
     * @return an existing persistent secret
     * @throws CryptoException
     */
    public SecretKeyWithVersion getSecret ( String keyId, int kvno, String algo ) throws CryptoException;


    /**
     * 
     * @param keyId
     * @param kvno
     * @param keySize
     * @return a new exportable,persistent secret
     * @throws CryptoException
     */
    public SecretKeyWithVersion getOrCreateExportableSecret ( String keyId, int kvno, int keySize ) throws CryptoException;


    /**
     * 
     * @param keyId
     * @param kvno
     * @param algo
     * @param keySize
     * @return a new persistent secret
     * @throws CryptoException
     */
    public SecretKeyWithVersion getOrCreateSecret ( String keyId, int kvno, String algo, int keySize ) throws CryptoException;
}
