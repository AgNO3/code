/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.10.2014 by mbechler
 */
package eu.agno3.runtime.security.internal;


import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationListener;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.pam.AuthenticationStrategy;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.realm.Realm;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.runtime.security.DynamicModularRealmAuthenticator;
import eu.agno3.runtime.security.login.LoginRealm;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.security.token.MultiFactorAuthenticationToken;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    DynamicModularRealmAuthenticator.class, ModularRealmAuthenticator.class
} )
public class DynamicMultiRealmAuthenticator extends ModularRealmAuthenticator implements DynamicModularRealmAuthenticator {

    private static final Logger log = Logger.getLogger(DynamicMultiRealmAuthenticator.class);

    private final Set<LoginRealm> realms = Collections.synchronizedSet(new HashSet<LoginRealm>());
    private final Set<AuthenticationListener> listeners = Collections.synchronizedSet(new HashSet<AuthenticationListener>());

    private final Set<String> realmIds = Collections.synchronizedSet(new HashSet<String>());

    private Map<String, List<LoginRealm>> stackCache = new ConcurrentHashMap<>();


    /**
     * 
     */
    public DynamicMultiRealmAuthenticator () {
        super();
    }


    @Reference ( policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE, updated = "updatedRealm" )
    protected synchronized void bindRealm ( Realm r ) {

        if ( ! ( r instanceof LoginRealm ) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Not binding realm as it is not an authenticating realm " + r.getClass().getName()); //$NON-NLS-1$
            }
            return;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Binding realm " + r); //$NON-NLS-1$
        }

        if ( !this.realmIds.add(r.getName()) ) {
            log.warn("Duplicate realm identifiers " + r.getName()); //$NON-NLS-1$
            return;
        }

        this.realms.add((LoginRealm) r);

        this.updateRealms();
    }


    protected synchronized void updatedRealm ( Realm r ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Realm was updated " + r); //$NON-NLS-1$
        }
        this.stackCache.clear();
    }


    protected synchronized void unbindRealm ( Realm r ) {
        if ( ! ( r instanceof LoginRealm ) ) {
            return;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Unbinding realm " + r); //$NON-NLS-1$
        }
        this.realmIds.remove(r.getName());
        this.realms.remove(r);
        this.updateRealms();
    }


    @Override
    public Collection<Realm> getRealms () {
        return Collections.unmodifiableCollection(super.getRealms());
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.authc.pam.ModularRealmAuthenticator#getAuthenticationStrategy()
     */
    @Override
    public AuthenticationStrategy getAuthenticationStrategy () {
        return new ProperAtLeastOneSuccessfulStrategy();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.authc.pam.ModularRealmAuthenticator#doAuthenticate(org.apache.shiro.authc.AuthenticationToken)
     */
    @Override
    protected AuthenticationInfo doAuthenticate ( AuthenticationToken authenticationToken ) throws AuthenticationException {
        if ( authenticationToken instanceof MultiFactorAuthenticationToken ) {
            return doMFAuthentication((MultiFactorAuthenticationToken) authenticationToken);
        }
        return super.doAuthenticate(authenticationToken);
    }


    protected AuthenticationInfo doMFAuthentication ( MultiFactorAuthenticationToken token ) {

        Collection<UserPrincipal> ups = token.getInfo().getPrincipals().byType(UserPrincipal.class);

        if ( ups == null || ups.size() != 1 ) {
            log.warn("Multiple user principals returned from multi factor auth: " + ups); //$NON-NLS-1$
            return null;
        }

        UserPrincipal up = token.getInfo().getPrincipals().oneByType(UserPrincipal.class);
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Multifactor authenticate %s (id: %s)", up, up.getUserId())); //$NON-NLS-1$
            for ( Object o : token.getInfo().getPrincipals().asList() ) {
                log.debug(o);
            }
        }
        return token.getInfo();
    }


    @Override
    public List<LoginRealm> getStack ( LoginRealm primary ) {
        List<LoginRealm> cached = this.stackCache.get(primary.getId());
        if ( cached != null ) {
            return cached;
        }
        cached = StackResolver.buildStack(primary, this.realms);
        this.stackCache.put(primary.getId(), cached);
        return cached;
    }


    /**
     * 
     */
    private void updateRealms () {
        this.stackCache.clear();
        this.setRealms(Collections.unmodifiableCollection(this.realms));
    }


    @Reference ( policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE )
    protected synchronized void bindAuthenticationListener ( AuthenticationListener l ) {
        this.listeners.add(l);
        this.updateListeners();
    }


    protected synchronized void unbindAuthenticationListener ( AuthenticationListener l ) {
        this.listeners.remove(l);
        this.updateListeners();
    }


    /**
     * 
     */
    private void updateListeners () {
        this.setAuthenticationListeners(this.listeners);
    }
}
