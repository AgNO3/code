/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.04.2014 by mbechler
 */
package eu.agno3.runtime.jsf.types.timezone;


import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import javax.inject.Named;

import org.joda.time.DateTimeZone;


/**
 * @author mbechler
 * 
 */
@Named ( "timeZoneConverter" )
@ApplicationScoped
@FacesConverter ( forClass = DateTimeZone.class )
public class TimeZoneConverter implements Converter {

    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.convert.Converter#getAsObject(javax.faces.context.FacesContext,
     *      javax.faces.component.UIComponent, java.lang.String)
     */
    @Override
    public Object getAsObject ( FacesContext ctx, UIComponent comp, String val ) {
        if ( val == null || val.isEmpty() ) {
            return null;
        }

        try {
            return DateTimeZone.forID(val);
        }
        catch ( IllegalArgumentException e ) {
            String msg = "Illegal timezone"; //$NON-NLS-1$
            throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg), e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.convert.Converter#getAsString(javax.faces.context.FacesContext,
     *      javax.faces.component.UIComponent, java.lang.Object)
     */
    @Override
    public String getAsString ( FacesContext ctx, UIComponent comp, Object obj ) {
        if ( ! ( obj instanceof DateTimeZone ) ) {
            return null;
        }

        return ( (DateTimeZone) obj ).getID();
    }

}
