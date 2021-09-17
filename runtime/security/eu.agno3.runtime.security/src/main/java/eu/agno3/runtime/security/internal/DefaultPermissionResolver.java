/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.10.2014 by mbechler
 */
package eu.agno3.runtime.security.internal;


import org.apache.shiro.authz.permission.PermissionResolver;
import org.apache.shiro.authz.permission.WildcardPermissionResolver;
import org.osgi.service.component.annotations.Component;


/**
 * @author mbechler
 *
 */
@Component ( service = PermissionResolver.class )
public class DefaultPermissionResolver extends WildcardPermissionResolver {

    /**
     * 
     */
    public DefaultPermissionResolver () {}
}
