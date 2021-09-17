/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2014 by mbechler
 */
package eu.agno3.runtime.http.service.internal;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTrackerCustomizer;

import eu.agno3.runtime.http.service.resource.BundleHeaderResource;
import eu.agno3.runtime.http.service.resource.ResourceDescriptor;


class ResourceBundleTracker implements BundleTrackerCustomizer<List<ResourceDescriptor>> {

    private static final Logger log = Logger.getLogger(ResourceBundleTracker.class);

    /**
     * 
     */
    private final DefaultResourceServlet resourceHandler;


    /**
     * @param resourceHandler
     */
    public ResourceBundleTracker ( DefaultResourceServlet resourceHandler ) {
        this.resourceHandler = resourceHandler;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.BundleTrackerCustomizer#addingBundle(org.osgi.framework.Bundle,
     *      org.osgi.framework.BundleEvent)
     */
    @Override
    public List<ResourceDescriptor> addingBundle ( Bundle bundle, BundleEvent event ) {

        String wwwResourceHeader = bundle.getHeaders().get(DefaultResourceServlet.WWW_RESOURCE_HEADER);

        if ( wwwResourceHeader == null ) {
            if ( log.isTraceEnabled() ) {
                log.trace(String.format("Bundle %s has no %s header set", bundle.getSymbolicName(), DefaultResourceServlet.WWW_RESOURCE_HEADER)); //$NON-NLS-1$
            }
            return Collections.EMPTY_LIST;
        }

        List<ResourceDescriptor> registered = new ArrayList<>();
        String[] registrations = wwwResourceHeader.split(Pattern.quote(";")); //$NON-NLS-1$

        for ( String registration : registrations ) {

            BundleHeaderResource resource = createResource(bundle, registration);
            registered.add(resource);
            this.resourceHandler.addResource(resource);

        }

        return registered;
    }


    /**
     * @param bundle
     * @param registration
     * @return
     */
    private static BundleHeaderResource createResource ( Bundle bundle, String registration ) {
        String pattern = null;
        String resourceBase = "/www-static/"; //$NON-NLS-1$
        Set<String> contexts = null;
        int priority = 0;

        int firstSep = registration.indexOf(':');

        if ( firstSep > 0 ) {
            pattern = registration.substring(0, firstSep).trim();

            if ( pattern.contains("@") ) { //$NON-NLS-1$
                int indexOfSeparator = pattern.indexOf('@');
                priority = Integer.parseInt(pattern.substring(indexOfSeparator));
                pattern = pattern.substring(0, indexOfSeparator);
            }

            int secondSep = registration.indexOf(':', firstSep + 1);

            if ( secondSep > 0 ) {
                resourceBase = registration.substring(firstSep + 1, secondSep);

                int thirdSep = registration.indexOf(':', secondSep + 1);

                if ( thirdSep > 0 ) {
                    contexts = new HashSet<>(Arrays.asList(registration.substring(thirdSep).split(Pattern.quote(",")))); //$NON-NLS-1$
                }
            }
            else {
                resourceBase = registration.substring(firstSep + 1);
            }
        }
        else {
            pattern = registration.trim();
            if ( pattern.contains("@") ) { //$NON-NLS-1$
                int indexOfSeparator = pattern.indexOf('@');
                priority = Integer.parseInt(pattern.substring(indexOfSeparator));
                pattern = pattern.substring(0, indexOfSeparator);
            }
        }

        String scopesSpec = "*"; //$NON-NLS-1$

        if ( contexts != null ) {
            scopesSpec = contexts.toString();
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Registering resource with pattern %s and base path %s from bundle %s [%d] in contexts %s", //$NON-NLS-1$
                pattern,
                resourceBase,
                bundle.getSymbolicName(),
                bundle.getBundleId(),
                scopesSpec));
        }

        return new BundleHeaderResource(pattern, contexts, priority, resourceBase, bundle);
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.BundleTrackerCustomizer#modifiedBundle(org.osgi.framework.Bundle,
     *      org.osgi.framework.BundleEvent, java.lang.Object)
     */
    @Override
    public void modifiedBundle ( Bundle bundle, BundleEvent event, List<ResourceDescriptor> object ) {}


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.BundleTrackerCustomizer#removedBundle(org.osgi.framework.Bundle,
     *      org.osgi.framework.BundleEvent, java.lang.Object)
     */
    @Override
    public void removedBundle ( Bundle bundle, BundleEvent event, List<ResourceDescriptor> object ) {

        if ( object != null ) {
            for ( ResourceDescriptor descriptor : object ) {
                this.resourceHandler.removeResource(descriptor);
            }
        }
    }

}