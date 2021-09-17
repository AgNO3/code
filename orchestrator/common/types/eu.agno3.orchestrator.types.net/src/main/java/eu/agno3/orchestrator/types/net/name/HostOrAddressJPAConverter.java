/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.04.2014 by mbechler
 */
package eu.agno3.orchestrator.types.net.name;


import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.osgi.service.component.annotations.Component;


/**
 * @author mbechler
 * 
 */
@Component ( service = AttributeConverter.class, property = {
    "converts=eu.agno3.orchestrator.types.net.HostOrAddress"
} )
@Converter ( autoApply = true )
public class HostOrAddressJPAConverter implements AttributeConverter<HostOrAddress, String> {

    /**
     * {@inheritDoc}
     * 
     * @see javax.persistence.AttributeConverter#convertToDatabaseColumn(java.lang.Object)
     */
    @Override
    public HostOrAddress convertToEntityAttribute ( String data ) {
        if ( data == null ) {
            return null;
        }
        return HostOrAddress.fromString(data);
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.persistence.AttributeConverter#convertToEntityAttribute(java.lang.Object)
     */
    @Override
    public String convertToDatabaseColumn ( HostOrAddress obj ) {
        if ( obj == null ) {
            return null;
        }
        return obj.toString();
    }

}
