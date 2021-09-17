/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.12.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config;


import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.UUID;

import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.service.ConfigurationService;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.CoreServiceProvider;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;


/**
 * @author mbechler
 * @param <T>
 * @param <TBase>
 *
 */
public abstract class AbstractBaseConfigObjectBean <T extends ConfigurationObject, TBase extends AbstractConfigurationObject<?>> {

    private static final Logger log = Logger.getLogger(AbstractBaseConfigObjectBean.class);

    @Inject
    protected ServerServiceProvider ssp;

    @Inject
    private CoreServiceProvider csp;


    protected abstract String getMessageBase ();


    protected abstract Class<T> getObjectType ();


    /**
     * 
     */
    public AbstractBaseConfigObjectBean () {
        super();
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
     * @param obj
     * @return the cloned object
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    @SuppressWarnings ( "unchecked" )
    public TBase cloneObject ( ConfigContext<?, ?> ctx, ConfigurationObject obj ) throws ModelServiceException, GuiWebServiceException {
        if ( !this.getObjectType().isAssignableFrom(obj.getType()) ) {
            throw new IllegalArgumentException("Not an " + this.getObjectType()); //$NON-NLS-1$
        }

        TBase cloned = (TBase) this.ssp.getService(ConfigurationService.class).getEmpty(ConfigUtil.getObjectTypeName(obj));
        UUID randomUUID = UUID.randomUUID();
        if ( randomUUID == null ) {
            throw new ModelServiceException();
        }
        cloned.setId(randomUUID);
        if ( obj.getVersion() != null ) {
            // persistent objects get cloned by inheriting
            cloned.setInherits(obj);
            return cloned;
        }
        this.cloneInternal(cloned, (T) obj);
        return cloned;
    }


    /**
     * @param cloned
     * @param obj
     */
    protected abstract void cloneInternal ( TBase cloned, T obj );


    public ResourceBundle getLocalizationBundle () {
        return this.csp.getLocalizationService().getBundle(getMessageBase(), FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }


    public <TEnum extends Enum<TEnum>> String translateEnumValue ( Class<TEnum> en, Object val ) {
        if ( val == null || !en.isAssignableFrom(val.getClass()) ) {
            return null;
        }
        @SuppressWarnings ( "unchecked" )
        TEnum enumVal = (TEnum) val;
        StringBuilder key = new StringBuilder();
        key.append(en.getSimpleName());
        key.append('.');
        key.append(enumVal.name());
        try {
            return this.getLocalizationBundle().getString(key.toString());
        }
        catch ( MissingResourceException e ) {
            log.debug("Missing resource", e); //$NON-NLS-1$
            return enumVal.name();
        }
    }


    public <TEnum extends Enum<TEnum>> String translateEnumDescription ( Class<TEnum> en, Object val ) {
        if ( val == null || !en.isAssignableFrom(val.getClass()) ) {
            return null;
        }
        @SuppressWarnings ( "unchecked" )
        TEnum enumVal = (TEnum) val;
        StringBuilder key = new StringBuilder();
        key.append(en.getSimpleName());
        key.append('.');
        key.append(enumVal.name());
        key.append(".description"); //$NON-NLS-1$
        try {
            return this.getLocalizationBundle().getString(key.toString());
        }
        catch ( MissingResourceException e ) {
            log.debug("Missing description resource", e); //$NON-NLS-1$
            return null;
        }
    }

}