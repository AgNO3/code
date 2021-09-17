/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.08.2014 by mbechler
 */
package eu.agno3.runtime.jsf.config;


import java.util.Locale;
import java.util.ResourceBundle;

import javax.faces.application.Application;
import javax.faces.application.ApplicationWrapper;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.apache.myfaces.application.ApplicationImpl;
import org.apache.myfaces.config.RuntimeConfig;

import eu.agno3.runtime.i18n.ResourceBundleService;


/**
 * @author mbechler
 * 
 */
public class OSGIApplicationWrapper extends ApplicationWrapper {

    private static final Logger log = Logger.getLogger(OSGIApplicationWrapper.class);

    private final Application wrapped;


    /**
     * @param wrapped
     */
    public OSGIApplicationWrapper ( Application wrapped ) {
        this.wrapped = wrapped;
        Application unwrapped = wrapped;

        while ( unwrapped instanceof ApplicationWrapper ) {
            unwrapped = ( (ApplicationWrapper) unwrapped ).getWrapped();
        }

        if ( wrapped instanceof ApplicationImpl ) {
            ApplicationImpl app = (ApplicationImpl) wrapped;
            app.setResolverBuilderForFaces(
                new ELResolverBuilder(RuntimeConfig.getCurrentInstance(FacesContext.getCurrentInstance().getExternalContext())));
        }
        else {
            log.warn("Failed to find ApplicationImpl"); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.application.ApplicationWrapper#getWrapped()
     */
    @Override
    public Application getWrapped () {
        return this.wrapped;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.application.ApplicationWrapper#getResourceBundle(javax.faces.context.FacesContext,
     *      java.lang.String)
     */
    @Override
    public ResourceBundle getResourceBundle ( FacesContext ctx, String name ) {

        org.apache.myfaces.config.element.ResourceBundle bundleConfig = RuntimeConfig.getCurrentInstance(ctx.getExternalContext())
                .getResourceBundle(name);

        if ( bundleConfig == null || bundleConfig.getBaseName() == null ) {
            return null;
        }

        ResourceBundleService rbs = JSFServiceProvider.getInstance().getResourceBundleService();
        Locale locale = Locale.getDefault();

        final UIViewRoot viewRoot = ctx.getViewRoot();
        if ( viewRoot != null && viewRoot.getLocale() != null ) {
            locale = viewRoot.getLocale();
        }

        return rbs.getBundle(bundleConfig.getBaseName(), locale, Thread.currentThread().getContextClassLoader());
    }

}
