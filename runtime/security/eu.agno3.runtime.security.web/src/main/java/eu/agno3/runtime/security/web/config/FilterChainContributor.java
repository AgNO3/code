/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.10.2013 by mbechler
 */
package eu.agno3.runtime.security.web.config;


import java.util.Collection;


/**
 * @author mbechler
 * 
 */
public interface FilterChainContributor {

    /**
     * 
     * @return a collection of filter chain contributions
     */
    Collection<FilterChainEntry> getChainContributions ();

}
