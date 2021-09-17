/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.08.2016 by mbechler
 */
package eu.agno3.fileshare.webgui.login;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.ops4j.pax.cdi.api.OsgiService;

import eu.agno3.runtime.security.login.LoginRealmManager;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "authInfoBean" )
public class AuthInfoBean {

    @Inject
    @OsgiService ( dynamic = true, timeout = 500 )
    private LoginRealmManager lrm;


    /**
     * 
     * @return whether the authentication config is a multi-realm one
     */
    public boolean getMultiRealm () {
        return this.lrm.isMultiRealm();
    }
}
