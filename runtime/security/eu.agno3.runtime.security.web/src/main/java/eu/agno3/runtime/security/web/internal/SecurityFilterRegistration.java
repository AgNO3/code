/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.10.2013 by mbechler
 */
package eu.agno3.runtime.security.web.internal;


import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.annotation.WebFilter;

import org.apache.shiro.web.servlet.ShiroFilter;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.http.service.filter.FilterConfig;


/**
 * @author mbechler
 * 
 */

@Component ( service = Filter.class, factory = SecurityFilterRegistration.FACTORY )
@WebFilter ( displayName = "Security filter", urlPatterns = "/*", dispatcherTypes = {
    DispatcherType.ERROR, DispatcherType.FORWARD, DispatcherType.INCLUDE, DispatcherType.REQUEST, DispatcherType.ASYNC
} )
@FilterConfig ( priority = -5000 )
public class SecurityFilterRegistration extends ShiroFilter {

    protected static final String FACTORY = "eu.agno3.runtime.security.web.internal.SecurityFilterRegistration"; //$NON-NLS-1$

}
