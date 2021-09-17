/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.08.2013 by mbechler
 */
package eu.agno3.runtime.xml.schema.internal;


import java.net.URL;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;

import eu.agno3.runtime.util.osgi.DsUtil;
import eu.agno3.runtime.util.osgi.ResourceUtil;
import eu.agno3.runtime.xml.schema.SchemaRegistration;
import eu.agno3.runtime.xml.schema.SchemaResolverException;
import eu.agno3.runtime.xml.schema.SchemaValidationConfig;


/**
 * @author mbechler
 * 
 */
@Component ( immediate = true )
public class BundleSchemaRegistrations implements BundleTrackerCustomizer<Object> {

    /**
     * 
     */
    private static final String XSD_PATTERN = "*.xsd"; //$NON-NLS-1$

    /**
     * 
     */
    private static final String XSD_FILE_BASE = "/xsd/"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(BundleSchemaRegistrations.class);

    private ComponentContext componentContext;
    private BundleTracker<Object> tracker;
    private Map<Bundle, Set<ServiceRegistration<SchemaRegistration>>> registrations = new HashMap<>();
    private URLSchemaRegistrationFactory urlRegFactory;

    private SchemaValidationConfig validationConfig;


    @Activate
    protected void activate ( ComponentContext context ) {
        this.componentContext = context;
        this.tracker = new BundleTracker<>(context.getBundleContext(), Bundle.RESOLVED | Bundle.STARTING | Bundle.ACTIVE | Bundle.STOPPING, this);
        this.tracker.open();
        log.debug("Starting XML Schema bundle tracker"); //$NON-NLS-1$
    }


    @Deactivate
    protected void deactivate ( ComponentContext context ) {
        this.tracker.close();
        this.tracker = null;
        this.componentContext = null;
    }


    @Reference
    protected synchronized void setURLRegFactory ( URLSchemaRegistrationFactory factory ) {
        this.urlRegFactory = factory;
    }


    protected synchronized void unsetURLRegFactory ( URLSchemaRegistrationFactory factory ) {
        if ( this.urlRegFactory == factory ) {
            this.urlRegFactory = null;
        }
    }


    @Reference
    protected synchronized void setValidationConfig ( SchemaValidationConfig svc ) {
        this.validationConfig = svc;
    }


    protected synchronized void unsetValidationConfig ( SchemaValidationConfig svc ) {
        if ( this.validationConfig == svc ) {
            this.validationConfig = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.BundleTrackerCustomizer#addingBundle(org.osgi.framework.Bundle,
     *      org.osgi.framework.BundleEvent)
     */
    @Override
    public Object addingBundle ( Bundle bundle, BundleEvent event ) {
        if ( event == null || event.getType() == BundleEvent.RESOLVED ) {
            if ( log.isTraceEnabled() ) {
                log.trace(String.format("Bundle resolved %s [%d]: %s", bundle.getSymbolicName(), bundle.getBundleId(), event)); //$NON-NLS-1$
            }

            List<URL> schemaFiles = ResourceUtil.safeFindPattern(bundle, XSD_FILE_BASE, XSD_PATTERN, true);

            if ( schemaFiles.isEmpty() ) {
                return null;
            }

            Set<ServiceRegistration<SchemaRegistration>> bundleRegistrations = new HashSet<>();

            for ( URL schemaFile : schemaFiles ) {
                addSchemaFile(bundleRegistrations, schemaFile);
            }

            this.registrations.put(bundle, bundleRegistrations);

        }
        else if ( event.getType() == BundleEvent.UNRESOLVED ) {
            this.removedBundle(bundle, event, null);
        }

        return null;
    }


    /**
     * @param bundleRegistrations
     * @param schemaFile
     */
    private void addSchemaFile ( Set<ServiceRegistration<SchemaRegistration>> bundleRegistrations, URL schemaFile ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Found schema file " + schemaFile.getPath()); //$NON-NLS-1$
        }

        try {
            URLSchemaRegistration reg = this.urlRegFactory.createURLSchemaRegistration(schemaFile);
            Dictionary<String, Object> regProperties = new Hashtable<>();
            regProperties.put("targetNamespace", reg.getTargetNamespace()); //$NON-NLS-1$

            if ( log.isDebugEnabled() ) {
                log.debug("Registering Schema for targetNamespace " + reg.getTargetNamespace()); //$NON-NLS-1$
            }
            bundleRegistrations.add(DsUtil.registerSafe(this.componentContext, SchemaRegistration.class, reg, regProperties));
        }
        catch ( SchemaResolverException e ) {
            log.error("Failed to load schema file " + schemaFile, e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.BundleTrackerCustomizer#modifiedBundle(org.osgi.framework.Bundle,
     *      org.osgi.framework.BundleEvent, java.lang.Object)
     */
    @Override
    public void modifiedBundle ( Bundle bundle, BundleEvent event, Object object ) {}


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.BundleTrackerCustomizer#removedBundle(org.osgi.framework.Bundle,
     *      org.osgi.framework.BundleEvent, java.lang.Object)
     */
    @Override
    public void removedBundle ( Bundle bundle, BundleEvent event, Object object ) {

        if ( this.registrations.containsKey(bundle) ) {
            for ( ServiceRegistration<SchemaRegistration> reg : this.registrations.get(bundle) ) {
                DsUtil.unregisterSafe(this.componentContext, reg);
            }
            this.registrations.remove(bundle);
        }
    }

}
