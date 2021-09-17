/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.03.2017 by mbechler
 */
package eu.agno3.runtime.jmx.internal;


import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.binary.Base64;

import eu.agno3.runtime.crypto.scrypt.SCryptUtil;
import eu.agno3.runtime.jmx.CredentialChecker;


/**
 * @author mbechler
 *
 */
public class SimpleCredentialChecker implements CredentialChecker {

    private String user;
    private String salt;
    private byte[] key;


    /**
     * @param user
     * @param pass
     */
    public SimpleCredentialChecker ( String user, String pass ) {
        this.user = user;
        int li = pass.lastIndexOf('$');
        if ( li <= 0 ) {
            throw new IllegalArgumentException();
        }
        this.salt = pass.substring(0, li);
        this.key = Base64.decodeBase64(pass.substring(li + 1));
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jmx.CredentialChecker#verifyPassword(java.lang.String, java.lang.String)
     */
    @Override
    public boolean verifyPassword ( String ruser, String rpass ) {
        if ( !this.user.equals(ruser) ) {
            // let's not care about timing for user enumeration here, might as well ignore the user completely for now
            return false;
        }
        return SCryptUtil.check(rpass.getBytes(StandardCharsets.UTF_8), this.salt, this.key);
    }

}
