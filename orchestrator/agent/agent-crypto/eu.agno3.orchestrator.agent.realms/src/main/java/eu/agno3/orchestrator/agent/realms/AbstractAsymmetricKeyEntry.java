/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.11.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.realms;


import java.io.Serializable;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Arrays;

import eu.agno3.runtime.crypto.keystore.KeyType;


/**
 * @author mbechler
 *
 */
public abstract class AbstractAsymmetricKeyEntry implements AsymmetricKeyStoreEntry, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6679221503037430783L;
    private String alias;
    private Certificate[] chain;
    private PublicKey publicKey;


    /**
     * 
     */
    public AbstractAsymmetricKeyEntry () {}


    /**
     * @param alias
     * @param pubKey
     * @param chain
     */
    public AbstractAsymmetricKeyEntry ( String alias, PublicKey pubKey, Certificate[] chain ) {
        super();
        this.alias = alias;
        this.publicKey = pubKey;
        if ( chain != null ) {
            this.chain = Arrays.copyOf(chain, chain.length);
        }
    }


    /**
     * @param alias
     *            the alias to set
     */
    public void setAlias ( String alias ) {
        this.alias = alias;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.KeyStoreEntry#getAlias()
     */
    @Override
    public String getAlias () {
        return this.alias;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.KeyStoreEntry#getType()
     */
    @Override
    public String getType () {
        return KeyType.getKeyType(this.getPublicKey()).name();
    }


    /**
     * @param publicKey
     *            the publicKey to set
     */
    public void setPublicKey ( PublicKey publicKey ) {
        this.publicKey = publicKey;
    }


    /**
     * 
     * @return the public key
     */
    @Override
    public PublicKey getPublicKey () {
        return this.publicKey;
    }


    /**
     * @param chain
     *            the chain to set
     */
    public void setChain ( Certificate[] chain ) {
        this.chain = chain;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.KeyStoreEntry#getCertificateChain()
     */
    @Override
    public Certificate[] getCertificateChain () {
        if ( this.chain != null ) {
            return Arrays.copyOf(this.chain, this.chain.length);
        }
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format(
            "%s: %s key, chain lenght %d", //$NON-NLS-1$
            this.getAlias(),
            this.getType(),
            this.chain == null ? 0 : this.chain.length);
    }
}
