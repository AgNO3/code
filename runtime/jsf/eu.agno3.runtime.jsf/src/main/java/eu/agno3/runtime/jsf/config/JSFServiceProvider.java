/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 23, 2016 by mbechler
 */
package eu.agno3.runtime.jsf.config;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import javax.servlet.ServletContext;
import javax.validation.ValidatorFactory;

import org.apache.log4j.Logger;
import org.apache.myfaces.shared.util.serial.SerialFactory;
import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.configloader.ConfigSearchPaths;
import eu.agno3.runtime.configloader.file.ConfigFileLoaderImpl;
import eu.agno3.runtime.i18n.ResourceBundleService;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    JSFServiceProvider.class
}, immediate = true )
public class JSFServiceProvider {

    /**
     * 
     */
    private static final String RESOURCE_PROVIDER = "jsf.resourceProvider"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(JSFServiceProvider.class);

    private static JSFServiceProvider INSTANCE;

    private SerialFactory serialFactory;
    private ValidatorFactory validatorFactory;
    private ResourceBundleService resourceBundleService;


    /**
     * @return the instance
     */
    public static JSFServiceProvider getInstance () {
        JSFServiceProvider jsp = INSTANCE;
        if ( jsp == null ) {
            throw new FacesException("OSGI context not properly setup"); //$NON-NLS-1$
        }
        return jsp;
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        INSTANCE = this;
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        INSTANCE = null;
    }


    @Reference
    protected synchronized void setSerialFactory ( SerialFactory sf ) {
        this.serialFactory = sf;
    }


    protected synchronized void unsetSerialFactory ( SerialFactory sf ) {
        if ( this.serialFactory == sf ) {
            this.serialFactory = null;
        }
    }


    @Reference
    protected synchronized void setValidatorFactory ( ValidatorFactory vf ) {
        this.validatorFactory = vf;
    }


    protected synchronized void unsetValidatorFactory ( ValidatorFactory vf ) {
        if ( this.validatorFactory == vf ) {
            this.validatorFactory = null;
        }
    }


    @Reference
    protected synchronized void setResourceBundleService ( ResourceBundleService rbs ) {
        this.resourceBundleService = rbs;
    }


    protected synchronized void unsetResourceBundleService ( ResourceBundleService rbs ) {
        if ( this.resourceBundleService == rbs ) {
            this.resourceBundleService = null;
        }
    }


    /**
     * @return the serialFactory
     */
    public SerialFactory getSerialFactory () {
        return this.serialFactory;
    }


    /**
     * @return the validatorFactory
     */
    public ValidatorFactory getValidatorFactory () {
        return this.validatorFactory;
    }


    /**
     * @return the resourceBundleService
     */
    public ResourceBundleService getResourceBundleService () {
        return this.resourceBundleService;
    }


    /**
     * @param ctx
     * @return resource provider for the bundle hosting ctx
     */
    public OSGIResourceProvider getResourceProvider ( ExternalContext ctx ) {
        ServletContext sc = (ServletContext) ctx.getContext();
        Bundle contextBundle = (Bundle) sc.getAttribute("context.bundle"); //$NON-NLS-1$
        if ( contextBundle == null ) {
            throw new FacesException("Unknown context bundle"); //$NON-NLS-1$
        }

        OSGIResourceProvider cached = (OSGIResourceProvider) sc.getAttribute(RESOURCE_PROVIDER);
        if ( cached == null ) {
            cached = new OSGIResourceProvider(contextBundle, makeConfigFileLoader(contextBundle));
            cached.init();
            sc.setAttribute(RESOURCE_PROVIDER, cached);
        }
        return cached;
    }


    private static ConfigFileLoaderImpl makeConfigFileLoader ( Bundle contextBundle ) {
        List<File> searchDirs = new ArrayList<>();

        for ( File searchDir : ConfigSearchPaths.getSearchDirs() ) {
            File webCfgDir = new File(searchDir, "web"); //$NON-NLS-1$
            if ( log.isDebugEnabled() ) {
                log.debug("Checking customization dir " + webCfgDir); //$NON-NLS-1$
            }
            if ( webCfgDir.isDirectory() && webCfgDir.canRead() ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Found customization dir " + webCfgDir); //$NON-NLS-1$
                }
                searchDirs.add(webCfgDir);
            }
        }
        return new ConfigFileLoaderImpl(searchDirs, contextBundle);
    }

}
