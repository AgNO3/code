/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 19, 2017 by mbechler
 */
package eu.agno3.runtime.crypto.openssl;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.bc.BcPEMDecryptorProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;


/**
 * @author mbechler
 *
 */
public class PEMLoader {

    private static final Logger log = Logger.getLogger(PEMLoader.class);
    private static final BouncyCastleProvider PROVIDER = new BouncyCastleProvider();

    private final List<PrivateKey> privateKeys = new ArrayList<>();
    private final List<X509Certificate> certificates = new ArrayList<>();


    private PEMLoader () {}


    /**
     * @return the certificates
     */
    public List<? extends Certificate> getCertificates () {
        return Collections.unmodifiableList(this.certificates);
    }


    /**
     * @return the privateKeys
     */
    public List<PrivateKey> getPrivateKeys () {
        return Collections.unmodifiableList(this.privateKeys);
    }


    /**
     * 
     * @return the only private key
     * @throws KeyStoreException
     */
    public PrivateKey getPrivateKey () throws KeyStoreException {
        if ( this.privateKeys.isEmpty() ) {
            throw new KeyStoreException("Did not find any private key"); //$NON-NLS-1$
        }
        if ( this.privateKeys.size() != 1 ) {
            throw new KeyStoreException("Found multiple private keys"); //$NON-NLS-1$
        }
        return this.privateKeys.get(0);
    }


    /**
     * 
     * @param fc
     * @param password
     *            for decrypting encrypted keys, optional
     * @return loaded certificate objects
     * @throws IOException
     * @throws CertificateException
     */
    public static PEMLoader load ( FileChannel fc, String password ) throws IOException, CertificateException {
        try ( InputStream is = Channels.newInputStream(fc) ) {
            return load(is, password);
        }
    }


    /**
     * @param is
     * @param password
     *            for decrypting encrypted keys, optional
     * @return loaded certificate objects
     * @throws IOException
     * @throws CertificateException
     */
    public static PEMLoader load ( InputStream is, String password ) throws IOException, CertificateException {
        try ( InputStreamReader r = new InputStreamReader(is, StandardCharsets.US_ASCII) ) {
            return load(r, password);
        }
    }


    /**
     * @param r
     * @param password
     *            for decrypting encrypted keys, optional
     * @return loaded certificate objects
     * @throws IOException
     * @throws CertificateException
     */
    public static PEMLoader load ( Reader r, String password ) throws IOException, CertificateException {
        PEMLoader data = new PEMLoader();
        try ( PEMParser parse = new PEMParser(r) ) {
            Object obj;
            while ( ( obj = parse.readObject() ) != null ) {
                if ( obj instanceof PEMKeyPair ) {
                    data.privateKeys.add(new JcaPEMKeyConverter().setProvider(PROVIDER).getKeyPair((PEMKeyPair) obj).getPrivate());
                }
                else if ( obj instanceof PrivateKeyInfo ) {
                    data.privateKeys.add(new JcaPEMKeyConverter().setProvider(PROVIDER).getPrivateKey((PrivateKeyInfo) obj));
                }
                else if ( obj instanceof PEMEncryptedKeyPair ) {
                    if ( password == null ) {
                        throw new IOException("Contains encrypted key but no password was specified"); //$NON-NLS-1$
                    }
                    log.debug("Have encrypted keypair"); //$NON-NLS-1$
                    PEMEncryptedKeyPair epki = (PEMEncryptedKeyPair) obj;
                    PEMDecryptorProvider dec = new BcPEMDecryptorProvider(password.toCharArray());
                    data.privateKeys.add(new JcaPEMKeyConverter().setProvider(PROVIDER).getKeyPair(epki.decryptKeyPair(dec)).getPrivate());
                }
                else if ( obj instanceof X509CertificateHolder ) {
                    data.certificates.add(new JcaX509CertificateConverter().getCertificate((X509CertificateHolder) obj));
                }
                else {
                    log.warn("Unhandled PEM object type " + obj.getClass().getName()); //$NON-NLS-1$
                }
            }
            return data;
        }
    }

}
