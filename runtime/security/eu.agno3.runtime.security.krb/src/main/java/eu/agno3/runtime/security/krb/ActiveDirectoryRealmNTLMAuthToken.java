/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.03.2015 by mbechler
 */
package eu.agno3.runtime.security.krb;


import org.apache.shiro.authc.AuthenticationToken;

import eu.agno3.runtime.net.ad.ADUserInfo;
import eu.agno3.runtime.net.ad.ntlm.NTLMContext;


/**
 * @author mbechler
 *
 */
public class ActiveDirectoryRealmNTLMAuthToken implements AuthenticationToken {

    /**
     * 
     */
    private static final long serialVersionUID = 3885753398349493108L;
    private String realm;
    private ADUserInfo info;


    /**
     * @param realm
     * @param ctx
     */
    public ActiveDirectoryRealmNTLMAuthToken ( String realm, NTLMContext ctx ) {
        this.realm = realm;
        this.info = ctx.getUserInfo();
    }


    /**
     * @return the realm
     */
    public String getRealm () {
        return this.realm;
    }


    /**
     * @return the info
     */
    public ADUserInfo getInfo () {
        return this.info;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.authc.AuthenticationToken#getCredentials()
     */
    @Override
    public Object getCredentials () {
        return this.info;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.authc.AuthenticationToken#getPrincipal()
     */
    @Override
    public Object getPrincipal () {
        return this.info;
    }

}
