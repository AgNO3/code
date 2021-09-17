/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2014 by mbechler
 */
package eu.agno3.runtime.http.service.internal;


import java.util.Dictionary;
import java.util.Hashtable;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.osgi.framework.ServiceReference;

import eu.agno3.runtime.http.service.HttpConfigurationException;


/**
 * @author mbechler
 * 
 */
public final class ServletUtil {

    private static final String URL_PATTERN_SEPARATOR = ","; //$NON-NLS-1$
    private static final String URL_PATTERNS_PROPERTY = "urlPatterns"; //$NON-NLS-1$
    private static final String DISPLAY_NAME_PROPERTY = "displayName"; //$NON-NLS-1$

    private static final String NO_URL_PATTERN_CONFIGURED = "No URL pattern configured"; //$NON-NLS-1$


    private ServletUtil () {}


    static String[] applyServletConfiguration ( Servlet s, ServletHolder holder, ServiceReference<Servlet> ref ) throws HttpConfigurationException {

        WebServlet servletAnnotation = s.getClass().getAnnotation(WebServlet.class);

        String[] patterns = null;
        if ( servletAnnotation != null ) {
            patterns = applyServletConfigurationFromAnnotation(s, holder, servletAnnotation);
        }
        else {
            patterns = applyServletConfigurationFromProperties(s, holder, ref);
        }

        for ( String key : ref.getPropertyKeys() ) {
            holder.setInitParameter(key, String.valueOf(ref.getProperty(key)));
        }

        return patterns;

    }


    /**
     * @param s
     * @param holder
     * @param ref
     * @param patterns
     * @return
     * @throws HttpConfigurationException
     */
    private static String[] applyServletConfigurationFromProperties ( Servlet s, ServletHolder holder, ServiceReference<Servlet> ref )
            throws HttpConfigurationException {
        String[] patterns = null;
        holder.setName(s.getClass().getName());

        String displayNameSpec = (String) ref.getProperty(DISPLAY_NAME_PROPERTY);
        if ( displayNameSpec != null ) {
            holder.setDisplayName(displayNameSpec);
        }

        String urlPatternSpec = (String) ref.getProperty(URL_PATTERNS_PROPERTY);
        if ( urlPatternSpec != null ) {
            patterns = urlPatternSpec.split(Pattern.quote(URL_PATTERN_SEPARATOR));
        }
        else {
            throw new HttpConfigurationException(NO_URL_PATTERN_CONFIGURED);
        }
        return patterns;
    }


    /**
     * @param s
     * @param holder
     * @param servletAnnotation
     * @param patterns
     * @return
     * @throws HttpConfigurationException
     */
    private static String[] applyServletConfigurationFromAnnotation ( Servlet s, ServletHolder holder, WebServlet servletAnnotation )
            throws HttpConfigurationException {
        String[] patterns = null;
        if ( servletAnnotation.urlPatterns() != null ) {
            patterns = servletAnnotation.urlPatterns();
        }
        else if ( servletAnnotation.value() != null ) {
            patterns = servletAnnotation.value();
        }
        else {
            throw new HttpConfigurationException(NO_URL_PATTERN_CONFIGURED);
        }

        if ( servletAnnotation.displayName() != null ) {
            holder.setDisplayName(servletAnnotation.displayName());
        }

        if ( servletAnnotation.name().length() > 0 ) {
            holder.setName(servletAnnotation.name());
        }
        else {
            holder.setName(s.getClass().getName());
        }

        if ( servletAnnotation.loadOnStartup() != -1 ) {
            holder.setInitOrder(servletAnnotation.loadOnStartup());
        }

        if ( servletAnnotation.asyncSupported() ) {
            holder.setAsyncSupported(servletAnnotation.asyncSupported());
        }

        if ( servletAnnotation.initParams() != null ) {
            for ( WebInitParam param : servletAnnotation.initParams() ) {
                holder.setInitParameter(param.name(), param.value());
            }
        }
        return patterns;
    }


    static String[] applyFilterConfiguration ( Filter f, FilterHolder holder, ServiceReference<Filter> ref ) throws HttpConfigurationException {

        WebFilter filterAnnotation = f.getClass().getAnnotation(WebFilter.class);

        String[] patterns = null;

        if ( filterAnnotation != null ) {

            patterns = applyFilterConfigurationFromAnnotation(f, holder, filterAnnotation);
        }
        else {
            holder.setName(f.getClass().getName());
        }

        for ( String key : ref.getPropertyKeys() ) {
            holder.setInitParameter(key, String.valueOf(ref.getProperty(key)));
        }

        return patterns;

    }


    /**
     * @param f
     * @param holder
     * @param filterAnnotation
     * @param patterns
     * @return
     * @throws HttpConfigurationException
     */
    private static String[] applyFilterConfigurationFromAnnotation ( Filter f, FilterHolder holder, WebFilter filterAnnotation )
            throws HttpConfigurationException {
        String[] patterns = null;

        if ( filterAnnotation.urlPatterns() != null ) {
            patterns = filterAnnotation.urlPatterns();
        }
        else if ( filterAnnotation.value() != null ) {
            patterns = filterAnnotation.value();
        }
        else {
            throw new HttpConfigurationException(NO_URL_PATTERN_CONFIGURED);
        }

        if ( filterAnnotation.displayName() != null ) {
            holder.setDisplayName(filterAnnotation.displayName());
        }

        if ( filterAnnotation.filterName().length() > 0 ) {
            holder.setName(filterAnnotation.filterName());
        }
        else {
            holder.setName(f.getClass().getName());
        }

        if ( filterAnnotation.asyncSupported() ) {
            holder.setAsyncSupported(filterAnnotation.asyncSupported());
        }

        if ( filterAnnotation.initParams() != null ) {
            for ( WebInitParam param : filterAnnotation.initParams() ) {
                holder.setInitParameter(param.name(), param.value());
            }
        }
        return patterns;
    }


    /**
     * @param ref
     * @return
     */
    static Dictionary<String, Object> cloneProperties ( ServiceReference<?> ref ) {
        Dictionary<String, Object> res = new Hashtable<>();

        if ( ref.getPropertyKeys() != null ) {
            for ( String key : ref.getPropertyKeys() ) {
                res.put(key, ref.getProperty(key));
            }
        }

        return res;
    }


    /**
     * @param s
     * @param ref
     * @return whether the service has a url pattern configured
     */
    public static boolean hasURLPattern ( Servlet s, ServiceReference<Servlet> ref ) {

        WebServlet servletAnnotation = s.getClass().getAnnotation(WebServlet.class);
        String[] patterns = null;

        if ( servletAnnotation == null ) {
            String urlPatternSpec = (String) ref.getProperty(URL_PATTERNS_PROPERTY);
            if ( urlPatternSpec != null ) {
                patterns = urlPatternSpec.split(Pattern.quote(URL_PATTERN_SEPARATOR));
            }
        }
        else if ( servletAnnotation.urlPatterns() != null ) {
            patterns = servletAnnotation.urlPatterns();
        }
        else if ( servletAnnotation.value() != null ) {
            patterns = servletAnnotation.value();
        }

        if ( patterns == null || patterns.length == 0 ) {
            return false;
        }
        return true;
    }

}
