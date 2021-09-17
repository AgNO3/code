/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.07.2013 by mbechler
 */
package eu.agno3.runtime.http.service.internal;


import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.Servlet;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.eclipse.jetty.http.pathmap.MappedResource;
import org.eclipse.jetty.http.pathmap.PathMappings;
import org.eclipse.jetty.http.pathmap.ServletPathSpec;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.util.resource.Resource;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.util.tracker.BundleTracker;

import eu.agno3.runtime.http.service.HttpConfigurationException;
import eu.agno3.runtime.http.service.handler.ContextHandlerConfig;
import eu.agno3.runtime.http.service.resource.ResourceDescriptor;
import eu.agno3.runtime.http.service.resource.ResourceInfo;
import eu.agno3.runtime.util.osgi.ResourceUtil;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    Servlet.class, ResourceInfo.class
}, configurationPid = DefaultResourceServlet.PID, configurationPolicy = ConfigurationPolicy.REQUIRE )
@WebServlet ( urlPatterns = "/*", name = "resources", displayName = "Resource Handler", initParams = {
    @WebInitParam ( name = "gzip", value = "false" ), @WebInitParam ( name = "dirAllowed", value = "false" ),
    @WebInitParam ( name = "aliases", value = "false" )
})
public class DefaultResourceServlet extends DefaultServlet implements ResourceInfo {

    private static final long serialVersionUID = -3487290654949054906L;
    private static final Logger log = Logger.getLogger(DefaultResourceServlet.class);

    /**
     * Bundle header for resource registration
     */
    public static final String WWW_RESOURCE_HEADER = "WWW-Resource"; //$NON-NLS-1$

    /**
     * Configuration PID
     */
    public static final String PID = "httpservice.resources"; //$NON-NLS-1$

    private String contextName;
    private Set<String> bindToConnectors = null;
    private PathMappings<ResourceDescriptor> resources;
    private transient BundleContext bundleContext;
    private transient BundleTracker<List<ResourceDescriptor>> bundleTracker;


    /**
     * 
     */
    public DefaultResourceServlet () {
        this.resources = new PathMappings<>();
    }


    /**
     * @return the bundleContext
     */
    public BundleContext getBundleContext () {
        return this.bundleContext;
    }


    @Activate
    @Modified
    protected synchronized void activate ( ComponentContext context ) throws HttpConfigurationException {
        this.bundleContext = context.getBundleContext();
        this.contextName = (String) context.getProperties().get(ContextHandlerConfig.CONTEXT_NAME_ATTR);

        if ( this.contextName == null ) {
            throw new HttpConfigurationException("DefaultResourceServlet has no context name set"); //$NON-NLS-1$
        }

        this.bundleTracker = new BundleTracker<>(this.bundleContext, Bundle.ACTIVE | Bundle.RESOLVED, new ResourceBundleTracker(this));
        this.bundleTracker.open();
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext context ) {
        if ( this.bundleTracker != null ) {
            this.bundleTracker.close();
            this.bundleTracker = null;
        }
        this.bundleContext = null;
    }


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindResource ( ResourceDescriptor res ) {
        this.addResource(res);
    }


    protected synchronized void unbindResource ( ResourceDescriptor res ) {
        if ( this.hasResource(res) ) {
            this.removeResource(res);
        }
    }


    @Override
    public String getContextName () {
        return this.contextName;
    }


    /**
     * @param res
     */
    protected synchronized void removeResource ( ResourceDescriptor res ) {
        for ( Iterator<MappedResource<ResourceDescriptor>> iterator = this.resources.getMappings().iterator(); iterator.hasNext(); ) {
            MappedResource<ResourceDescriptor> mappedResource = iterator.next();
            if ( res.equals(mappedResource.getResource()) ) {
                iterator.remove();
            }
        }
    }


    @Override
    public synchronized boolean hasResource ( ResourceDescriptor res ) {
        for ( MappedResource<ResourceDescriptor> mappedResource : this.resources.getMappings() ) {
            if ( res.equals(mappedResource.getResource()) ) {
                return true;
            }
        }
        return false;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.http.service.resource.ResourceInfo#getResource()
     */
    @Override
    public Collection<ResourceDescriptor> getResource () {
        List<ResourceDescriptor> descs = new ArrayList<>();
        for ( MappedResource<ResourceDescriptor> mappedResource : this.resources.getMappings() ) {
            descs.add(mappedResource.getResource());
        }
        return descs;
    }


    /**
     * Add a resource registration
     * 
     * @param res
     */
    protected synchronized void addResource ( ResourceDescriptor res ) {

        if ( res.getContexts() != null && !res.getContexts().contains(this.getContextName()) ) {
            log.trace(String.format(
                "Resource %s with scope %s did not match context %s", //$NON-NLS-1$
                res.getClass().getName(),
                res.getContexts(),
                this.getContextName()));
            return;
        }

        log.debug(
            String.format("Adding resource to path %s in context %s with priority %d", res.getPath(), this.getContextName(), res.getPriority())); //$NON-NLS-1$

        this.resources.put(new ServletPathSpec(res.getPath()), res);
    }


    @Override
    public Resource getResource ( String path ) {

        log.debug("Trying to serve using DefaultResourceServlet"); //$NON-NLS-1$

        List<MappedResource<ResourceDescriptor>> matches = this.resources.getMatches(path);

        if ( matches.isEmpty() ) {
            if ( log.isDebugEnabled() ) {
                log.debug("No match found for " + path); //$NON-NLS-1$
            }
            for ( MappedResource<ResourceDescriptor> mr : this.resources.getMappings() ) {
                ResourceDescriptor r = mr.getResource();
                if ( log.isTraceEnabled() ) {
                    log.trace(String.format("%s registered from %s [%d]", r.getPath(), r.getBundle().getSymbolicName(), r.getBundle().getBundleId())); //$NON-NLS-1$
                }
            }

            return null;
        }

        ResourceDescriptor bestMatch = findBestMatch(matches);

        log.debug(String.format(
            "Best match in bundle %s [%d], serving", //$NON-NLS-1$
            bestMatch.getBundle().getSymbolicName(),
            bestMatch.getBundle().getBundleId()));

        return getResourceFromBundle(path, bestMatch);
    }


    /**
     * @param path
     * @param bestMatch
     * @return
     */
    private static Resource getResourceFromBundle ( String path, ResourceDescriptor bestMatch ) {
        URL urlToUse = ResourceUtil.safeFindEntry(bestMatch.getBundle(), bestMatch.getResourceBase(), path);
        if ( urlToUse == null ) {
            log.warn("Failed to locate resource in bundle: " + path); //$NON-NLS-1$
            return null;
        }

        try {
            return new BundleURLResource(urlToUse, urlToUse.openConnection());
        }
        catch ( IOException e ) {
            log.warn("Failed to open resource " + urlToUse, e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * @param matches
     * @param sortedMatches
     * @return
     */
    private static ResourceDescriptor findBestMatch ( List<MappedResource<ResourceDescriptor>> matches ) {

        SortedSet<ResourceDescriptor> sortedMatches = new TreeSet<>(new ResourceInfoComparator());
        for ( MappedResource<ResourceDescriptor> match : matches ) {
            ResourceDescriptor info = match.getResource();

            if ( log.isTraceEnabled() ) {
                log.trace(String.format(
                    "Registration with context path %s from bundle %s [%d] matches request", //$NON-NLS-1$
                    info.getPath(),
                    info.getBundle().getSymbolicName(),
                    info.getBundle().getBundleId()));
            }
            sortedMatches.add(info);
        }

        return sortedMatches.last();
    }


    protected boolean matchResource ( HttpServletRequest httpReq, ResourceDescriptor res ) {
        if ( this.bindToConnectors == null ) {
            return true;
        }

        Enumeration<String> attrs = httpReq.getAttributeNames();

        while ( attrs.hasMoreElements() ) {
            log.debug(attrs.nextElement());
        }

        return true;
    }

}
