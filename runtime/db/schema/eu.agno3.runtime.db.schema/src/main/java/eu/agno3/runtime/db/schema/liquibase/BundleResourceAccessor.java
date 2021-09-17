/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.07.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.liquibase;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;

import eu.agno3.runtime.util.osgi.ResourceUtil;


/**
 * @author mbechler
 * 
 */
public class BundleResourceAccessor extends ClassLoaderResourceAccessor implements ResourceAccessor {

    private static final Logger log = Logger.getLogger(BundleResourceAccessor.class);

    private static final String BUNDLE_ROOT = "/"; //$NON-NLS-1$
    private Bundle bundle;


    /**
     * @param b
     * 
     */
    public BundleResourceAccessor ( Bundle b ) {
        if ( b == null ) {
            throw new IllegalArgumentException("Bundle is null"); //$NON-NLS-1$
        }
        this.bundle = b;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see liquibase.resource.ClassLoaderResourceAccessor#getResourcesAsStream(java.lang.String)
     */
    @Override
    public Set<InputStream> getResourcesAsStream ( String r ) throws IOException {
        if ( log.isDebugEnabled() ) {
            log.debug("Fetching resource from bundle " + r); //$NON-NLS-1$
        }

        // allow loading of liquibase schema so that the schema is not downloaded from remote
        if ( r != null && r.startsWith("liquibase/parser/core/xml/") ) { //$NON-NLS-1$
            return super.getResourcesAsStream(r);
        }

        List<URL> u = ResourceUtil.safeFindEntries(this.bundle, BUNDLE_ROOT, r);

        if ( u == null || u.isEmpty() ) {
            return Collections.EMPTY_SET;
        }

        Set<InputStream> res = new HashSet<>();

        for ( URL url : u ) {
            res.add(url.openStream());
        }

        return res;
    }


    /**
     * {@inheritDoc}
     *
     * @see liquibase.resource.ResourceAccessor#list(java.lang.String, java.lang.String, boolean, boolean, boolean)
     */
    @Override
    public Set<String> list ( String relativeTo, String path, boolean includeFiles, boolean includeDirectories, boolean recursive )
            throws IOException {
        // TODO:
        throw new NotImplementedException("TODO if required"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.resource.ResourceAccessor#toClassLoader()
     */
    @Override
    public ClassLoader toClassLoader () {
        if ( this.bundle == null ) {
            return this.getClass().getClassLoader();
        }
        BundleWiring wiring = this.bundle.adapt(BundleWiring.class);
        if ( wiring == null ) {
            throw new IllegalStateException("No bundle wiring available"); //$NON-NLS-1$
        }
        return wiring.getClassLoader();
    }

}
