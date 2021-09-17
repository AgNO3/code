/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.11.2014 by mbechler
 */
package eu.agno3.runtime.console.ssh.internal;


import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.signature.Signature;


/**
 * @author mbechler
 *
 */
public class RSAProviderSignatureFactory implements NamedFactory<Signature> {

    /**
     * {@inheritDoc}
     *
     * @see org.apache.sshd.common.Factory#create()
     */
    @Override
    public Signature create () {
        return new ProviderSignature("SHA1withRSA"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.sshd.common.NamedFactory#getName()
     */
    @Override
    public String getName () {
        return "ssh-rsa"; //$NON-NLS-1$
    }
}
