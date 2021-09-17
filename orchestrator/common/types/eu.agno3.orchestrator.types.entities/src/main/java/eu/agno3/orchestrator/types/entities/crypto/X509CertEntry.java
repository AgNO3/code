/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.11.2014 by mbechler
 */
package eu.agno3.orchestrator.types.entities.crypto;


import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;

import org.apache.commons.codec.binary.Base64;
import org.hibernate.HibernateException;

import eu.agno3.orchestrator.types.DeduplicatedGlobal;
import eu.agno3.orchestrator.types.crypto.X509CertificateJPAConverter;


/**
 * @author mbechler
 *
 */
@Entity
@Table ( name = "config_crypto_certificates" )
@PersistenceUnit ( unitName = "config" )
public class X509CertEntry implements Serializable, DeduplicatedGlobal {

    /**
     * 
     */
    private static final long serialVersionUID = 703769162357429752L;
    private String certId;
    private X509Certificate certificateData;

    private Integer version = 1;


    /**
     * 
     */
    public X509CertEntry () {}


    /**
     * @param cert
     * 
     */
    public X509CertEntry ( X509Certificate cert ) {
        this.certificateData = cert;
        this.certId = deriveId(this.certificateData);
    }


    /**
     * @return the id
     */
    @Override
    @Id
    @Column ( name = "certId", length = 64 )
    public String getDerivedId () {
        return this.certId;
    }


    /**
     * @param id
     *            the id to set
     */
    public void setDerivedId ( String id ) {
        this.certId = id;
    }


    /**
     * @return the version
     */
    @Override
    public Integer getVersion () {
        return this.version;
    }


    /**
     * @param version
     *            the version to set
     */
    public void setVersion ( int version ) {
        this.version = version;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.types.DeduplicatedGlobal#replace(eu.agno3.orchestrator.types.DeduplicatedGlobal)
     */
    @Override
    public void replace ( DeduplicatedGlobal o ) {
        if ( o instanceof X509CertEntry ) {
            setCertificate( ( (X509CertEntry) o ).getCertificate());
        }
    }


    /**
     * @return the certificateData
     */
    @Lob
    @Column ( name = "data" )
    @Convert ( converter = X509CertificateJPAConverter.class )
    public X509Certificate getCertificate () {
        return this.certificateData;
    }


    /**
     * @param certificateData
     *            the certificateData to set
     */
    public void setCertificate ( X509Certificate certificateData ) {
        this.certificateData = certificateData;
        this.certId = deriveId(this.certificateData);
    }


    /**
     * @param cert
     * @return
     */
    private static String deriveId ( X509Certificate cert ) {
        try {
            MessageDigest dgst = MessageDigest.getInstance("SHA-256"); //$NON-NLS-1$

            dgst.update(cert.getSubjectX500Principal().getEncoded());

            if ( cert.getIssuerX500Principal() != null ) {
                dgst.update(cert.getIssuerX500Principal().getEncoded());
            }

            if ( cert.getSerialNumber() != null ) {
                dgst.update(cert.getSerialNumber().toByteArray());
            }

            return Base64.encodeBase64String(dgst.digest());
        }
        catch ( NoSuchAlgorithmException e ) {
            throw new HibernateException("Failed to generate certificate hash", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    // +GENERATED
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.certId == null ) ? 0 : this.certId.hashCode() );
        return result;
    }


    // -GENERATED

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    // +GENERATED
    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        X509CertEntry other = (X509CertEntry) obj;
        if ( this.certId == null ) {
            if ( other.certId != null )
                return false;
        }
        else if ( !this.certId.equals(other.certId) )
            return false;
        return true;
    }
    // -GENERATED

}
