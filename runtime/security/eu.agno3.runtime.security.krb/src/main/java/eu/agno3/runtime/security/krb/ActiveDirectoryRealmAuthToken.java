/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.03.2015 by mbechler
 */
package eu.agno3.runtime.security.krb;


import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;

import eu.agno3.runtime.net.ad.ADUserInfo;


/**
 * @author mbechler
 *
 */
public class ActiveDirectoryRealmAuthToken extends KerberosRealmAuthToken {

    /**
     * 
     */
    private static final long serialVersionUID = 5267647210023659114L;
    private ADUserInfo adUserInfo;


    /**
     * 
     */
    public ActiveDirectoryRealmAuthToken () {}


    /**
     * @param realm
     * @param ctx
     * @param adUserInfo
     * @throws GSSException
     */
    public ActiveDirectoryRealmAuthToken ( String realm, GSSContext ctx, ADUserInfo adUserInfo ) throws GSSException {
        super(realm, ctx);
        this.adUserInfo = adUserInfo;
    }


    /**
     * @return the adUserInfo
     */
    public ADUserInfo getAdUserInfo () {
        return this.adUserInfo;
    }

}
