/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.10.2013 by mbechler
 */
package eu.agno3.runtime.security.web.internal;


import java.util.Collection;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.security.web.DenyFilter;
import eu.agno3.runtime.security.web.SecurityFilter;
import eu.agno3.runtime.security.web.config.FilterChainContributor;
import eu.agno3.runtime.security.web.config.FilterChainEntry;
import eu.agno3.runtime.security.web.config.FilterChainPriorities;
import eu.agno3.runtime.security.web.config.util.FilterChainBuilderImpl;


/**
 * @author mbechler
 * 
 */
@Component ( service = FilterChainContributor.class )
public class DefaultDenyPolicyContributor implements FilterChainContributor {

    private DenyFilter denyFilter;


    @Reference ( target = "(name=deny)" )
    protected synchronized void setDenyFilter ( SecurityFilter filter ) {
        this.denyFilter = (DenyFilter) filter;
    }


    protected synchronized void unsetDenyFilter ( SecurityFilter filter ) {
        if ( this.denyFilter == filter ) {
            this.denyFilter = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.security.web.config.FilterChainContributor#getChainContributions()
     */
    @Override
    public Collection<FilterChainEntry> getChainContributions () {
        return FilterChainBuilderImpl.create().forPath(FilterChainPriorities.FALLBACK, "/**") //$NON-NLS-1$
                .append(this.denyFilter.getFilterName()).complete().build();
    }

}
