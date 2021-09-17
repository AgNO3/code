/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2014 by mbechler
 */
package eu.agno3.runtime.security.web.config;


import java.util.Collection;


/**
 * @author mbechler
 * 
 */
public interface FilterChainBuilder {

    /**
     * @param e
     * @return the filter chain builder for further use
     */
    FilterChainBuilder withEntry ( FilterChainEntry e );


    /**
     * 
     * @param priority
     * @param path
     * @param filterSpec
     * @return the filter chain builder for further use
     */
    FilterChainBuilder withChain ( int priority, String path, String filterSpec );


    /**
     * 
     * @param prio
     * @param path
     * @param filterSpec
     * @return the filter chain builder for further use
     */
    FilterChainBuilder withChain ( FilterChainPriorities prio, String path, String filterSpec );


    /**
     * 
     * @param priority
     * @param path
     * @return a FilterChainSpecBuilder
     */
    FilterChainSpecBuilder forPath ( int priority, String path );


    /**
     * 
     * @param prio
     * @param path
     * @return a FilterChainSpecBuilder
     */
    FilterChainSpecBuilder forPath ( FilterChainPriorities prio, String path );


    /**
     * @return the constructed FilterChainEntry set
     */
    Collection<FilterChainEntry> build ();

}