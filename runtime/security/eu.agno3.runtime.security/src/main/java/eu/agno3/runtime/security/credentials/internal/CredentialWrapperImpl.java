/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 16, 2017 by mbechler
 */
package eu.agno3.runtime.security.credentials.internal;


import java.io.IOException;
import java.security.PublicKey;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.wrap.CryptWrapper;
import eu.agno3.runtime.security.credentials.CredentialWrapper;
import eu.agno3.runtime.security.credentials.UnwrappedCredentials;
import eu.agno3.runtime.security.credentials.WrappedCredentials;


/**
 * @author mbechler
 *
 */
@Component ( service = CredentialWrapper.class )
public class CredentialWrapperImpl implements CredentialWrapper {

    private CryptWrapper cryptWrapper;


    /**
     * 
     */
    public CredentialWrapperImpl () {}


    /**
     * 
     * @param cw
     */
    public CredentialWrapperImpl ( CryptWrapper cw ) {
        this.cryptWrapper = cw;
    }


    @Reference
    protected synchronized void setCryptWrapper ( CryptWrapper cw ) {
        this.cryptWrapper = cw;
    }


    protected synchronized void unsetCryptWrapper ( CryptWrapper cw ) {
        if ( this.cryptWrapper == cw ) {
            this.cryptWrapper = null;
        }
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws IOException
     *
     * @see eu.agno3.runtime.security.credentials.CredentialWrapper#wrap(eu.agno3.runtime.security.credentials.UnwrappedCredentials,
     *      java.security.PublicKey[])
     */
    @Override
    public WrappedCredentials wrap ( UnwrappedCredentials creds, PublicKey... recipients ) throws CryptoException, IOException {
        return WrappedCredentials.fromCryptBlob(this.cryptWrapper.wrap(creds.encode(), recipients));
    }
}
