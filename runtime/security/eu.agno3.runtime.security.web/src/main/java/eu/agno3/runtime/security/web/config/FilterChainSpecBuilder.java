/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.10.2013 by mbechler
 */
package eu.agno3.runtime.security.web.config;


/**
 * @author mbechler
 * 
 */
public interface FilterChainSpecBuilder {

    /**
     * @param filterName
     * @return the builder
     */
    FilterChainSpecBuilder append ( String filterName );


    /**
     * 
     * @param filterName
     * @param filterConfigSpec
     * @return the builder
     */
    FilterChainSpecBuilder append ( String filterName, String filterConfigSpec );


    /**
     * 
     * @return the outer FilterChainBuilder
     */
    FilterChainBuilder complete ();
}
