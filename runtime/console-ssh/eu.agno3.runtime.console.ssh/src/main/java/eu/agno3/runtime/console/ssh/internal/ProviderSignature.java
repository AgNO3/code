/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.11.2014 by mbechler
 */
package eu.agno3.runtime.console.ssh.internal;


import java.security.PrivateKey;
import java.security.PublicKey;

import org.apache.sshd.common.signature.AbstractSignature;
import org.apache.sshd.common.signature.Signature;


/**
 * @author mbechler
 *
 */
public class ProviderSignature extends AbstractSignature implements Signature {

    private java.security.Signature signature;


    /**
     * @param algorithm
     */
    public ProviderSignature ( String algorithm ) {
        super(algorithm);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.sshd.common.signature.AbstractSignature#getSignature()
     */
    @Override
    protected java.security.Signature getSignature () {
        return this.signature;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.sshd.common.signature.AbstractSignature#initSigner(java.security.PrivateKey)
     */
    @Override
    public void initSigner ( PrivateKey key ) throws Exception {
        this.signature = java.security.Signature.getInstance(this.getAlgorithm());
        this.signature.initSign(key);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.sshd.common.signature.AbstractSignature#initVerifier(java.security.PublicKey)
     */
    @Override
    public void initVerifier ( PublicKey key ) throws Exception {
        this.signature = java.security.Signature.getInstance(this.getAlgorithm());
        this.signature.initVerify(key);
    }


    @Override
    public boolean verify ( byte[] sig ) throws Exception {
        return this.signature.verify(extractEncodedSignature(sig).getValue());
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see org.apache.sshd.common.signature.Signature#sign()
     */
    @Override
    public byte[] sign () throws Exception {
        return this.signature.sign();
    }
}
