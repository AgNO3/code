/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.04.2014 by mbechler
 */
package eu.agno3.orchestrator.types.timezone;


import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.joda.time.DateTimeZone;
import org.osgi.service.component.annotations.Component;


/**
 * @author mbechler
 * 
 */
@Component ( service = AttributeConverter.class, property = {
    "converts=org.joda.time.DateTimeZone"
} )
@Converter ( autoApply = true )
public class TimezoneJPAConverter implements AttributeConverter<DateTimeZone, String> {

    /**
     * {@inheritDoc}
     * 
     * @see javax.persistence.AttributeConverter#convertToEntityAttribute(java.lang.Object)
     */
    @Override
    public String convertToDatabaseColumn ( DateTimeZone tz ) {
        if ( tz == null ) {
            return null;
        }

        return tz.getID();
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.persistence.AttributeConverter#convertToEntityAttribute(java.lang.Object)
     */
    @Override
    public DateTimeZone convertToEntityAttribute ( String tz ) {
        if ( tz == null ) {
            return null;
        }

        return DateTimeZone.forID(tz);
    }

}
