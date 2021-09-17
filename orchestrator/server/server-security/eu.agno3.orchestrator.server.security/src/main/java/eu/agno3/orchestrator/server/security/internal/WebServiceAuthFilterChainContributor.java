/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.security.internal;


import java.util.Collection;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.security.web.config.FilterChainContributor;
import eu.agno3.runtime.security.web.config.FilterChainEntry;
import eu.agno3.runtime.security.web.config.FilterChainPriorities;
import eu.agno3.runtime.security.web.config.util.FilterChainBuilderImpl;


/**
 * @author mbechler
 *
 */
@Component ( service = FilterChainContributor.class )
public class WebServiceAuthFilterChainContributor implements FilterChainContributor {

    @Reference
    protected synchronized void setWebServiceAuthFilter ( WebServiceAuthenticationFilter wsAuthFilter ) {
        // depenency only
    }


    protected synchronized void unsetWebServiceAuthFilter ( WebServiceAuthenticationFilter wsAuthFilter ) {
        // dependency only
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.config.FilterChainContributor#getChainContributions()
     */
    @Override
    public Collection<FilterChainEntry> getChainContributions () {
        return FilterChainBuilderImpl.create().forPath(FilterChainPriorities.GLOBAL_DEFAULT, "/**") //$NON-NLS-1$
                .append(WebServiceAuthenticationFilter.WS_AUTH_FILTER).complete().build();
    }

}
