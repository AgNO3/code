/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.11.2014 by mbechler
 */
package eu.agno3.runtime.jsf.components.localestr;


import java.util.Locale;
import java.util.Map;

import javax.faces.component.NamingContainer;
import javax.faces.component.UIInput;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.runtime.jsf.components.ResettableComponent;


/**
 * @author mbechler
 *
 */
public class LocalizedStringEditor extends UIInput implements NamingContainer, ResettableComponent {

    private static final Logger log = Logger.getLogger(LocalizedStringEditor.class);

    private static final String ADD_LOCALE = "locale"; //$NON-NLS-1$


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.components.ResettableComponent#resetComponent()
     */
    @Override
    public boolean resetComponent () {
        this.getStateHelper().remove(ADD_LOCALE);
        return true;
    }


    /**
     * 
     * @return the locale to add
     */
    public Locale getAddLocale () {
        return (Locale) getStateHelper().get(ADD_LOCALE);
    }


    /**
     * 
     * @param l
     */
    public void setAddLocale ( Locale l ) {
        getStateHelper().put(ADD_LOCALE, l);
    }


    /**
     * @param l
     * @return value mapper
     */
    @SuppressWarnings ( "unchecked" )
    public MapValueWrapper<?, ?> getValueWrapper ( Locale l ) {
        return new MapValueWrapper<>((Map<Object, Object>) getValue(), l);
    }


    /**
     * 
     * @param l
     */
    public void doRemove ( Locale l ) {
        @SuppressWarnings ( "unchecked" )
        Map<Locale, String> val = (Map<Locale, String>) getValue();
        if ( val == null ) {
            log.warn("Value is null"); //$NON-NLS-1$
            return;
        }

        val.remove(l);
    }


    /**
     * 
     */
    public void doAdd () {
        @SuppressWarnings ( "unchecked" )
        Map<Locale, String> val = (Map<Locale, String>) getValue();
        if ( val == null ) {
            log.warn("Value is null"); //$NON-NLS-1$
            return;
        }

        Locale l = getAddLocale();
        if ( log.isDebugEnabled() ) {
            log.debug("adding string for locale " + l); //$NON-NLS-1$
        }

        if ( l == null ) {
            l = Locale.ROOT;
        }

        val.put(l, StringUtils.EMPTY);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIInput#getFamily()
     */
    @Override
    public String getFamily () {
        return UINamingContainer.COMPONENT_FAMILY;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIInput#processValidators(javax.faces.context.FacesContext)
     */
    @Override
    public void processValidators ( FacesContext context ) {
        this.pushComponentToEL(context, this);
        try {
            super.processValidators(context);
        }
        finally {
            this.popComponentFromEL(context);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIInput#processUpdates(javax.faces.context.FacesContext)
     */
    @Override
    public void processUpdates ( FacesContext context ) {
        this.pushComponentToEL(context, this);
        try {
            super.processUpdates(context);
        }
        finally {
            this.popComponentFromEL(context);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIInput#updateModel(javax.faces.context.FacesContext)
     */
    @Override
    public void updateModel ( FacesContext context ) {
        super.updateModel(context);
    }
}
