/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.11.2014 by mbechler
 */
package eu.agno3.orchestrator.types.crypto;


import java.security.PublicKey;
import java.sql.Blob;
import java.sql.SQLException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import javax.sql.rowset.serial.SerialBlob;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.osgi.service.component.annotations.Component;


/**
 * @author mbechler
 *
 */
@Component ( service = AttributeConverter.class, property = {
    "converts=java.security.interfaces.PublicKey"
} )
@Converter ( autoApply = true )
public class PublicKeyJPAConverter implements AttributeConverter<PublicKey, Blob> {

    private static final Logger log = Logger.getLogger(PublicKeyJPAConverter.class);


    /**
     * {@inheritDoc}
     *
     * @see javax.persistence.AttributeConverter#convertToDatabaseColumn(java.lang.Object)
     */
    @Override
    public Blob convertToDatabaseColumn ( PublicKey v ) {
        if ( v == null ) {
            return null;
        }

        try {
            return new SerialBlob(CryptoConvertUtil.convertPublicToBytes(v));
        }
        catch (
            SQLException |
            IllegalArgumentException e ) {
            throw new HibernateException("Failed to write RSA public key", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.persistence.AttributeConverter#convertToEntityAttribute(java.lang.Object)
     */
    @Override
    public PublicKey convertToEntityAttribute ( Blob v ) {
        if ( v == null ) {
            return null;
        }
        try {
            return CryptoConvertUtil.convertPublicFromStream(v.getBinaryStream());
        }
        catch (
            SQLException |
            IllegalArgumentException e ) {
            log.error("Failed to restore public key from database", e); //$NON-NLS-1$
            return null;
        }
    }
}
