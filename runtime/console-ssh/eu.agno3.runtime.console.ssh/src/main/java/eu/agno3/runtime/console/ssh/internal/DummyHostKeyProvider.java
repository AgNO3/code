/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.01.2014 by mbechler
 */
package eu.agno3.runtime.console.ssh.internal;


import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.sshd.common.keyprovider.AbstractKeyPairProvider;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;


class DummyHostKeyProvider extends AbstractKeyPairProvider {

    /**
     * 
     */
    private static final BouncyCastleProvider BC = new BouncyCastleProvider();

    @SuppressWarnings ( "hiding" )
    private static final Logger log = Logger.getLogger(DummyHostKeyProvider.class);

    /**
     * 
     */
    private static final String US_ASCII = "US-ASCII"; //$NON-NLS-1$

    private URL hostKeyUrl;


    /**
     * @param hostKeyUrl
     * 
     */
    public DummyHostKeyProvider ( URL hostKeyUrl ) {
        this.hostKeyUrl = hostKeyUrl;
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
            if ( this.hostKeyUrl == null ) {
                throw new KeyStoreException("Failed to open dummy hostkey"); //$NON-NLS-1$
            }

            KeyPair dummyKeyPair = parseKey(this.hostKeyUrl);
            keys.add(dummyKeyPair);
        }
        catch ( Exception e ) {
            log.error("Failed to load keys:", e); //$NON-NLS-1$
        }
        return keys;
    }


    /**
     * @param dummyKeyPair
     * @return
     * @throws KeyStoreException
     */
    protected KeyPair parseKey ( URL hostkey ) throws KeyStoreException {
        try ( InputStreamReader hostkeyReader = new InputStreamReader(hostkey.openStream(), Charset.forName(US_ASCII));
              PEMParser parser = new PEMParser(hostkeyReader) ) {
            Object obj = parser.readObject();
            if ( ! ( obj instanceof PEMKeyPair ) ) {
                throw new KeyStoreException("Failed to locate KeyPair in PEM file"); //$NON-NLS-1$
            }

            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(BC); // $NON-NLS-1$
            return converter.getKeyPair((PEMKeyPair) obj);

        }
        catch ( IOException e ) {
            throw new KeyStoreException("Failed to read hostkey:", e); //$NON-NLS-1$
        }
    }
}