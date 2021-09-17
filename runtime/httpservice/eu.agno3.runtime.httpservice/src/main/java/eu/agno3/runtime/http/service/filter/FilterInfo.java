/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.07.2013 by mbechler
 */
package eu.agno3.runtime.http.service.filter;


import java.util.Collection;

import javax.servlet.Filter;

import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.FilterMapping;


/**
 * @author mbechler
 * 
 */
public interface FilterInfo {

    /**
     * 
     * @return the contexts name
     */
    String getContextName ();


    /**
     * Returns the registered filters
     * 
     * @return collection of filter holders
     */
    Collection<FilterHolder> getFilters ();


    /**
     * Gets the registered filter mappings
     * 
     * @return a collections of filter mappings
     */
    Collection<FilterMapping> getFilterMappings ();


    /**
     * Return current mapping for a specific filter
     * 
     * @param f
     * @return the filters mapping
     */
    FilterMapping getFilterMapping ( Filter f );

}