/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.10.2013 by mbechler
 */
package eu.agno3.runtime.validation.internal;


import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;

import eu.agno3.runtime.i18n.ResourceBundleService;


/**
 * @author mbechler
 * 
 */
public class OSGIResourceBundleLocator implements ResourceBundleLocator {

    private static final Logger log = Logger.getLogger(OSGIResourceBundleLocator.class);
    private String baseName;
    private ResourceBundleService resourceBundleService;


    /**
     * @param service
     * @param baseName
     */
    public OSGIResourceBundleLocator ( ResourceBundleService service, String baseName ) {
        this.baseName = baseName;
        this.resourceBundleService = service;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.hibernate.validator.spi.resourceloading.ResourceBundleLocator#getResourceBundle(java.util.Locale)
     */
    @Override
    public ResourceBundle getResourceBundle ( Locale locale ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Looking up resource bundle %s for locale %s", this.baseName, locale.toString())); //$NON-NLS-1$
        }
        return this.resourceBundleService.getBundle(this.baseName, locale);
    }
}
