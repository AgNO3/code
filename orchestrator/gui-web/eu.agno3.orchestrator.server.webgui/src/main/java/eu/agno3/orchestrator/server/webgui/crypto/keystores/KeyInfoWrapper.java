/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.04.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.crypto.keystores;


import java.io.Serializable;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.List;

import eu.agno3.orchestrator.crypto.keystore.KeyInfo;
import eu.agno3.orchestrator.crypto.keystore.KeyStoreInfo;


/**
 * @author mbechler
 *
 */
public class KeyInfoWrapper implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5060861569755522858L;
    private KeyStoreInfo keystore;
    private KeyInfo key;
    private List<Certificate> certChain;
    private PublicKey publicKey;
    private String publicKeyFingerprint;


    /**
     * @return the keystore
     */
    public KeyStoreInfo getKeystore () {
        return this.keystore;
    }


    /**
     * @param keystore
     *            the keystore to set
     */
    public void setKeystore ( KeyStoreInfo keystore ) {
        this.keystore = keystore;
    }


    /**
     * @return the key
     */
    public KeyInfo getKey () {
        return this.key;
    }


    /**
     * @param key
     *            the key to set
     */
    public void setKey ( KeyInfo key ) {
        this.key = key;
    }


    /**
     * 
     * @return key alias for displaying
     */
    public String getDisplayKeyAlias () {
        return KeyInfoUtil.getDisplayKeyAlias(this.key);
    }


    /**
     * @return the certChain
     */
    public List<Certificate> getDecodedCertificateChain () {
        return this.certChain;
    }


    /**
     * @param certChain
     */
    public void setDecodedCertificateChain ( List<Certificate> certChain ) {
        this.certChain = certChain;
    }


    /**
     * @return the publicKey
     */
    public PublicKey getPublicKey () {
        return this.publicKey;
    }


    /**
     * @param publicKey
     *            the publicKey to set
     */
    public void setPublicKey ( PublicKey publicKey ) {
        this.publicKey = publicKey;
    }


    /**
     * 
     * @return the public key fingerprint
     */
    public String getPublicKeyFingerprint () {
        return this.publicKeyFingerprint;
    }


    /**
     * @param publicKeyFingerprint
     *            the publicKeyFingerprint to set
     */
    public void setPublicKeyFingerprint ( String publicKeyFingerprint ) {
        this.publicKeyFingerprint = publicKeyFingerprint;
    }
}
