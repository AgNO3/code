/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.10.2013 by mbechler
 */
package eu.agno3.runtime.i18n.internal;


import java.util.Locale;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.i18n.ResourceBundleService;


/**
 * @author mbechler
 * 
 */
@Component ( service = ResourceBundleService.class )
public class ResourceBundleServiceImpl implements ResourceBundleService {

    private Control resourceBundleControl;


    @Reference
    protected synchronized void setBundleControl ( Control c ) {
        this.resourceBundleControl = c;
    }


    protected synchronized void unsetBundleControl ( Control c ) {
        if ( this.resourceBundleControl == c ) {
            this.resourceBundleControl = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.i18n.ResourceBundleService#getControl()
     */
    @Override
    public Control getControl () {
        return this.resourceBundleControl;
    }


    @Override
    public ResourceBundle getBundle ( String baseName ) {
        return ResourceBundle.getBundle(baseName, this.getControl());
    }


    @Override
    public ResourceBundle getBundle ( String baseName, Locale locale ) {
        return ResourceBundle.getBundle(baseName, locale, this.getControl());
    }


    @Override
    public ResourceBundle getBundle ( String baseName, ClassLoader fallbackClassloader ) {
        return ResourceBundle.getBundle(baseName, Locale.getDefault(), fallbackClassloader, this.getControl());
    }


    @Override
    public ResourceBundle getBundle ( String baseName, Locale locale, ClassLoader fallbackClassloader ) {
        return ResourceBundle.getBundle(baseName, locale, fallbackClassloader, this.getControl());
    }

}
