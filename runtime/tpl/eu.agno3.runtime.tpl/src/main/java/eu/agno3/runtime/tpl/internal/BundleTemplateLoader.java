/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.03.2014 by mbechler
 */
package eu.agno3.runtime.tpl.internal;


import java.net.URL;

import org.osgi.framework.Bundle;

import eu.agno3.runtime.util.osgi.ResourceUtil;

import freemarker.cache.URLTemplateLoader;


/**
 * @author mbechler
 * 
 */
public class BundleTemplateLoader extends URLTemplateLoader {

    /**
     * 
     */
    private static final String TPL_BASE = "/tpl/"; //$NON-NLS-1$

    private String prefix = TPL_BASE;
    private Bundle bundle;


    /**
     * @param bundle
     */
    public BundleTemplateLoader ( Bundle bundle ) {
        this.bundle = bundle;
    }


    /**
     * @param bundle
     * @param prefix
     * 
     */
    public BundleTemplateLoader ( Bundle bundle, String prefix ) {
        this.bundle = bundle;
        this.prefix = prefix;
    }


    /**
     * {@inheritDoc}
     * 
     * @see freemarker.cache.URLTemplateLoader#getURL(java.lang.String)
     */
    @Override
    protected URL getURL ( String tplName ) {
        return ResourceUtil.safeFindEntry(this.bundle, this.prefix, tplName);
    }
}
