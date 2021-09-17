/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.12.2014 by mbechler
 */
package eu.agno3.orchestrator.types.entities.crypto;


import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

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
import eu.agno3.orchestrator.types.crypto.PublicKeyJPAConverter;


/**
 * 
 * @author mbechler
 *
 */
@Entity
@Table ( name = "config_crypto_public_keys" )
@PersistenceUnit ( unitName = "config" )
public class PublicKeyEntry implements Serializable, DeduplicatedGlobal {

    /**
     * 
     */
    private static final long serialVersionUID = 6000495852576894135L;
    private String keyId;
    private String comment;
    private PublicKey publicKey;
    private Integer version = 1;


    /**
     * 
     */
    public PublicKeyEntry () {}


    /**
     * 
     * @param pub
     */
    public PublicKeyEntry ( PublicKey pub ) {
        this(pub, null);
    }


    /**
     * @param pub
     * @param comment
     */
    public PublicKeyEntry ( PublicKey pub, String comment ) {
        this.publicKey = pub;
        this.keyId = deriveId(pub);
        this.comment = comment;
    }


    /**
     * @return the version
     */
    @Override
    @Column ( nullable = true )
    public Integer getVersion () {
        return this.version;
    }


    /**
     * @param version
     *            the version to set
     */
    public void setVersion ( Integer version ) {
        this.version = version;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.types.DeduplicatedGlobal#getDerivedId()
     */
    @Override
    @Id
    @Column ( name = "keyId", length = 128 )
    public String getDerivedId () {
        return this.keyId;
    }


    /**
     * 
     * @param derivedId
     */
    public void setDerivedId ( String derivedId ) {
        this.keyId = derivedId;
    }


    /**
     * @return the comment
     */
    @Column ( name = "comment", length = 128 )
    public String getComment () {
        return this.comment;
    }


    /**
     * @param comment
     *            the comment to set
     */
    public void setComment ( String comment ) {
        this.comment = comment;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.types.DeduplicatedGlobal#replace(eu.agno3.orchestrator.types.DeduplicatedGlobal)
     */
    @Override
    public void replace ( DeduplicatedGlobal o ) {
        if ( o instanceof PublicKeyEntry ) {
            setComment( ( (PublicKeyEntry) o ).getComment());
            setPublicKey( ( (PublicKeyEntry) o ).getPublicKey());
        }
    }


    /**
     * @return the publicKey
     */
    @Lob
    @Convert ( converter = PublicKeyJPAConverter.class )
    public PublicKey getPublicKey () {
        return this.publicKey;
    }


    /**
     * @param publicKey
     *            the publicKey to set
     */
    public void setPublicKey ( PublicKey publicKey ) {
        this.publicKey = publicKey;
    }


    /**
     * @param pub
     * @return
     */
    private static String deriveId ( PublicKey pub ) {
        try {
            MessageDigest dgst = MessageDigest.getInstance("SHA-512"); //$NON-NLS-1$
            dgst.update(pub.getEncoded());
            return Base64.encodeBase64String(dgst.digest());
        }
        catch ( NoSuchAlgorithmException e ) {
            throw new HibernateException("Failed to generate certificate hash", e); //$NON-NLS-1$
        }
    }

}
