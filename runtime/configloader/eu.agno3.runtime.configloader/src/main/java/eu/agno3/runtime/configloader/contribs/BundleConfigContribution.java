/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.09.2014 by mbechler
 */
package eu.agno3.runtime.configloader.contribs;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;

import eu.agno3.runtime.util.osgi.ResourceUtil;


/**
 * @author mbechler
 * 
 */
public class BundleConfigContribution extends AbstractSinglePropertiesConfigContribution {

    /**
     * 
     */
    private static final String BUNDLE_ROOT = "/"; //$NON-NLS-1$
    /**
     * 
     */
    private static final String CONFIGURATION_PROPERTIES = "configuration.properties"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(BundleConfigContribution.class);

    private Bundle bundle;


    /**
     * 
     * @param bundle
     */
    public BundleConfigContribution ( Bundle bundle ) {
        super(-9999);
        this.bundle = bundle;
    }


    /**
     * @return the bundle
     */
    public Bundle getBundle () {
        return this.bundle;
    }


    @Override
    protected InputStream getInputStream () throws IOException {
        URL configUrl = ResourceUtil.safeFindEntry(this.bundle, BUNDLE_ROOT, CONFIGURATION_PROPERTIES);

        if ( configUrl != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Found configuration file in bundle " + this.bundle.getSymbolicName()); //$NON-NLS-1$
            }
            return configUrl.openStream();
        }

        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {
        if ( obj instanceof BundleConfigContribution ) {
            return this.bundle.equals( ( (BundleConfigContribution) obj ).bundle);
        }
        return super.equals(obj);
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        return this.bundle.hashCode();
    }
}
