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
public class KeyStoreInfo implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5184563889370550947L;

    private String alias;
    private List<KeyInfo> keyEntries;
    private String validationTrustStore;
    private boolean internal;


    /**
     * @return the alias
     */
    public String getAlias () {
        return this.alias;
    }


    /**
     * @param alias
     *            the alias to set
     */
    public void setAlias ( String alias ) {
        this.alias = alias;
    }


    /**
     * @return the keyEntries
     */
    public List<KeyInfo> getKeyEntries () {
        return this.keyEntries;
    }


    /**
     * @param keyEntries
     *            the keyEntries to set
     */
    public void setKeyEntries ( List<KeyInfo> keyEntries ) {
        this.keyEntries = keyEntries;
    }


    /**
     * @return the validationTrustStore
     */
    public String getValidationTrustStore () {
        return this.validationTrustStore;
    }


    /**
     * @param validationTruststoreName
     */
    public void setValidationTrustStore ( String validationTruststoreName ) {
        this.validationTrustStore = validationTruststoreName;
    }


    /**
     * @return the internal
     */
    public boolean isInternal () {
        return this.internal;
    }


    /**
     * @param internal
     */
    public void setInternal ( boolean internal ) {
        this.internal = internal;
    }
}
