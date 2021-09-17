/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.12.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config;


import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.service.ConfigurationService;
import eu.agno3.orchestrator.config.model.realm.service.DefaultsService;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.CoreServiceProvider;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.util.i18n.I18NUtil;


/**
 * @author mbechler
 * @param <T>
 * @param <TImpl>
 *
 */
public abstract class AbstractConfigObjectBean <T extends ConfigurationObject, TImpl extends AbstractConfigurationObject<T>> {

    private static final Logger log = Logger.getLogger(AbstractConfigObjectBean.class);

    @Inject
    protected ServerServiceProvider ssp;

    @Inject
    private CoreServiceProvider csp;


    protected abstract String getMessageBase ();


    protected abstract Class<T> getObjectType ();


    protected abstract Class<TImpl> getInstanceType ();


    protected abstract void cloneInternal ( ConfigContext<?, ?> ctx, TImpl cloned, T local, T defaults )
            throws ModelServiceException, GuiWebServiceException;


    /**
     * 
     */
    public AbstractConfigObjectBean () {
        super();
    }


    @SuppressWarnings ( "unchecked" )
    public T makeInstance () throws InstantiationException, IllegalAccessException {
        return (T) this.getInstanceType().newInstance();
    }


    public String labelFor ( Object obj ) {
        if ( obj == null || !this.getObjectType().isAssignableFrom(obj.getClass()) ) {
            return null;
        }

        @SuppressWarnings ( "unchecked" )
        String label = labelForInternal((T) obj);
        if ( label == null ) {
            return GuiMessages.get(GuiMessages.UNNAMED_CONFIG_OBJECT);
        }

        return label;
    }


    /**
     * @param obj
     * @return
     */
    protected abstract String labelForInternal ( T obj );


    /**
     * Clone the object
     * 
     * @param ctx
     * 
     * @param local
     * @return the cloned object
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    @SuppressWarnings ( "unchecked" )
    public TImpl cloneObject ( ConfigContext<?, ?> ctx, ConfigurationObject local ) throws ModelServiceException, GuiWebServiceException {

        if ( !this.getObjectType().isAssignableFrom(local.getType()) ) {
            throw new IllegalArgumentException("Not an " + this.getObjectType()); //$NON-NLS-1$
        }

        String objectTypeName = ConfigUtil.getObjectTypeName(local);
        TImpl cloned = (TImpl) this.ssp.getService(ConfigurationService.class).getEmpty(objectTypeName);

        T defaults;
        try {
            defaults = (T) this.ssp.getService(DefaultsService.class).getDefaultsFor(ctx.getAnchor(), objectTypeName, ctx.getObjectTypeName());
        }
        catch ( Exception e ) {
            throw new ModelServiceException("Failed to fetch defaults", e); //$NON-NLS-1$
        }

        UUID randomUUID = UUID.randomUUID();
        if ( randomUUID == null ) {
            throw new ModelServiceException();
        }
        cloned.setId(randomUUID);
        if ( local.getVersion() != null ) {
            // persistent objects get cloned by inheritance
            cloned.setInherits(local);
            return cloned;
        }
        this.cloneInternal(ctx, cloned, (T) local, defaults);
        return cloned;
    }


    public ResourceBundle getLocalizationBundle () {
        return this.csp.getLocalizationService().getBundle(getMessageBase(), FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }


    public <TEnum extends Enum<TEnum>> String translateEnumValue ( Class<TEnum> en, Object val ) {
        return I18NUtil.translateEnumValue(getLocalizationBundle(), en, val);
    }


    public <TEnum extends Enum<TEnum>> String translateEnumDescription ( Class<TEnum> en, Object val ) {
        return I18NUtil.translateEnumDescription(getLocalizationBundle(), en, val);
    }


    /**
     * @param ctx
     * @param cloned
     * @param old
     * @param def
     */
    public void cloneDefault ( ConfigContext<?, ?> ctx, TImpl cloned, T old, T def ) {

        if ( cloned.getInherits() != null ) {
            // if inheritance works that's just fine
            log.debug("Inherits is set"); //$NON-NLS-1$
            return;
        }

        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(this.getInstanceType());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for ( PropertyDescriptor desc : propertyDescriptors ) {
                Method m = desc.getReadMethod();

                if ( m == null || AbstractConfigurationObject.class.equals(m.getDeclaringClass()) || Object.class.equals(m.getDeclaringClass())
                        || m.getAnnotation(Transient.class) != null ) {
                    continue;
                }

                Method w = desc.getWriteMethod();
                if ( w != null ) {
                    Object val = m.invoke(old);
                    Object clonedVal = m.invoke(cloned);
                    if ( Objects.equals(val, m.invoke(def)) ) {
                        // is the default value
                        if ( log.isTraceEnabled() ) {
                            log.trace("Is the default value " + desc.getDisplayName()); //$NON-NLS-1$
                        }
                        continue;
                    }

                    if ( Collections.class.isAssignableFrom(m.getReturnType()) ) {
                        if ( clonedVal != null && ! ( (Collection<?>) clonedVal ).isEmpty() ) {
                            continue;
                        }

                        if ( List.class.isAssignableFrom(m.getReturnType()) ) {
                            w.invoke(cloned, new ArrayList<>((List<?>) val));
                        }
                        else if ( Set.class.isAssignableFrom(m.getReturnType()) ) {
                            w.invoke(cloned, new HashSet<>((Set<?>) val));
                        }
                    }
                    else if ( clonedVal == null && Map.class.isAssignableFrom(m.getReturnType()) ) {
                        w.invoke(cloned, new HashMap<>((Map<?, ?>) val));
                    }
                    else if ( clonedVal == null && ConfigurationObject.class.isAssignableFrom(m.getReturnType()) ) {
                        log.warn("Reference not cloned " + desc.getDisplayName()); //$NON-NLS-1$
                    }
                    else if ( clonedVal == null ) {
                        w.invoke(cloned, val);
                    }
                }
                else if ( log.isDebugEnabled() ) {
                    log.debug("Property not writable " + desc.getDisplayName()); //$NON-NLS-1$
                }
            }
        }
        catch ( Exception e ) {
            log.warn("Introspection failed", e); //$NON-NLS-1$
        }
    }

}