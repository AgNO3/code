/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.openssl;


import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Enumeration;

import javax.security.auth.x500.X500Principal;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DERT61String;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.asn1.DLSet;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;

import eu.agno3.runtime.crypto.CryptoException;


/**
 * @author mbechler
 *
 */
public final class NameHashUtil {

    private static final Logger log = Logger.getLogger(NameHashUtil.class);

    /**
     * 
     */
    private static final String DER = "DER"; //$NON-NLS-1$
    /**
     * 
     */
    private static final String NAME_DIGEST_ALGO = "SHA1"; //$NON-NLS-1$


    /**
     * 
     */
    private NameHashUtil () {}


    /**
     * 
     * @param cert
     * @return the certificate subject name hash
     * @throws CryptoException
     */
    public static String opensslSubjectHash ( X509Certificate cert ) throws CryptoException {
        try {
            return opensslNameHash(new JcaX509CertificateHolder(cert).getSubject());
        }
        catch ( CertificateEncodingException e ) {
            throw new CryptoException("Failed to read certificate", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * @param cert
     * @return the certificate issuer name hash
     * @throws CryptoException
     */
    public static String opensslIssuerHash ( X509Certificate cert ) throws CryptoException {
        try {
            return opensslNameHash(new JcaX509CertificateHolder(cert).getIssuer());
        }
        catch ( CertificateEncodingException e ) {
            throw new CryptoException("Failed to read certificate", e); //$NON-NLS-1$
        }
    }


    /**
     * @param issuer
     * @return the name hash
     * @throws CryptoException
     */
    public static String opensslNameHash ( X500Principal issuer ) throws CryptoException {
        return opensslNameHash(X500Name.getInstance(issuer.getEncoded()));
    }


    /**
     * @param principal
     * @return the name hash value for the given principal
     * @throws CryptoException
     */
    public static String opensslNameHash ( X500Name principal ) throws CryptoException {

        try {
            ASN1Sequence canonNameASN = (ASN1Sequence) canonicalize(principal.toASN1Primitive());
            MessageDigest dgst = MessageDigest.getInstance(NAME_DIGEST_ALGO);

            Enumeration<ASN1Encodable> objs = canonNameASN.getObjects();
            while ( objs.hasMoreElements() ) {
                ASN1Encodable obj = objs.nextElement();
                dgst.update(obj.toASN1Primitive().getEncoded(DER));
            }

            byte[] digest = dgst.digest();
            if ( digest == null || digest.length < 4 ) {
                throw new CryptoException("Unexpected hash value " + Arrays.toString(digest)); //$NON-NLS-1$
            }

            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(digest, 0, 4));
            return String.format("%08x", Integer.reverseBytes(dis.readInt())); //$NON-NLS-1$
        }
        catch (
            NoSuchAlgorithmException |
            IOException e ) {
            throw new CryptoException("Failed to generate name hash", e); //$NON-NLS-1$
        }
    }


    /**
     * @param canonName
     * @throws CryptoException
     */
    static ASN1Primitive canonicalize ( ASN1Primitive obj ) throws CryptoException {

        if ( obj instanceof DERSequence || obj instanceof DLSequence ) {
            return new DERSequence(canonicalizeVector(obj));
        }
        else if ( obj instanceof DERSet || obj instanceof DLSet ) {
            return new DERSet(canonicalizeVector(obj));
        }
        else if ( obj instanceof ASN1ObjectIdentifier ) {
            return obj;
        }
        else if ( obj instanceof DERUTF8String ) {
            return new DERUTF8String(canonicalizeString( ( (DERUTF8String) obj ).getString()));
        }
        else if ( obj instanceof DERT61String ) {
            return new DERUTF8String(canonicalizeString( ( (DERT61String) obj ).getString()));
        }
        else if ( obj instanceof DERIA5String ) {
            return new DERUTF8String(canonicalizeString( ( (DERIA5String) obj ).getString()));
        }
        else if ( obj instanceof DERPrintableString ) {
            return new DERUTF8String(canonicalizeString( ( (DERPrintableString) obj ).getString()));
        }
        else {
            throw new CryptoException("Unsupported type " + obj.getClass().getName()); //$NON-NLS-1$
        }

    }


    /**
     * @param obj
     * @return
     */
    private static String canonicalizeString ( String str ) {
        String canonicalized = str.trim().replaceAll("[ ]+", StringUtils.SPACE); //$NON-NLS-1$
        StringBuilder sb = new StringBuilder();
        // this lowercases the string, but only for ASCII characters
        for ( char c : canonicalized.toCharArray() ) {
            if ( c > 127 ) {
                sb.append(c);
            }
            else {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }


    /**
     * @param obj
     * @return
     * @throws CryptoException
     */
    private static ASN1EncodableVector canonicalizeVector ( ASN1Primitive obj ) throws CryptoException {
        Enumeration<ASN1Encodable> children = null;
        if ( obj instanceof DERSequence ) {
            children = ( (DERSequence) obj ).getObjects();
        }
        else if ( obj instanceof DLSequence ) {
            children = ( (DLSequence) obj ).getObjects();
        }
        else if ( obj instanceof DERSet ) {
            children = ( (DERSet) obj ).getObjects();
        }
        else if ( obj instanceof DLSet ) {
            children = ( (DLSet) obj ).getObjects();
        }
        else {
            throw new CryptoException("Unsupported collection " + obj.getClass().getName()); //$NON-NLS-1$
        }

        ASN1EncodableVector vec = new ASN1EncodableVector();
        while ( children != null && children.hasMoreElements() ) {
            vec.add(canonicalize(children.nextElement().toASN1Primitive()));
        }

        return vec;
    }


    static void dumpObject ( ASN1Primitive obj, int depth ) throws IOException {

        if ( log.isDebugEnabled() ) {
            log.debug(StringUtils.repeat(' ', depth) + obj.getClass().getName());

            if ( obj instanceof ASN1ObjectIdentifier ) {
                log.debug(StringUtils.repeat(' ', depth) + obj);
            }
            else if ( obj instanceof DERUTF8String ) {
                String string = ( (DERUTF8String) obj ).getString();
                log.debug(StringUtils.repeat(' ', depth) + string);
                log.debug(StringUtils.repeat(' ', depth) + Arrays.toString(obj.getEncoded(DER)));
            }
        }

        Enumeration<ASN1Encodable> children = null;
        if ( obj instanceof DERSequence ) {
            children = ( (DERSequence) obj ).getObjects();
        }
        else if ( obj instanceof DLSequence ) {
            children = ( (DLSequence) obj ).getObjects();
        }
        else if ( obj instanceof DERSet ) {
            children = ( (DERSet) obj ).getObjects();
        }
        else if ( obj instanceof DLSet ) {
            children = ( (DLSet) obj ).getObjects();
        }

        while ( children != null && children.hasMoreElements() ) {
            ASN1Encodable element = children.nextElement();
            dumpObject(element.toASN1Primitive(), depth + 1);
        }

    }

}
