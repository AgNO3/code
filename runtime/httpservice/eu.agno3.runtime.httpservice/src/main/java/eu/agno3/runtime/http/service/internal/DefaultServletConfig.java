/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.07.2015 by mbechler
 */
package eu.agno3.runtime.http.service.internal;


import java.util.Enumeration;

import javax.servlet.ServletContext;


/**
 * @author mbechler
 *
 */
final class DefaultServletConfig implements javax.servlet.ServletConfig {

    private DefaultServletContextHandler contextHandler;
    private String servletName;


    /**
     * @param name
     * @param contextHandler
     * 
     */
    public DefaultServletConfig ( String name, DefaultServletContextHandler contextHandler ) {
        this.servletName = name;
        this.contextHandler = contextHandler;
    }


    @Override
    public String getServletName () {
        return this.servletName;
    }


    @Override
    public ServletContext getServletContext () {
        return this.contextHandler.getServletContext();
    }


    @Override
    public Enumeration<String> getInitParameterNames () {
        return null;
    }


    @Override
    public String getInitParameter ( String arg0 ) {
        return null;
    }
}