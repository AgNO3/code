/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.11.2014 by mbechler
 */
package eu.agno3.orchestrator.types.uri;


import java.net.URI;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.osgi.service.component.annotations.Component;


/**
 * @author mbechler
 *
 */
@Component ( service = AttributeConverter.class, property = {
    "converts=java.net.URI"
} )
@Converter ( autoApply = true )
public class UriJPAConverter implements AttributeConverter<URI, String> {

    /**
     * {@inheritDoc}
     *
     * @see javax.persistence.AttributeConverter#convertToDatabaseColumn(java.lang.Object)
     */
    @Override
    public String convertToDatabaseColumn ( URI u ) {

        if ( u == null ) {
            return null;
        }
        return u.toString();
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.persistence.AttributeConverter#convertToEntityAttribute(java.lang.Object)
     */
    @Override
    public URI convertToEntityAttribute ( String s ) {
        if ( s == null ) {
            return null;
        }
        return URI.create(s);
    }
}
