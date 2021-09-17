/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.07.2013 by mbechler
 */
package eu.agno3.runtime.http.service.servlet;


import java.util.Collection;

import javax.servlet.Servlet;

import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;


/**
 * @author mbechler
 * 
 */
public interface ServletInfo {

    /**
     * 
     * @return the contexts name
     */
    String getContextName ();


    /**
     * Returns the registered servlets
     * 
     * @return collection of servlet holders
     */
    Collection<ServletHolder> getServlets ();


    /**
     * Gets the registered servlet mappings
     * 
     * @return a collections of servlet mappings
     */
    Collection<ServletMapping> getServletMappings ();


    /**
     * Get the servlet mapping applied to a specific path
     * 
     * @param contextPath
     * @return the servlet mapping for this path
     */
    ServletMapping getServletMapping ( String contextPath );


    /**
     * Return current mapping for a specific servlet
     * 
     * @param s
     * @return the servlets servlet mapping
     */
    ServletMapping getServletMapping ( Servlet s );

}