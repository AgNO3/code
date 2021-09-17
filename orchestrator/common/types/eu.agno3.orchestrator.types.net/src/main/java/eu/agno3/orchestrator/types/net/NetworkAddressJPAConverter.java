/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.04.2014 by mbechler
 */
package eu.agno3.orchestrator.types.net;


import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.osgi.service.component.annotations.Component;


/**
 * @author mbechler
 * 
 */
@Component ( service = AttributeConverter.class, property = {
    "converts=eu.agno3.orchestrator.types.net.NetworkAddress"
} )
@Converter ( autoApply = true )
public class NetworkAddressJPAConverter implements AttributeConverter<NetworkAddress, String> {

    /**
     * {@inheritDoc}
     * 
     * @see javax.persistence.AttributeConverter#convertToDatabaseColumn(java.lang.Object)
     */
    @Override
    public String convertToDatabaseColumn ( NetworkAddress addr ) {
        if ( addr == null ) {
            return null;
        }
        return addr.getCanonicalForm();
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.persistence.AttributeConverter#convertToEntityAttribute(java.lang.Object)
     */
    @Override
    public NetworkAddress convertToEntityAttribute ( String s ) {
        if ( s == null ) {
            return null;
        }
        return AbstractIPAddress.parse(s);
    }

}
