/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.07.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config.auth;


import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.orchestrator.config.auth.ad.ADAuthenticatorConfig;
import eu.agno3.orchestrator.realms.RealmLookupResult;
import eu.agno3.orchestrator.server.webgui.components.OuterWrapper;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "adAuthConfigBean" )
public class ADAuthConfigBean {

    @Inject
    private SIDLookupCache sidCache;


    public static String getDomain ( OuterWrapper<?> outer ) {
        if ( outer == null ) {
            return null;
        }
        OuterWrapper<?> outerWrapper = outer.get("urn:agno3:objects:1.0:auth:authenticator:ad"); //$NON-NLS-1$
        if ( outerWrapper == null ) {
            return null;
        }

        try {
            ADAuthenticatorConfig current = (ADAuthenticatorConfig) outerWrapper.getEditor().getCurrent();
            if ( current != null ) {
                return current.getDomain();
            }

            ADAuthenticatorConfig defaults = (ADAuthenticatorConfig) outerWrapper.getEditor().getDefaults();
            if ( defaults != null ) {
                return defaults.getDomain();
            }
            return null;
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return null;
        }
    }


    public RealmLookupResult lookupDomainSID ( String origin, String sid ) {
        try {
            return this.sidCache.lookupDomain(origin, sid);
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return null;
        }
    }


    public RealmLookupResult lookupSID ( String domain, String sid ) {
        try {
            return this.sidCache.lookup(domain, sid);
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return null;
        }
    }


    public List<RealmLookupResult> lookupDomainByName ( String origin, String name ) {
        try {
            return this.sidCache.lookupDomainByName(origin, name);
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return Collections.EMPTY_LIST;
        }
    }


    public List<RealmLookupResult> search ( String domain, String filter ) {
        try {
            return this.sidCache.searchName(domain, filter);
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return Collections.EMPTY_LIST;
        }
    }
}
