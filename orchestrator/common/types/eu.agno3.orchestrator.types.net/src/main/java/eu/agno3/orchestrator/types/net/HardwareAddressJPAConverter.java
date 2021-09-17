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
    "converts=eu.agno3.orchestrator.types.net.HardwareAddress"
} )
@Converter ( autoApply = true )
public class HardwareAddressJPAConverter implements AttributeConverter<HardwareAddress, String> {

    /**
     * {@inheritDoc}
     * 
     * @see javax.persistence.AttributeConverter#convertToDatabaseColumn(java.lang.Object)
     */
    @Override
    public String convertToDatabaseColumn ( HardwareAddress addr ) {
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
    public HardwareAddress convertToEntityAttribute ( String s ) {
        if ( s == null ) {
            return null;
        }
        MACAddress mac = new MACAddress();
        mac.fromString(s);
        return mac;
    }

}
