/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.01.2015 by mbechler
 */
package eu.agno3.fileshare.security.internal;


import java.io.IOException;
import java.security.SecureRandom;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.crypto.random.SecureRandomProvider;
import eu.agno3.runtime.crypto.secret.ConfigSecretKeyProvider;
import eu.agno3.runtime.crypto.secret.SecretKeyProvider;
import eu.agno3.runtime.security.web.login.token.TokenGenerator;
import eu.agno3.runtime.security.web.login.token.crypto.CryptoTokenValidator;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    TokenGenerator.class
}, configurationPid = FileshareCryptoTokenValidator.PID )
public class FileshareCryptoTokenValidator extends CryptoTokenValidator {

    /**
     * 
     */
    public static final String PID = "crypto.token"; //$NON-NLS-1$


    /**
     * 
     */
    public FileshareCryptoTokenValidator () {
        super(FileshareCryptoTokenValidator.class.getClassLoader(), null);
        this.setRealm("FILE"); //$NON-NLS-1$
    }

    private SecureRandom random;
    private SecureRandomProvider randProv;
    private SecretKeyProvider keyProvider;


    @Reference
    protected synchronized void setSecureRandomProvider ( SecureRandomProvider srp ) {
        this.randProv = srp;
    }


    protected synchronized void unsetSecureRandomProvider ( SecureRandomProvider srp ) {
        if ( this.randProv == srp ) {
            this.randProv = null;
        }
    }


    @Reference
    protected synchronized void setSecretKeyProvider ( SecretKeyProvider skp ) {
        this.keyProvider = skp;
    }


    protected synchronized void unsetSecretKeyProvider ( SecretKeyProvider skp ) {
        if ( this.keyProvider == skp ) {
            this.keyProvider = null;
        }
    }


    /**
     * @return the random
     */
    public SecureRandom getRandom () {
        if ( this.random == null ) {
            this.random = this.randProv.getSecureRandom();
        }
        return this.random;
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) throws IOException {
        setRandom(getRandom());
        int keySize = 16;
        String keySizeSpec = (String) ctx.getProperties().get("keySize"); //$NON-NLS-1$

        if ( !StringUtils.isBlank(keySizeSpec) ) {
            keySize = Integer.parseInt(keySizeSpec);
        }

        setKeyid("FS.token"); //$NON-NLS-1$
        setKeySize(keySize);

        SecretKeyProvider kp = ConfigSecretKeyProvider.create(ctx.getProperties(), "tokenKey"); //$NON-NLS-1$
        if ( kp != null ) {
            setKeyProvider(kp);
        }
        else {
            setKeyProvider(this.keyProvider);
        }

    }
}
