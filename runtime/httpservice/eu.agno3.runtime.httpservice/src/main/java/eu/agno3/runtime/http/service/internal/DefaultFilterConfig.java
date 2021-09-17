/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.07.2015 by mbechler
 */
package eu.agno3.runtime.http.service.internal;


import java.util.Enumeration;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;


/**
 * @author mbechler
 *
 */
public class DefaultFilterConfig implements FilterConfig {

    private DefaultServletContextHandler servletContext;
    private String name;


    /**
     * @param name
     * @param context
     * 
     */
    public DefaultFilterConfig ( String name, DefaultServletContextHandler context ) {
        this.name = name;
        this.servletContext = context;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.FilterConfig#getFilterName()
     */
    @Override
    public String getFilterName () {
        return this.name;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.FilterConfig#getInitParameter(java.lang.String)
     */
    @Override
    public String getInitParameter ( String arg0 ) {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.FilterConfig#getInitParameterNames()
     */
    @Override
    public Enumeration<String> getInitParameterNames () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.FilterConfig#getServletContext()
     */
    @Override
    public ServletContext getServletContext () {
        return this.servletContext.getServletContext();
    }

}
