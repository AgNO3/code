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
    "converts=eu.agno3.orchestrator.types.net.NetworkSpecification"
} )
@Converter ( autoApply = true )
public class NetworkSpecificationJPAConverter implements AttributeConverter<NetworkSpecification, String> {

    /**
     * {@inheritDoc}
     * 
     * @see javax.persistence.AttributeConverter#convertToDatabaseColumn(java.lang.Object)
     */
    @Override
    public String convertToDatabaseColumn ( NetworkSpecification addr ) {
        if ( addr == null || addr.getAddress() == null ) {
            return null;
        }

        return String.format("%s/%d", addr.getAddress().getCanonicalForm(), addr.getPrefixLength()); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.persistence.AttributeConverter#convertToEntityAttribute(java.lang.Object)
     */
    @Override
    public NetworkSpecification convertToEntityAttribute ( String s ) {
        if ( s == null ) {
            return null;
        }

        return NetworkSpecification.fromString(s, false);
    }

}
