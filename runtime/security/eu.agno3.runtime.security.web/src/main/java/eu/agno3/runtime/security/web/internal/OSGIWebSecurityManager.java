/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.10.2013 by mbechler
 */
package eu.agno3.runtime.security.web.internal;


import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.SimpleAccountRealm;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.runtime.ldap.filter.FilterBuilder;


/**
 * @author mbechler
 * 
 */
@Component ( service = WebSecurityManager.class, factory = OSGIWebSecurityManager.FACTORY )
public class OSGIWebSecurityManager extends DefaultWebSecurityManager {

    /**
     * 
     */
    private static final String CONTEXT_PROPERTY = "context"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(OSGIWebSecurityManager.class);

    protected static final String FACTORY = "eu.agno3.runtime.security.web.internal.OSGIWebSecurityManager"; //$NON-NLS-1$

    private Set<Realm> availableRealms = new HashSet<>();

    private String contextName = "UNKNOWN"; //$NON-NLS-1$


    @Activate
    protected void activate ( ComponentContext context ) {
        this.contextName = (String) context.getProperties().get(CONTEXT_PROPERTY);
    }


    @Deactivate
    protected void deactivate ( ComponentContext context ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Deactivate WebSecurityManager for " + this.contextName); //$NON-NLS-1$
        }
    }


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, target = "(_doesnotexist=*)" )
    protected synchronized void bindRealm ( Realm r ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Bind realm %s to context %s", r.getClass().getName(), this.contextName)); //$NON-NLS-1$
        }

        this.availableRealms.add(r);
        this.setRealms(this.availableRealms);
    }


    protected synchronized void unbindRealm ( Realm r ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Unbind realm %s from context %s", r.getClass().getName(), this.contextName)); //$NON-NLS-1$
        }

        this.availableRealms.remove(r);

        if ( this.availableRealms.isEmpty() ) {
            if ( log.isDebugEnabled() ) {
                log.debug("No authentication realm present in context " + this.contextName); //$NON-NLS-1$
            }
            this.setRealm(new SimpleAccountRealm("Uninitialized")); //$NON-NLS-1$
        }
        else {
            this.setRealms(this.availableRealms);
        }
    }


    protected static String makeContextFilter ( String contextName ) {
        FilterBuilder fb = FilterBuilder.get();
        return fb.or(fb.not(fb.exists(CONTEXT_PROPERTY)), fb.eq(CONTEXT_PROPERTY, contextName)).toString();
    }

}
