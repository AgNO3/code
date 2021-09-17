/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.05.2015 by mbechler
 */
package eu.agno3.runtime.security.password;


import java.util.Locale;


/**
 * @author mbechler
 *
 */
public interface PasswordGenerator {

    /**
     * 
     * @param type
     * @param entropy
     * @param l
     * @return a new random password
     * @throws PasswordGenerationException
     */
    public String generate ( PasswordType type, int entropy, Locale l ) throws PasswordGenerationException;
}
