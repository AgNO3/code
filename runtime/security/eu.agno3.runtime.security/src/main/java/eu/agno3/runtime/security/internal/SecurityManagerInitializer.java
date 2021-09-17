/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.10.2013 by mbechler
 */
package eu.agno3.runtime.security.internal;


import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.authz.ModularRealmAuthorizer;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.util.osgi.DsUtil;


/**
 * @author mbechler
 * 
 */
@Component ( immediate = true )
public class SecurityManagerInitializer {

    private ServiceRegistration<org.apache.shiro.mgt.SecurityManager> securityManagerRegistration;

    private ModularRealmAuthenticator modAuth;
    private ModularRealmAuthorizer modAuthz;


    @Reference
    protected synchronized void setAuthRealm ( ModularRealmAuthenticator authenticator ) {
        this.modAuth = authenticator;
    }


    protected synchronized void unsetAuthRealm ( ModularRealmAuthenticator authenticator ) {
        if ( this.modAuth == authenticator ) {
            this.modAuth = null;
        }
    }


    @Reference
    protected synchronized void setAuthzRealm ( ModularRealmAuthorizer authorizer ) {
        this.modAuthz = authorizer;
    }


    protected synchronized void unsetAuthzRealm ( ModularRealmAuthorizer authorizer ) {
        if ( this.modAuthz == authorizer ) {
            this.modAuthz = null;
        }
    }


    @Activate
    protected synchronized void activate ( ComponentContext context ) {
        DefaultSecurityManager securityManager = new DefaultSecurityManager();
        securityManager.setAuthenticator(this.modAuth);
        securityManager.setAuthorizer(this.modAuthz);
        SecurityUtils.setSecurityManager(securityManager);
        this.securityManagerRegistration = DsUtil.registerSafe(context, org.apache.shiro.mgt.SecurityManager.class, securityManager, null);
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext context ) {
        if ( this.securityManagerRegistration != null ) {
            DsUtil.unregisterSafe(context, this.securityManagerRegistration);
            this.securityManagerRegistration = null;
        }
        SecurityUtils.setSecurityManager(null);
    }

}
