/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jan 17, 2017 by mbechler
 */
package eu.agno3.runtime.net.krb5;


import javax.security.auth.kerberos.KerberosPrincipal;


/**
 * @author mbechler
 *
 */
public class KerberosPrincipalNotFoundException extends KerberosPrincipalException {

    /**
     * 
     */
    private static final long serialVersionUID = 5004500832834939449L;


    /**
     * 
     */
    public KerberosPrincipalNotFoundException () {}


    /**
     * @param m
     * @param princ
     * @param t
     */
    public KerberosPrincipalNotFoundException ( String m, KerberosPrincipal princ, Throwable t ) {
        super(m, princ, t);
    }


    /**
     * @param m
     * @param princ
     */
    public KerberosPrincipalNotFoundException ( String m, KerberosPrincipal princ ) {
        super(m, princ);
    }


    /**
     * @param princ
     * @param t
     */
    public KerberosPrincipalNotFoundException ( KerberosPrincipal princ, Throwable t ) {
        super(princ, t);
    }

}
