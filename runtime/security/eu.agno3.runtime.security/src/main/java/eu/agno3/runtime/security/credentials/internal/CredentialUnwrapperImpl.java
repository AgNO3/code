/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 16, 2017 by mbechler
 */
package eu.agno3.runtime.security.credentials.internal;


import java.io.IOException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.wrap.CryptUnwrapper;
import eu.agno3.runtime.security.credentials.CredentialType;
import eu.agno3.runtime.security.credentials.CredentialUnwrapper;
import eu.agno3.runtime.security.credentials.UnwrappedCredentials;
import eu.agno3.runtime.security.credentials.UsernamePasswordCredential;
import eu.agno3.runtime.security.credentials.WrappedCredentials;


/**
 * @author mbechler
 *
 */
@Component ( service = CredentialUnwrapper.class, configurationPid = "credentialUnwrap", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class CredentialUnwrapperImpl implements CredentialUnwrapper {

    private CryptUnwrapper cryptUnwrapper;


    /**
     * 
     */
    public CredentialUnwrapperImpl () {}


    /**
     * 
     * @param cu
     */
    public CredentialUnwrapperImpl ( CryptUnwrapper cu ) {
        this.cryptUnwrapper = cu;
    }


    @Reference
    protected synchronized void setCryptUnwrapper ( CryptUnwrapper cu ) {
        this.cryptUnwrapper = cu;
    }


    protected synchronized void unsetCryptUnwrapper ( CryptUnwrapper cu ) {
        if ( this.cryptUnwrapper == cu ) {
            this.cryptUnwrapper = null;
        }
    }


    @Override
    public UnwrappedCredentials unwrap ( WrappedCredentials cr ) throws CryptoException, IOException {
        byte[] unwrapped = this.cryptUnwrapper.unwrap(cr.toCryptBlob());

        if ( unwrapped == null || unwrapped.length < 1 ) {
            throw new CryptoException("Invalid wrapped credentials"); //$NON-NLS-1$
        }

        int type = unwrapped[ 0 ] & 0xFF;
        CredentialType[] types = CredentialType.values();

        if ( type < 0 || type >= types.length ) {
            throw new CryptoException("Unsupported credential type " + type); //$NON-NLS-1$
        }

        CredentialType ct = types[ type ];
        return decode(ct, unwrapped);
    }


    /**
     * @param ct
     * @param unwrapped
     * @return
     * @throws CryptoException
     * @throws IOException
     */
    private static UnwrappedCredentials decode ( CredentialType ct, byte[] unwrapped ) throws CryptoException, IOException {
        switch ( ct ) {
        case USERNAME_PASSWORD:
            return UsernamePasswordCredential.decode(unwrapped);
        default:
            throw new CryptoException("Unhandled credential type " + ct); //$NON-NLS-1$
        }
    }
}
