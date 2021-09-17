/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.10.2014 by mbechler
 */
package eu.agno3.runtime.db.orm.converters;


import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.log4j.Logger;
import org.hibernate.type.descriptor.java.JavaTypeDescriptorRegistry;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.chrono.GJChronology;
import org.osgi.service.component.annotations.Component;


/**
 * @author mbechler
 *
 */
@Component ( service = AttributeConverter.class, property = {
    "converts=org.joda.time.DateTime"
} )
@Converter ( autoApply = true )
public class DateTimeJPAConverter implements AttributeConverter<DateTime, Calendar> {

    private static final Logger log = Logger.getLogger(DateTimeJPAConverter.class);


    static {
        // fix needed for equality checks
        JavaTypeDescriptorRegistry.INSTANCE.addDescriptor(DateTimeJavaTypeDescriptor.INSTANCE);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.persistence.AttributeConverter#convertToDatabaseColumn(java.lang.Object)
     */
    @Override
    public Calendar convertToDatabaseColumn ( DateTime date ) {
        if ( date == null ) {
            return null;
        }

        GJChronology chronology = GJChronology.getInstance(DateTimeZone.UTC);
        GregorianCalendar gc = date.withZone(DateTimeZone.UTC).toDateTime(chronology).toGregorianCalendar();

        if ( log.isDebugEnabled() ) {
            log.debug(String.format(
                "Converted to db %d (%s - %s) -> %d (%s - %s)", //$NON-NLS-1$
                date.getMillis(),
                date.getZone().getID(),
                date.toString(),
                gc.getTimeInMillis(),
                gc.getTimeZone().getID(),
                gc.toString()));
        }

        return gc;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.persistence.AttributeConverter#convertToEntityAttribute(java.lang.Object)
     */
    @Override
    public DateTime convertToEntityAttribute ( Calendar date ) {
        if ( date == null ) {
            return null;
        }
        GJChronology chronology = GJChronology.getInstance(DateTimeZone.forTimeZone(date.getTimeZone()));
        DateTime dt = new DateTime(date, chronology).withZone(DateTimeZone.UTC);

        if ( log.isDebugEnabled() ) {
            log.debug(String.format(
                "Converted from db %d (%s - %s) -> %d (%s - %s)", //$NON-NLS-1$
                date.getTimeInMillis(),
                date.getTimeZone().getID(),
                date.toString(),
                dt.getMillis(),
                dt.getZone().getID(),
                dt.toString()));
        }
        return dt;
    }

}
