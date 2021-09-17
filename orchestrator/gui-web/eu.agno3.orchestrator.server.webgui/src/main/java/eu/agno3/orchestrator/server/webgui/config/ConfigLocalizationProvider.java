/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.08.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config;


import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;
import eu.agno3.orchestrator.server.webgui.CoreServiceProvider;
import eu.agno3.orchestrator.server.webgui.util.i18n.I18NUtil;


/**
 * @author mbechler
 * 
 */
@ApplicationScoped
@Named ( "configLocalizationProvider" )
public class ConfigLocalizationProvider {

    /**
     * 
     */
    private static final String FIELD_NAME_MISSING_FMT = "Field name localization not present for %s"; //$NON-NLS-1$
    private static final String FIELD_DESC_MISSING_FMT = "Field description localization not present for %s"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(ConfigLocalizationProvider.class);
    private static final String TYPE_PREFIX = "urn:agno3:objects:1.0:"; //$NON-NLS-1$
    private static final int CACHE_SIZE = 50;
    private static final int HIBERNATE_CACHE_SIZE = 10;

    @Inject
    private CoreServiceProvider csp;

    @Inject
    private ConfigServiceProvider confsp;

    private Map<ResourceBundleKey, ResourceBundle> bundleCache = new LRUMap<>(CACHE_SIZE);

    private Map<Locale, ResourceBundle> hibernateBundleCache = new LRUMap<>(HIBERNATE_CACHE_SIZE);


    /**
     * 
     * @param objectType
     * @return the translated object type name
     */
    public String getTypeName ( String objectType ) {
        String typeKey = makeTypeKey(objectType);
        if ( typeKey == null ) {
            return null;
        }
        try {
            ResourceBundle b = this.getBundleFor(objectType);
            return b.getString("type." + typeKey); //$NON-NLS-1$
        }
        catch (
            MissingResourceException |
            UndeclaredThrowableException e ) {
            log.trace("Type key not present", e); //$NON-NLS-1$
            log.warn("Type key not present " + typeKey); //$NON-NLS-1$
            return objectType;
        }
    }


    private static String makeTypeKey ( String objectType ) {
        if ( objectType == null ) {
            return null;
        }
        String typeKey = objectType.substring(TYPE_PREFIX.length());
        typeKey = typeKey.replace(':', '.');
        return typeKey;
    }


    /**
     * @param objectType
     * @return
     */
    private ResourceBundle getBundleFor ( String objectType ) {
        ResourceBundleKey cacheKey = new ResourceBundleKey(objectType, FacesContext.getCurrentInstance().getViewRoot().getLocale());
        ResourceBundle cached = this.bundleCache.get(cacheKey);
        if ( cached == null ) {

            ObjectTypeDescriptor<?> objectTypeDescriptor = null;
            try {
                objectTypeDescriptor = this.confsp.getObjectTypeRegistry().get(objectType);
            }
            catch ( ModelServiceException e ) {
                log.warn("Error looking up object type descriptor", e); //$NON-NLS-1$
            }

            if ( objectTypeDescriptor == null ) {
                throw new MissingResourceException(
                    "No object type descriptor found", //$NON-NLS-1$
                    ResourceBundle.class.getName(),
                    objectType);
            }

            if ( objectTypeDescriptor.getLocalizationBase() == null ) {
                throw new MissingResourceException(
                    "No base name found in object type descriptor", //$NON-NLS-1$
                    ResourceBundle.class.getName(),
                    objectType);
            }

            cached = this.csp.getLocalizationService()
                    .getBundle(objectTypeDescriptor.getLocalizationBase(), FacesContext.getCurrentInstance().getViewRoot().getLocale());
            this.bundleCache.put(cacheKey, cached);
        }

        return cached;
    }


    /**
     * 
     * @param objectType
     * @param field
     * @return the translated field name
     */
    public String getFieldName ( String objectType, String field ) {

        String typeKey = makeTypeKey(objectType);
        String fieldKey = typeKey + "." + field; //$NON-NLS-1$

        try {
            ResourceBundle b = this.getBundleFor(objectType);

            if ( !b.containsKey(fieldKey) ) {
                log.warn(String.format(FIELD_NAME_MISSING_FMT, fieldKey));
                return field;
            }

            return b.getString(fieldKey);
        }
        catch (
            MissingResourceException |
            UndeclaredThrowableException e ) {
            log.trace("Filed name missing", e); //$NON-NLS-1$
            log.warn(String.format(FIELD_NAME_MISSING_FMT, fieldKey));
            return field;
        }

    }


    /**
     * @param objectType
     * @param messageTemplate
     * @param messageArgs
     * @return the formatted violation message
     */
    public String getViolationMessage ( String objectType, String messageTemplate, List<String> messageArgs ) {

        if ( isHibernateTemplate(messageTemplate) ) {
            return handleHibernateValidationMessage(messageTemplate, messageArgs);
        }

        try {
            ResourceBundle b = this.getBundleFor(objectType);

            if ( !b.containsKey(messageTemplate) ) {
                log.warn(String.format("Violation message localization not present for %s in %s", messageTemplate, b.getBaseBundleName())); //$NON-NLS-1$
                return messageTemplate;
            }

            return I18NUtil.format(b, messageTemplate, messageArgs.toArray());
        }
        catch (
            MissingResourceException |
            UndeclaredThrowableException e ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Violation message localization not present for %s", messageTemplate), e); //$NON-NLS-1$
            }
            return messageTemplate;
        }
    }


    private static boolean isHibernateTemplate ( String messageTemplate ) {
        return messageTemplate.charAt(0) == '{' && messageTemplate.charAt(messageTemplate.length() - 1) == '}';
    }


    /**
     * @param messageTemplate
     * @param messageArgs
     * @return
     */
    private String handleHibernateValidationMessage ( String messageTemplate, List<String> messageArgs ) {
        String realKey = messageTemplate.substring(1, messageTemplate.length() - 1);

        Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        ResourceBundle b;
        synchronized ( this.hibernateBundleCache ) {
            if ( !this.hibernateBundleCache.containsKey(locale) ) {
                b = ResourceBundle.getBundle("org.hibernate.validator.ValidationMessages", locale, this.getClass().getClassLoader()); //$NON-NLS-1$
                this.hibernateBundleCache.put(locale, b);
            }
            else {
                b = this.hibernateBundleCache.get(locale);
            }
        }

        return I18NUtil.format(b, realKey, messageArgs);
    }


    /**
     * 
     * @param objectType
     * @param field
     * @return the translated field description
     */
    public String getFieldDescription ( String objectType, String field ) {
        String fieldDescriptionKey = makeDescriptionKey(objectType, field);
        try {
            ResourceBundle b = this.getBundleFor(objectType);

            if ( !b.containsKey(fieldDescriptionKey) ) {
                if ( log.isTraceEnabled() ) {
                    log.trace(String.format(FIELD_DESC_MISSING_FMT, fieldDescriptionKey));
                }
                return null;
            }

            return b.getString(fieldDescriptionKey);
        }
        catch (
            MissingResourceException |
            UndeclaredThrowableException e ) {
            if ( log.isTraceEnabled() ) {
                log.trace(String.format(FIELD_DESC_MISSING_FMT, fieldDescriptionKey), e);
            }
            return null;
        }
    }


    /**
     * 
     * @param objectType
     * @param field
     * @return whether a field description exists
     */
    public boolean hasFieldDescription ( String objectType, String field ) {
        String descKey = makeDescriptionKey(objectType, field);
        try {
            ResourceBundle b = this.getBundleFor(objectType);
            return b.containsKey(descKey);
        }
        catch (
            MissingResourceException |
            UndeclaredThrowableException e ) {
            log.debug(String.format(FIELD_DESC_MISSING_FMT, descKey), e);
            return false;
        }
    }


    private static String makeDescriptionKey ( String objectType, String field ) {
        StringBuilder b = new StringBuilder();
        b.append(makeTypeKey(objectType));
        b.append('.');
        b.append(field);
        b.append(".description"); //$NON-NLS-1$
        return b.toString();
    }

    private static class ResourceBundleKey {

        private String objectType;
        private Locale locale;


        /**
         * @param objectType
         * @param locale
         * 
         */
        public ResourceBundleKey ( String objectType, Locale locale ) {
            this.objectType = objectType;
            this.locale = locale;
        }


        // +GENERATED
        @Override
        public int hashCode () {
            final int prime = 31;
            int result = 1;
            result = prime * result + ( ( this.locale == null ) ? 0 : this.locale.hashCode() );
            result = prime * result + ( ( this.objectType == null ) ? 0 : this.objectType.hashCode() );
            return result;
        }

        // -GENERATED


        // +GENERATED
        @Override
        public boolean equals ( Object obj ) {
            if ( this == obj )
                return true;
            if ( obj == null )
                return false;
            if ( ! ( obj instanceof ResourceBundleKey ) )
                return false;
            ResourceBundleKey other = (ResourceBundleKey) obj;
            if ( this.locale == null ) {
                if ( other.locale != null )
                    return false;
            }
            else if ( !this.locale.equals(other.locale) )
                return false;
            if ( this.objectType == null ) {
                if ( other.objectType != null )
                    return false;
            }
            else if ( !this.objectType.equals(other.objectType) )
                return false;
            return true;
        }
        // -GENERATED

    }

}
