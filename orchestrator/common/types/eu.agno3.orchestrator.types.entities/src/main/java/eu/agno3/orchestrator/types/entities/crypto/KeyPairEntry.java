/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.12.2014 by mbechler
 */
package eu.agno3.orchestrator.types.entities.crypto;


import java.io.Serializable;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
import eu.agno3.orchestrator.types.crypto.KeyPairJPAConverter;


/**
 * 
 * 
 * The id derivation here is potentially security relevant as if one
 * is able to produce a collision on the derived id he will be able to
 * access a private key.
 * 
 * @author mbechler
 *
 */
@Entity
@Table ( name = "config_crypto_keypairs" )
@PersistenceUnit ( unitName = "config" )
public class KeyPairEntry implements Serializable, DeduplicatedGlobal {

    /**
     * 
     */
    private static final long serialVersionUID = 6000495852576894135L;
    private String keyId;
    private KeyPair keyPair;

    private Integer version = 1;


    /**
     * 
     */
    public KeyPairEntry () {}


    /**
     * @param kp
     */
    public KeyPairEntry ( KeyPair kp ) {
        this.keyPair = kp;
        this.keyId = deriveId(kp);
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
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.types.DeduplicatedGlobal#replace(eu.agno3.orchestrator.types.DeduplicatedGlobal)
     */
    @Override
    public void replace ( DeduplicatedGlobal o ) {
        if ( o instanceof KeyPairEntry ) {
            setKeyPair( ( (KeyPairEntry) o ).getKeyPair());
        }
    }


    /**
     * @return the keyPair
     */
    @Lob
    @Convert ( converter = KeyPairJPAConverter.class )
    public KeyPair getKeyPair () {
        return this.keyPair;
    }


    /**
     * @param keyPair
     *            the keyPair to set
     */
    public void setKeyPair ( KeyPair keyPair ) {
        this.keyPair = keyPair;
        this.keyId = deriveId(this.keyPair);
    }


    /**
     * @param kp
     * @return
     */
    private static String deriveId ( KeyPair kp ) {
        try {
            MessageDigest dgst = MessageDigest.getInstance("SHA-512"); //$NON-NLS-1$
            dgst.update(kp.getPrivate().getEncoded());
            return Base64.encodeBase64String(dgst.digest());
        }
        catch ( NoSuchAlgorithmException e ) {
            throw new HibernateException("Failed to generate certificate hash", e); //$NON-NLS-1$
        }
    }

}
