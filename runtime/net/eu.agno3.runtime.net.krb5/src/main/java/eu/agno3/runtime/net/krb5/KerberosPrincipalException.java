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
public class KerberosPrincipalException extends KerberosException {

    /**
     * 
     */
    private static final long serialVersionUID = 3614279448324923895L;
    private KerberosPrincipal principal;


    /**
     * 
     */
    public KerberosPrincipalException () {
        super();
    }


    /**
     * @param m
     * @param princ
     * @param t
     */
    public KerberosPrincipalException ( String m, KerberosPrincipal princ, Throwable t ) {
        super(m, t);
        this.principal = princ;
    }


    /**
     * @param m
     * @param princ
     */
    public KerberosPrincipalException ( String m, KerberosPrincipal princ ) {
        super(m);
        this.principal = princ;
    }


    /**
     * @param princ
     * @param t
     */
    public KerberosPrincipalException ( KerberosPrincipal princ, Throwable t ) {
        super(t);
        this.principal = princ;
    }


    /**
     * @return the principal
     */
    public KerberosPrincipal getPrincipal () {
        return this.principal;
    }
}
