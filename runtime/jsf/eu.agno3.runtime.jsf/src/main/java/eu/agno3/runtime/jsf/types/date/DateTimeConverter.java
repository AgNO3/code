/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.02.2015 by mbechler
 */
package eu.agno3.runtime.jsf.types.date;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.inject.Named;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.primefaces.component.calendar.Calendar;
import org.primefaces.component.calendar.CalendarUtils;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "dateTimeConverter" )
public class DateTimeConverter implements Converter {

    /**
     * {@inheritDoc}
     *
     * @see javax.faces.convert.Converter#getAsObject(javax.faces.context.FacesContext,
     *      javax.faces.component.UIComponent, java.lang.String)
     */
    @Override
    public Object getAsObject ( FacesContext ctx, UIComponent comp, String str ) throws ConverterException {
        Calendar cal = (Calendar) comp;
        SimpleDateFormat dateFormat = new SimpleDateFormat(cal.calculatePattern(), cal.calculateLocale(ctx));
        TimeZone tz = cal.calculateTimeZone();
        dateFormat.setTimeZone(tz);

        try {
            return new DateTime(dateFormat.parse(str), DateTimeZone.forTimeZone(tz));
        }
        catch ( ParseException e ) {
            throw new ConverterException("Failed to parse date", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.convert.Converter#getAsString(javax.faces.context.FacesContext,
     *      javax.faces.component.UIComponent, java.lang.Object)
     */
    @Override
    public String getAsString ( FacesContext ctx, UIComponent comp, Object obj ) throws ConverterException {

        if ( ! ( comp instanceof Calendar ) ) {
            return null;
        }

        Calendar cal = (Calendar) comp;

        if ( obj instanceof Date ) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(cal.calculatePattern(), cal.calculateLocale(ctx));
            dateFormat.setTimeZone(cal.calculateTimeZone());
            return dateFormat.format((Date) obj);
        }

        if ( ! ( obj instanceof DateTime ) ) {
            return null;
        }

        return CalendarUtils.getValueAsString(ctx, cal, ( (DateTime) obj ).toDate());
    }
}
