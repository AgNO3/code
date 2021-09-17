/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.10.2016 by mbechler
 */
package eu.agno3.runtime.crypto.pubkey;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.ssh.OpenSSHKeyCodec;


/**
 * @author mbechler
 *
 */
public final class PublicKeyParser {

    private static final Logger log = Logger.getLogger(PublicKeyParser.class);


    /**
     * Parses public keys from in different formats
     * 
     * @param is
     * @return public keys found in the input
     * @throws CryptoException
     */
    public static final PublicKeyEntry parsePublicKeys ( InputStream is ) throws CryptoException {
        boolean foundAny = false;
        PublicKeyEntry found = null;
        is.mark(1024);
        try ( InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
              BufferedReader br = new BufferedReader(isr) ) {
            String line;
            while ( ( line = br.readLine() ) != null ) {
                PublicKeyEntry parsed = null;
                if ( !foundAny && "-----BEGIN RSA PUBLIC KEY-----".equals(line.trim()) ) { //$NON-NLS-1$
                    // PEM encapsulated PublicKey, PKCS1
                    log.debug("Parsing as PKCS1"); //$NON-NLS-1$
                    foundAny = true;
                    parsed = parsePKCS1(br);
                }
                else if ( !foundAny && "-----BEGIN PUBLIC KEY-----".equals(line.trim()) ) { //$NON-NLS-1$
                    // PEM encapsulated PublicKeyInfo, PKCS8
                    log.debug("Parsing as PKCS8"); //$NON-NLS-1$
                    foundAny = true;
                    parsed = parseSubjectPublicKeyInfo(br);

                }
                else if ( !foundAny && "-----BEGIN CERTIFICATE-----".equals(line.trim()) ) { //$NON-NLS-1$
                    // X509 certificate
                    log.debug("Parsing as X509"); //$NON-NLS-1$
                    foundAny = true;
                    parsed = parseX509(is);
                }
                else if ( !foundAny && "---- BEGIN SSH2 PUBLIC KEY ----".equals(line.trim()) ) { //$NON-NLS-1$
                    log.debug("Parsing as SSH2"); //$NON-NLS-1$
                    foundAny = true;
                    parsed = parseRFC4716(br);
                }
                else {
                    log.debug("Parsing as OpenSSH"); //$NON-NLS-1$
                    parsed = parseOpenSSH(line);
                    if ( foundAny && parsed == null ) {
                        throw new CryptoException("Invalid data in public key input"); //$NON-NLS-1$
                    }
                }
                if ( parsed != null ) {
                    foundAny = true;
                    found = parsed;
                }
            }
        }
        catch ( IOException e ) {
            throw new CryptoException("Failed to read public keys", e); //$NON-NLS-1$
        }

        if ( !foundAny ) {
            throw new CryptoException("No public keys found, unsupported format?"); //$NON-NLS-1$
        }

        return found;
    }


    /**
     * @param is
     * @param found
     * @return
     * @throws CryptoException
     * @throws IOException
     */
    private static PublicKeyEntry parseSubjectPublicKeyInfo ( BufferedReader br ) throws IOException, CryptoException {
        X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.decodeBase64(decodeASCIIArmor(br, "-----END PUBLIC KEY-----"))); //$NON-NLS-1$
        try {
            KeyFactory f = KeyFactory.getInstance("RSA"); //$NON-NLS-1$
            return new PublicKeyEntry(f.generatePublic(spec));
        }
        catch ( Exception e ) {
            throw new CryptoException("Invalid key in input", e); //$NON-NLS-1$
        }
    }


    /**
     * @param is
     * @param found
     * @throws CryptoException
     * @throws IOException
     */
    private static PublicKeyEntry parsePKCS1 ( BufferedReader br ) throws IOException, CryptoException {
        byte[] data = Base64.decodeBase64(decodeASCIIArmor(br, "-----END RSA PUBLIC KEY-----")); //$NON-NLS-1$
        try ( ASN1InputStream is = new ASN1InputStream(new ByteArrayInputStream(data)) ) {
            ASN1Primitive r = is.readObject();
            if ( ! ( r instanceof ASN1Sequence ) ) {
                throw new CryptoException("Invalid key data, not a sequence"); //$NON-NLS-1$
            }

            ASN1Sequence s = (ASN1Sequence) r;
            if ( s.size() != 2 || ! ( s.getObjectAt(0) instanceof ASN1Integer ) || ! ( s.getObjectAt(1) instanceof ASN1Integer ) ) {
                throw new CryptoException("Invalid key data, invalid parameters"); //$NON-NLS-1$
            }

            ASN1Integer mod = (ASN1Integer) s.getObjectAt(0);
            ASN1Integer exp = (ASN1Integer) s.getObjectAt(1);
            try {
                KeyFactory f = KeyFactory.getInstance("RSA"); //$NON-NLS-1$
                return new PublicKeyEntry(f.generatePublic(new RSAPublicKeySpec(mod.getValue(), exp.getValue())));
            }
            catch ( Exception e ) {
                throw new CryptoException("Invalid key in input", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param isr
     * @param found
     * @return
     * @throws CryptoException
     * @throws IOException
     */
    private static PublicKeyEntry parseRFC4716 ( BufferedReader br ) throws CryptoException, IOException {
        Map<String, String> headers = new HashMap<>();
        String decodeASCIIArmor = decodeASCIIArmor(br, "---- END SSH2 PUBLIC KEY ----", headers); //$NON-NLS-1$
        return new PublicKeyEntry(decodeSSHSpec(decodeASCIIArmor), headers.get("comment")); //$NON-NLS-1$
    }


    private static String decodeASCIIArmor ( BufferedReader br, String endMarker ) throws IOException, CryptoException {
        return decodeASCIIArmor(br, endMarker, null);
    }


    /**
     * @param br
     * @param headers
     * @return
     * @throws IOException
     * @throws CryptoException
     */
    private static String decodeASCIIArmor ( BufferedReader br, String endMarker, Map<String, String> headers ) throws IOException, CryptoException {
        String line;
        StringBuilder b64Data = new StringBuilder();
        while ( ( line = br.readLine() ) != null ) {
            int sepPos = line.indexOf(':');
            if ( endMarker.equals(line.trim()) ) { // $NON-NLS-1$
                return b64Data.toString();
            }
            else if ( sepPos > 0 ) {
                // header
                if ( headers != null ) {
                    String key = line.substring(0, sepPos).trim().toLowerCase(Locale.ROOT);
                    String value = line.substring(sepPos + 1).trim();
                    headers.put(key, value);
                }
                continue;
            }
            b64Data.append(line.trim());
        }
        throw new CryptoException("Missing end marker"); //$NON-NLS-1$
    }


    /**
     * @param is
     * @param found
     * @throws IOException
     * @throws CryptoException
     */
    private static PublicKeyEntry parseX509 ( InputStream is ) throws IOException, CryptoException {
        is.reset();
        try {
            return new PublicKeyEntry(CertificateFactory.getInstance("X509").generateCertificate(is).getPublicKey()); //$NON-NLS-1$
        }
        catch ( CertificateException e ) {
            throw new CryptoException("Failed to parse X509 certificates", e); //$NON-NLS-1$
        }
    }


    /**
     * @param line
     * @return
     * @throws CryptoException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    private static PublicKeyEntry parseOpenSSH ( String line ) throws CryptoException {
        String[] split = StringUtils.split(line, " ", 3); //$NON-NLS-1$
        if ( split.length < 2 && !"ssh-rsa".equals(split[ 0 ]) ) { //$NON-NLS-1$
            return null;
        }
        String comment = null;
        if ( split.length > 2 ) {
            comment = split[ 2 ];
        }
        return new PublicKeyEntry(decodeSSHSpec(split[ 1 ]), comment);
    }


    /**
     * @param b64data
     * @return
     * @throws CryptoException
     */
    private static PublicKey decodeSSHSpec ( String b64data ) throws CryptoException {
        RSAPublicKeySpec decodeRSAPublic = OpenSSHKeyCodec.decodeRSAPublic(b64data);
        try {
            KeyFactory f = KeyFactory.getInstance("RSA"); //$NON-NLS-1$
            return f.generatePublic(decodeRSAPublic);
        }
        catch ( Exception e ) {
            throw new CryptoException("Invalid key in input", e); //$NON-NLS-1$
        }
    }

    /**
     * @author mbechler
     *
     */
    public static final class PublicKeyEntry {

        private PublicKey key;
        private String comment;


        /**
         * @param k
         * 
         */
        public PublicKeyEntry ( PublicKey k ) {
            this(k, null);
        }


        /**
         * @param k
         * @param comment
         */
        public PublicKeyEntry ( PublicKey k, String comment ) {
            this.key = k;
            this.comment = comment;
        }


        /**
         * @return the key
         */
        public PublicKey getKey () {
            return this.key;
        }


        /**
         * @return the comment
         */
        public String getComment () {
            return this.comment;
        }

    }
}
