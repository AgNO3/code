/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.04.2015 by mbechler
 */
package eu.agno3.orchestrator.crypto.keystore;


import java.io.Serializable;
import java.util.List;


/**
 * @author mbechler
 *
 */
public class KeyInfo implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4192504464407398527L;
    private String keyAlias;
    private String type;
    private String encodedPublicKey;

    private List<CertificateInfo> certificateChain;


    /**
     * @return the keyAlias
     */
    public String getKeyAlias () {
        return this.keyAlias;
    }


    /**
     * @param alias
     */
    public void setKeyAlias ( String alias ) {
        this.keyAlias = alias;
    }


    /**
     * @return the algorithm
     */
    public String getKeyType () {
        return this.type;
    }


    /**
     * @param type
     *            the algorithm to set
     */
    public void setKeyType ( String type ) {
        this.type = type;
    }


    /**
     * @return the encodedPublicKey
     */
    public String getEncodedPublicKey () {
        return this.encodedPublicKey;
    }


    /**
     * @param pubkeyEncoded
     */
    public void setEncodedPublicKey ( String pubkeyEncoded ) {
        this.encodedPublicKey = pubkeyEncoded;
    }


    /**
     * @return the certificateChain
     */
    public List<CertificateInfo> getCertificateChain () {
        return this.certificateChain;
    }


    /**
     * @param certificateChain
     *            the certificateChain to set
     */
    public void setCertificateChain ( List<CertificateInfo> certificateChain ) {
        this.certificateChain = certificateChain;
    }
}
