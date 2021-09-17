/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.02.2015 by mbechler
 */
package eu.agno3.runtime.jsf.types.locale;


import java.util.Locale;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;


/**
 * @author mbechler
 *
 */
@Named ( "localeConverter" )
@ApplicationScoped
public class LocaleConverter implements Converter {

    /**
     * {@inheritDoc}
     *
     * @see javax.faces.convert.Converter#getAsObject(javax.faces.context.FacesContext,
     *      javax.faces.component.UIComponent, java.lang.String)
     */
    @Override
    public Object getAsObject ( FacesContext ctx, UIComponent comp, String str ) throws ConverterException {
        if ( StringUtils.isBlank(str) ) {
            return null;
        }
        return Locale.forLanguageTag(str);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.convert.Converter#getAsString(javax.faces.context.FacesContext,
     *      javax.faces.component.UIComponent, java.lang.Object)
     */
    @Override
    public String getAsString ( FacesContext ctx, UIComponent comp, Object obj ) throws ConverterException {
        if ( ! ( obj instanceof Locale ) ) {
            return null;
        }
        Locale l = (Locale) obj;
        return l.toLanguageTag();
    }

}
