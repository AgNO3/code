/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2014 by mbechler
 */
package eu.agno3.runtime.http.service.internal;


import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.servlet.Servlet;

import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;

import eu.agno3.runtime.http.service.servlet.ServletInfo;


/**
 * @author mbechler
 * 
 */
class ServletInfoAdapter implements ServletInfo {

    private DefaultServletContextHandler contextHandler;
    private ContextServletHolder holder;


    /**
     * @param holder
     * 
     */
    ServletInfoAdapter ( DefaultServletContextHandler contextHandler, ContextServletHolder holder ) {
        this.contextHandler = contextHandler;
        this.holder = holder;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.http.service.servlet.ServletInfo#getContextName()
     */
    @Override
    public String getContextName () {
        return this.contextHandler.getContextName();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.http.service.servlet.ServletInfo#getServlets()
     */
    @Override
    public synchronized Collection<ServletHolder> getServlets () {
        return this.holder.getServlets();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.http.service.servlet.ServletInfo#getServletMappings()
     */
    @Override
    public synchronized Collection<ServletMapping> getServletMappings () {
        if ( this.contextHandler.getServletHandler() == null ) {
            return Collections.EMPTY_LIST;
        }

        return Arrays.asList(this.contextHandler.getServletHandler().getServletMappings());
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.http.service.servlet.ServletInfo#getServletMapping(java.lang.String)
     */
    @Override
    public synchronized ServletMapping getServletMapping ( String contextPath ) {
        if ( this.contextHandler.getServletHandler() == null ) {
            return null;
        }

        return this.contextHandler.getServletHandler().getServletMapping(contextPath);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.http.service.servlet.ServletInfo#getServletMapping(javax.servlet.Servlet)
     */
    @Override
    public synchronized ServletMapping getServletMapping ( Servlet s ) {
        return this.holder.getServletMapping(s);
    }
}
