/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.04.2015 by mbechler
 */
package eu.agno3.orchestrator.crypto.jobs;


import eu.agno3.orchestrator.jobs.DefaultGroup;
import eu.agno3.orchestrator.jobs.JobImpl;


/**
 * @author mbechler
 *
 */
public class GenerateKeyJob extends JobImpl {

    private String keyStore;
    private String keyAlias;
    private String keyType;


    /**
     * 
     */
    public GenerateKeyJob () {
        super(new DefaultGroup());
    }


    /**
     * @return the keyStore
     */
    public String getKeyStore () {
        return this.keyStore;
    }


    /**
     * @param keyStore
     *            the keyStore to set
     */
    public void setKeyStore ( String keyStore ) {
        this.keyStore = keyStore;
    }


    /**
     * @return the keyAlias
     */
    public String getKeyAlias () {
        return this.keyAlias;
    }


    /**
     * @param keyAlias
     *            the keyAlias to set
     */
    public void setKeyAlias ( String keyAlias ) {
        this.keyAlias = keyAlias;
    }


    /**
     * @return the key type
     */
    public String getKeyType () {
        return this.keyType;
    }


    /**
     * @param keyType
     *            the keyType to set
     */
    public void setKeyType ( String keyType ) {
        this.keyType = keyType;
    }

}
