/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.04.2015 by mbechler
 */
package eu.agno3.runtime.security.krb;


import java.io.Serializable;

import eu.agno3.runtime.net.ad.ADUserInfo;


/**
 * @author mbechler
 *
 */
public class ActiveDirectoryPrincipal implements Serializable {

    private ADUserInfo adUserInfo;


    /**
     * @param adUserInfo
     */
    public ActiveDirectoryPrincipal ( ADUserInfo adUserInfo ) {
        this.adUserInfo = adUserInfo;
    }


    /**
     * @return the adUserInfo
     */
    public ADUserInfo getAdUserInfo () {
        return this.adUserInfo;
    }

    /**
     * 
     */
    private static final long serialVersionUID = -6611642058834721003L;

}
