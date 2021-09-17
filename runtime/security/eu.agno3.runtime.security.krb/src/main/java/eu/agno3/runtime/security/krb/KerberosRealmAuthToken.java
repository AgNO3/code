/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.04.2015 by mbechler
 */
package eu.agno3.runtime.security.krb;


import javax.security.auth.kerberos.KerberosPrincipal;

import org.apache.shiro.authc.AuthenticationToken;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;


/**
 * @author mbechler
 *
 */
public class KerberosRealmAuthToken implements AuthenticationToken {

    /**
     * 
     */
    private static final long serialVersionUID = 9035572192986805205L;
    private KerberosPrincipal principal;
    private String realm;


    /**
     * 
     */
    public KerberosRealmAuthToken () {
        super();
    }


    /**
     * @param realm
     * @param ctx
     * @throws GSSException
     */
    public KerberosRealmAuthToken ( String realm, GSSContext ctx ) throws GSSException {
        super();
        this.realm = realm;
        this.principal = new KerberosPrincipal(ctx.getSrcName().toString(), KerberosPrincipal.KRB_NT_PRINCIPAL);
    }


    /**
     * 
     * @return the realm name
     */
    public String getRealm () {
        return this.realm;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.authc.AuthenticationToken#getCredentials()
     */
    @Override
    public Object getCredentials () {
        return this.principal;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.authc.AuthenticationToken#getPrincipal()
     */
    @Override
    public KerberosPrincipal getPrincipal () {
        return this.principal;
    }

}