/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.11.2014 by mbechler
 */
package eu.agno3.runtime.console.ssh.internal;


import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.net.ssl.KeyManager;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509KeyManager;

import org.apache.log4j.Logger;
import org.apache.sshd.common.keyprovider.AbstractKeyPairProvider;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.KeyStoreConfiguration;


/**
 * @author mbechler
 *
 */
@Component ( service = KeyPairProvider.class, configurationPolicy = ConfigurationPolicy.REQUIRE, configurationPid = "console.ssh.keypair.provider" )
public class KeyStoreHostKeyProvider extends AbstractKeyPairProvider {

    @SuppressWarnings ( "hiding" )
    private static final Logger log = Logger.getLogger(KeyStoreHostKeyProvider.class);

    private KeyStoreConfiguration keyStoreConfig;
    private String alias;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.alias = (String) ctx.getProperties().get("keyAlias"); //$NON-NLS-1$
        if ( this.keyStoreConfig == null ) {
            log.error("Not properly set up"); //$NON-NLS-1$
        }
    }


    @Reference
    protected synchronized void setKeyStoreConfiguration ( KeyStoreConfiguration kc ) {
        this.keyStoreConfig = kc;
    }


    protected synchronized void unsetKeyStoreConfiguration ( KeyStoreConfiguration kc ) {
        if ( this.keyStoreConfig == kc ) {
            this.keyStoreConfig = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.sshd.common.keyprovider.AbstractKeyPairProvider#getKeyTypes()
     */
    @Override
    public List<String> getKeyTypes () {
        return Collections.singletonList(KeyPairProvider.SSH_RSA);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.sshd.common.keyprovider.AbstractKeyPairProvider#loadKey(java.lang.String)
     */
    @Override
    public KeyPair loadKey ( String type ) {

        if ( !KeyPairProvider.SSH_RSA.equals(type) ) {
            throw new UnsupportedOperationException();
        }

        Iterable<KeyPair> loadKeys = loadKeys();
        Iterator<KeyPair> iterator = loadKeys.iterator();
        if ( !iterator.hasNext() ) {
            throw new UnsupportedOperationException("No host key in " + this.keyStoreConfig.getId()); //$NON-NLS-1$
        }
        return iterator.next();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.sshd.common.keyprovider.AbstractKeyPairProvider#loadKeys()
     */
    @Override
    public Iterable<KeyPair> loadKeys () {
        Set<KeyPair> keys = new HashSet<>();
        try {
            for ( KeyManager km : this.keyStoreConfig.getKeyManagerFactory().getKeyManagers() ) {
                if ( ! ( km instanceof X509ExtendedKeyManager ) ) {
                    log.trace("Not a X509KeyManager"); //$NON-NLS-1$
                    continue;
                }
                X509KeyManager xkm = (X509KeyManager) km;

                for ( String a : xkm.getServerAliases("RSA", null) ) { //$NON-NLS-1$
                    X509Certificate[] certificateChain = xkm.getCertificateChain(a);

                    if ( this.alias != null && !a.endsWith(this.alias) ) {
                        continue;
                    }

                    if ( certificateChain == null || certificateChain.length == 0 ) {
                        log.warn("Certicate chain is empty " + a); //$NON-NLS-1$
                        continue;
                    }

                    PrivateKey privateKey = xkm.getPrivateKey(a);
                    PublicKey pubKey = certificateChain[ 0 ].getPublicKey();
                    keys.add(new KeyPair(pubKey, privateKey));
                }
            }
        }
        catch ( CryptoException e ) {
            log.error("Failed to get key manager", e); //$NON-NLS-1$
        }
        return keys;
    }
}
