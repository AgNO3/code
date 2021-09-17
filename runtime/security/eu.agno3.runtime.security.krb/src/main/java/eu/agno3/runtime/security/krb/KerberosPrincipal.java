/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.03.2015 by mbechler
 */
package eu.agno3.runtime.security.krb;


import java.io.Serializable;


/**
 * @author mbechler
 *
 */
public class KerberosPrincipal implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2942762156033097520L;
    private javax.security.auth.kerberos.KerberosPrincipal principal;


    /**
     * @param principal
     */
    public KerberosPrincipal ( javax.security.auth.kerberos.KerberosPrincipal principal ) {
        this.principal = principal;
    }


    /**
     * @return the realm name
     */
    public String getRealm () {
        return this.principal.getRealm();
    }


    /**
     * 
     * @return the principal name
     */
    public String getPrincipal () {
        return this.principal.getName();

    }


    /**
     * 
     * @return the kerberos principal
     */
    public javax.security.auth.kerberos.KerberosPrincipal getKerberosPrincipal () {
        return this.principal;
    }
}
