/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.11.2014 by mbechler
 */
package eu.agno3.runtime.db.orm.converters;


import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.hibernate.type.descriptor.java.JavaTypeDescriptorRegistry;
import org.joda.time.Duration;
import org.osgi.service.component.annotations.Component;


/**
 * @author mbechler
 *
 */
@Component ( service = AttributeConverter.class, property = {
    "converts=org.joda.time.Duration"
} )
@Converter ( autoApply = true )
public class DurationJPAConverter implements AttributeConverter<Duration, Long> {

    static {
        // fix needed for equality checks
        JavaTypeDescriptorRegistry.INSTANCE.addDescriptor(DurationJavaTypeDescriptor.INSTANCE);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.persistence.AttributeConverter#convertToDatabaseColumn(java.lang.Object)
     */
    @Override
    public Long convertToDatabaseColumn ( Duration d ) {
        if ( d == null ) {
            return null;
        }
        return d.getMillis();
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.persistence.AttributeConverter#convertToEntityAttribute(java.lang.Object)
     */
    @Override
    public Duration convertToEntityAttribute ( Long ms ) {
        if ( ms == null ) {
            return null;
        }
        return new Duration((long) ms);
    }

}
