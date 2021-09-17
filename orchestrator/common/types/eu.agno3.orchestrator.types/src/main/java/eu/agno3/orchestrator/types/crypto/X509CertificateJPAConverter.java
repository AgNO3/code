/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.11.2014 by mbechler
 */
package eu.agno3.orchestrator.types.crypto;


import java.security.cert.X509Certificate;
import java.sql.Blob;
import java.sql.SQLException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import javax.sql.rowset.serial.SerialBlob;

import org.hibernate.HibernateException;
import org.osgi.service.component.annotations.Component;


/**
 * @author mbechler
 *
 */
@Component ( service = AttributeConverter.class, property = {
    "converts=java.security.cert.X509Certificate"
} )
@Converter ( autoApply = true )
public class X509CertificateJPAConverter implements AttributeConverter<X509Certificate, Blob> {

    /**
     * {@inheritDoc}
     *
     * @see javax.persistence.AttributeConverter#convertToDatabaseColumn(java.lang.Object)
     */
    @Override
    public Blob convertToDatabaseColumn ( X509Certificate v ) {
        if ( v == null ) {
            return null;
        }

        try {
            return new SerialBlob(CryptoConvertUtil.convertX509CertificateToBytes(v));
        }
        catch ( SQLException e ) {
            throw new HibernateException("Failed to create blob", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.persistence.AttributeConverter#convertToEntityAttribute(java.lang.Object)
     */
    @Override
    public X509Certificate convertToEntityAttribute ( Blob v ) {
        if ( v == null ) {
            return null;
        }

        try {
            return CryptoConvertUtil.convertX509CertificateFromStream(v.getBinaryStream());
        }
        catch ( SQLException e ) {
            throw new HibernateException("Failed to read blob", e); //$NON-NLS-1$
        }

    }

}
