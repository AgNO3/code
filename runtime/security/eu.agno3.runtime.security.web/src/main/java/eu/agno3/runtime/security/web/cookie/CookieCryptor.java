/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.09.2016 by mbechler
 */
package eu.agno3.runtime.security.web.cookie;


import javax.servlet.http.Cookie;

import eu.agno3.runtime.crypto.CryptoException;


/**
 * @author mbechler
 *
 */
public interface CookieCryptor {

    /**
     * @param name
     * @param value
     * @param t
     * @return encoded cookie
     * @throws CryptoException
     */
    Cookie encodeCookie ( String name, String value, CookieType t ) throws CryptoException;


    /**
     * @param value
     * @param t
     * @return decoded value
     * @throws CryptoException
     */
    String decodeCookie ( Cookie value, CookieType t ) throws CryptoException;

}
