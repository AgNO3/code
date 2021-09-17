/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 17, 2017 by mbechler
 */
package eu.agno3.runtime.crypto.scrypt;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;


/**
 * @author mbechler
 *
 */
public class SCryptor {

    /**
     * Generate a hash
     * 
     * @param args
     * @throws IOException
     */
    public static void main ( String[] args ) throws IOException {
        SCryptParams params = new SCryptParams(1 << 14 - 1, 8, 1);
        byte[] salt = new byte[32];
        ( new SecureRandom() ).nextBytes(salt);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int r;
        while ( ( r = System.in.read() ) >= 0 ) {
            bos.write(r);
        }
        SCryptResult hashed = SCryptUtil.generate(bos.toByteArray(), salt, params);
        System.out.println(hashed.export());
    }
}
