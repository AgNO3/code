/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.10.2014 by mbechler
 */
package eu.agno3.runtime.security;


/**
 * @author mbechler
 *
 */
public class SaltedHash {

    private String salt;
    private String hash;


    /**
     * @param hash
     * @param salt
     */
    public SaltedHash ( String hash, String salt ) {
        this.hash = hash;
        this.salt = salt;
    }


    /**
     * @return the hash
     */
    public String getHash () {
        return this.hash;
    }


    /**
     * @return the salt
     */
    public String getSalt () {
        return this.salt;
    }
}
