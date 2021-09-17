/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.11.2013 by mbechler
 */
package eu.agno3.runtime.jsf.types.uuid;


import java.util.UUID;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.runtime.jsf.i18n.BaseMessages;


/**
 * @author mbechler
 * 
 */
public class UUIDConverter implements javax.faces.convert.Converter {

    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.convert.Converter#getAsObject(javax.faces.context.FacesContext,
     *      javax.faces.component.UIComponent, java.lang.String)
     */
    @Override
    public Object getAsObject ( FacesContext context, UIComponent component, String value ) {

        if ( value == null ) {
            return null;
        }

        try {
            return UUID.fromString(value);
        }
        catch ( IllegalArgumentException e ) {
            throw new ConverterException(
                new FacesMessage(FacesMessage.SEVERITY_ERROR, BaseMessages.get("uuidConverter.invalid"), StringUtils.EMPTY), //$NON-NLS-1$
                e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.convert.Converter#getAsString(javax.faces.context.FacesContext,
     *      javax.faces.component.UIComponent, java.lang.Object)
     */
    @Override
    public String getAsString ( FacesContext context, UIComponent component, Object value ) {

        if ( ! ( value instanceof UUID ) ) {
            return null;
        }

        return value.toString();
    }

}
