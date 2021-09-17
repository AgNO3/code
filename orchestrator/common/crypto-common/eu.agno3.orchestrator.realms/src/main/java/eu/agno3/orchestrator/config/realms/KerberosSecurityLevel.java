/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.02.2016 by mbechler
 */
package eu.agno3.orchestrator.config.realms;


/**
 * @author mbechler
 *
 */
@SuppressWarnings ( "nls" )
public enum KerberosSecurityLevel {

    /**
     * High security, only AES256 keys
     */
    HIGH256("aes256-cts-hmac-sha1-96"),

    /**
     * High security, only AES keys
     */
    HIGH("aes256-cts-hmac-sha1-96", "aes128-cts-hmac-sha1-96"),

    /**
     * Legacy compatability, AES/3DES/RC4
     */
    LEGACY("aes256-cts-hmac-sha1-96", "aes128-cts-hmac-sha1-96", "arcfour-hmac-md5", "des3-cbc-sha1"),

    /**
     * Weak, LEGACY+DES
     */
    WEAK("aes256-cts-hmac-sha1-96", "aes128-cts-hmac-sha1-96", "arcfour-hmac-md5", "des3-cbc-sha1", "des-cbc-md5");

    private String[] etypes;


    /**
     * 
     */
    private KerberosSecurityLevel ( String... etypes ) {
        this.etypes = etypes;
    }


    /**
     * @return the etypes
     */
    public String[] getEtypes () {
        return this.etypes;
    }

}
