/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.11.2014 by mbechler
 */
package eu.agno3.orchestrator.types.crypto;


import java.security.interfaces.RSAPrivateKey;
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
    "converts=java.security.interfaces.RSAPrivateKey"
} )
@Converter ( autoApply = true )
public class RSAPrivateKeyJPAConverter implements AttributeConverter<RSAPrivateKey, Blob> {

    private static final Logger log = Logger.getLogger(RSAPrivateKeyJPAConverter.class);


    /**
     * {@inheritDoc}
     *
     * @see javax.persistence.AttributeConverter#convertToDatabaseColumn(java.lang.Object)
     */
    @Override
    public Blob convertToDatabaseColumn ( RSAPrivateKey v ) {
        if ( v == null ) {
            return null;
        }
        try {
            return new SerialBlob(CryptoConvertUtil.convertRSAPrivateToBytes(v));
        }
        catch ( SQLException e ) {
            throw new HibernateException("Failed to write RSA privat key", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.persistence.AttributeConverter#convertToEntityAttribute(java.lang.Object)
     */
    @Override
    public RSAPrivateKey convertToEntityAttribute ( Blob v ) {
        if ( v == null ) {
            return null;
        }
        try {
            return CryptoConvertUtil.convertRSAPrivateFromStream(v.getBinaryStream());
        }
        catch ( SQLException e ) {
            log.error("Failed to restore RSA private key from database", e); //$NON-NLS-1$
            return null;
        }
    }
}
