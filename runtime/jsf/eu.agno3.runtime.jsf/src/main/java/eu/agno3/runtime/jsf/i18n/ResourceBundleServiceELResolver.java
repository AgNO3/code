/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.10.2013 by mbechler
 */
package eu.agno3.runtime.jsf.i18n;


import java.beans.FeatureDescriptor;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.i18n.ResourceBundleService;


/**
 * @author mbechler
 * 
 */
@Component ( service = ResourceBundleServiceELResolver.class )
public class ResourceBundleServiceELResolver extends ELResolver {

    private static final Logger log = Logger.getLogger(ResourceBundleService.class);

    private ResourceBundleService resourceBundleService;


    @Reference
    protected synchronized void setResourceBundleService ( ResourceBundleService service ) {
        this.resourceBundleService = service;
    }


    protected synchronized void unsetResourceBundleService ( ResourceBundleService service ) {
        if ( this.resourceBundleService == service ) {
            this.resourceBundleService = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.el.ELResolver#getCommonPropertyType(javax.el.ELContext, java.lang.Object)
     */
    @Override
    public Class<?> getCommonPropertyType ( ELContext ctx, Object base ) {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.el.ELResolver#getFeatureDescriptors(javax.el.ELContext, java.lang.Object)
     */
    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors ( ELContext ctx, Object base ) {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.el.ELResolver#getType(javax.el.ELContext, java.lang.Object, java.lang.Object)
     */
    @Override
    public Class<?> getType ( ELContext ctx, Object base, Object property ) {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.el.ELResolver#getValue(javax.el.ELContext, java.lang.Object, java.lang.Object)
     */
    @Override
    public Object getValue ( ELContext ctx, Object base, Object property ) {
        if ( base == null && "_".equals(property) ) { //$NON-NLS-1$
            log.debug("Start ResourceBundle resolver"); //$NON-NLS-1$
            ctx.setPropertyResolved(true);
            return new ResourceBundlePackage();
        }

        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.el.ELResolver#isReadOnly(javax.el.ELContext, java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean isReadOnly ( ELContext ctx, Object base, Object property ) {
        return true;
    }


    /**
     * @return the resourceBundleService
     */
    synchronized ResourceBundleService getResourceBundleService () {
        return this.resourceBundleService;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.el.ELResolver#setValue(javax.el.ELContext, java.lang.Object, java.lang.Object, java.lang.Object)
     */
    @Override
    public void setValue ( ELContext ctx, Object base, Object property, Object value ) {}

    class ResourceBundlePackage extends AbstractMap<String, ResourceBundle> {

        /**
         * {@inheritDoc}
         * 
         * @see java.util.Map#get(java.lang.Object)
         */
        @Override
        public ResourceBundle get ( Object key ) {
            return getResourceBundleService().getBundle((String) key, FacesContext.getCurrentInstance().getViewRoot().getLocale());
        }


        /**
         * {@inheritDoc}
         * 
         * @see java.util.AbstractMap#entrySet()
         */
        @Override
        public Set<java.util.Map.Entry<String, ResourceBundle>> entrySet () {
            return Collections.EMPTY_SET;
        }

    }

}
