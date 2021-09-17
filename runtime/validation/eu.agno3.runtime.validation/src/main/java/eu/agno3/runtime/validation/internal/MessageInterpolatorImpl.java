/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.10.2013 by mbechler
 */
package eu.agno3.runtime.validation.internal;


import java.util.Locale;

import javax.validation.MessageInterpolator;

import org.apache.log4j.Logger;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.i18n.ResourceBundleService;


/**
 * @author mbechler
 * 
 */
@Component ( service = MessageInterpolator.class )
public class MessageInterpolatorImpl implements MessageInterpolator {

    private static final String END_TEMPLATE = "}"; //$NON-NLS-1$
    private static final String START_TEMPLATE = "{"; //$NON-NLS-1$
    private static final String HIBERNATE_BUILTIN_PACKAGE = "org.hibernate.validator.constraints"; //$NON-NLS-1$
    private static final String BEANVAL_BUILTIN_PACKAGE = "javax.validation"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(MessageInterpolatorImpl.class);
    private MessageInterpolator builtinDelegate;
    private ResourceBundleService resourceBundleService;


    @Activate
    protected void activate ( ComponentContext context ) {
        this.builtinDelegate = new ResourceBundleMessageInterpolator();
    }


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
     * @see javax.validation.MessageInterpolator#interpolate(java.lang.String,
     *      javax.validation.MessageInterpolator.Context)
     */
    @Override
    public String interpolate ( String template, Context context ) {
        String packageName = getPackageName(template, context);
        MessageInterpolator delegate = determineInterpolator(packageName);
        String actualTemplate = fixTemplate(template);

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Interpolate '%s' in default locale for package %s", template, packageName)); //$NON-NLS-1$
        }

        return delegate.interpolate(actualTemplate, context);
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.MessageInterpolator#interpolate(java.lang.String,
     *      javax.validation.MessageInterpolator.Context, java.util.Locale)
     */
    @Override
    public String interpolate ( String template, Context context, Locale locale ) {
        String packageName = getPackageName(template, context);
        MessageInterpolator delegate = determineInterpolator(packageName);
        String actualTemplate = fixTemplate(template);

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Interpolate '%s' in locale %s for package %s", template, locale, packageName)); //$NON-NLS-1$
        }

        return delegate.interpolate(actualTemplate, context, locale);
    }


    private String getPackageName ( String template, Context context ) {
        String packageName = null;

        int colonIndex = template.indexOf(':');
        if ( colonIndex > 0 && template.startsWith(START_TEMPLATE) && template.endsWith(END_TEMPLATE) ) {
            packageName = template.substring(1, colonIndex);
        }
        else {
            packageName = getPackageName(context);
        }
        return packageName;
    }


    private MessageInterpolator determineInterpolator ( String packageName ) {
        MessageInterpolator delegate = null;
        if ( isBuiltinPackage(packageName) ) {
            delegate = this.builtinDelegate;
        }
        else {
            delegate = getInterpolator(packageName);
        }
        return delegate;
    }


    private static String fixTemplate ( String template ) {
        int colonIndex = template.indexOf(':');
        String actualTemplate = null;
        if ( colonIndex > 0 && template.startsWith(START_TEMPLATE) && template.endsWith(END_TEMPLATE) ) {
            actualTemplate = START_TEMPLATE + template.substring(colonIndex + 1);
        }
        else {
            actualTemplate = template;
        }
        return actualTemplate;
    }


    /**
     * @param packageName
     * @return
     */
    protected ResourceBundleMessageInterpolator getInterpolator ( String packageName ) {
        // TODO: cache interpolators?
        return new ResourceBundleMessageInterpolator(new OSGIResourceBundleLocator(this.resourceBundleService, packageName));
    }


    /**
     * @param context
     * @return
     */
    protected String getPackageName ( Context context ) {
        return context.getConstraintDescriptor().getAnnotation().annotationType().getPackage().getName();
    }


    /**
     * @param packageName
     * @return
     */
    protected boolean isBuiltinPackage ( String packageName ) {
        return packageName.startsWith(BEANVAL_BUILTIN_PACKAGE) || packageName.startsWith(HIBERNATE_BUILTIN_PACKAGE);
    }

}
