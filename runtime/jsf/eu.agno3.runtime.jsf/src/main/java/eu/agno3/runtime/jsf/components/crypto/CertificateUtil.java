/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.11.2014 by mbechler
 */
package eu.agno3.runtime.jsf.components.crypto;


import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.security.auth.x500.X500Principal;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.joda.time.DateTime;

import eu.agno3.runtime.crypto.HashUtil;
import eu.agno3.runtime.jsf.i18n.BaseMessages;
import eu.agno3.runtime.jsf.util.date.DateFormatter;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "certificateUtil" )
public class CertificateUtil {

    private static final String PKINIT_KDC_OID = "1.3.6.1.5.2.3.5"; //$NON-NLS-1$
    private static final String PKINIT_CLIENT_OID = "1.3.6.1.5.2.3.4"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(CertificateUtil.class);

    /*
     * KeyUsage ::= BIT STRING
     * digitalSignature (0),
     * nonRepudiation (1),
     * keyEncipherment (2),
     * dataEncipherment (3),
     * keyAgreement (4),
     * keyCertSign (5),
     * cRLSign (6),
     * encipherOnly (7),
     * decipherOnly (8)
     */
    private static final Map<Integer, String> KEY_USAGES = new LinkedHashMap<>();

    /*
     * GeneralName ::= CHOICE
     * otherName [0] OtherName,
     * rfc822Name [1] IA5String,
     * dNSName [2] IA5String,
     * x400Address [3] ORAddress,
     * directoryName [4] Name,
     * ediPartyName [5] EDIPartyName,
     * uniformResourceIdentifier [6] IA5String,
     * iPAddress [7] OCTET STRING,
     * registeredID [8] OBJECT IDENTIFIER
     */
    private static final String[] GENERAL_NAME_TYPES = new String[] {
        "otherName", //$NON-NLS-1$
        "rfc822Name", //$NON-NLS-1$
        "dNSName", //$NON-NLS-1$
        "x400Address", //$NON-NLS-1$
        "directoryName", //$NON-NLS-1$
        "ediPartyName", //$NON-NLS-1$
        "uniformResourceIdentifier", //$NON-NLS-1$
        "iPAddress", //$NON-NLS-1$
        "registeredID" //$NON-NLS-1$
    };

    private static final Map<String, String> EXTENDED_KEY_USAGES = new LinkedHashMap<>();
    private static final Set<String> IGNORED_EXTENSION_OIDS = new HashSet<>();
    private static final Map<String, X509ExtensionFormatter> EXTENSION_FORMATTERS = new HashMap<>();
    private static final Map<String, String> CN_OID_MAP = new HashMap<>();


    static {
        EXTENDED_KEY_USAGES.put(KeyPurposeId.anyExtendedKeyUsage.getId(), "any"); //$NON-NLS-1$
        EXTENDED_KEY_USAGES.put(KeyPurposeId.id_kp_clientAuth.getId(), "clientAuth"); //$NON-NLS-1$
        EXTENDED_KEY_USAGES.put(KeyPurposeId.id_kp_serverAuth.getId(), "serverAuth"); //$NON-NLS-1$
        EXTENDED_KEY_USAGES.put(KeyPurposeId.id_kp_codeSigning.getId(), "codeSigning"); //$NON-NLS-1$
        EXTENDED_KEY_USAGES.put(KeyPurposeId.id_kp_emailProtection.getId(), "emailProtection"); //$NON-NLS-1$
        EXTENDED_KEY_USAGES.put(KeyPurposeId.id_kp_OCSPSigning.getId(), "OCSPSigning"); //$NON-NLS-1$
        EXTENDED_KEY_USAGES.put(KeyPurposeId.id_kp_timeStamping.getId(), "timeStamping"); //$NON-NLS-1$
        EXTENDED_KEY_USAGES.put(KeyPurposeId.id_kp_ipsecEndSystem.getId(), "ipsecEndSystem"); //$NON-NLS-1$
        EXTENDED_KEY_USAGES.put(KeyPurposeId.id_kp_ipsecIKE.getId(), "ipsecIKE"); //$NON-NLS-1$
        EXTENDED_KEY_USAGES.put(KeyPurposeId.id_kp_ipsecTunnel.getId(), "ipsecTunnel"); //$NON-NLS-1$
        EXTENDED_KEY_USAGES.put(KeyPurposeId.id_kp_ipsecUser.getId(), "ipsecUser"); //$NON-NLS-1$
        EXTENDED_KEY_USAGES.put(KeyPurposeId.id_kp_smartcardlogon.getId(), "smartcardLogon"); //$NON-NLS-1$
        EXTENDED_KEY_USAGES.put(PKINIT_CLIENT_OID, "pkinit-clientAuth"); //$NON-NLS-1$
        EXTENDED_KEY_USAGES.put(PKINIT_KDC_OID, "pkinit-kdc"); //$NON-NLS-1$

        IGNORED_EXTENSION_OIDS.add(Extension.subjectKeyIdentifier.getId());
        IGNORED_EXTENSION_OIDS.add(Extension.authorityKeyIdentifier.getId());
        IGNORED_EXTENSION_OIDS.add(Extension.extendedKeyUsage.getId());
        IGNORED_EXTENSION_OIDS.add(Extension.subjectAlternativeName.getId());
        IGNORED_EXTENSION_OIDS.add(Extension.basicConstraints.getId());
        IGNORED_EXTENSION_OIDS.add(Extension.keyUsage.getId());
        EXTENSION_FORMATTERS.put(Extension.authorityInfoAccess.getId(), new AuthorityInfoAccessFormatter());
        EXTENSION_FORMATTERS.put(Extension.cRLDistributionPoints.getId(), new CRLDistributionPointFormatter());
        EXTENSION_FORMATTERS.put(Extension.issuerAlternativeName.getId(), new IssuerAlternativeNameFormatter());
        EXTENSION_FORMATTERS.put(Extension.certificatePolicies.getId(), new CertificatePoliciesFormatter());
        CN_OID_MAP.put(
            "1.3.6.1.4.1.44756.1.1.1", //$NON-NLS-1$
            "agentId"); //$NON-NLS-1$
        CN_OID_MAP.put(
            "1.3.6.1.4.1.44756.1.1.2", //$NON-NLS-1$
            "serverId"); //$NON-NLS-1$
        CN_OID_MAP.put(
            "1.3.6.1.4.1.44756.1.1.3", //$NON-NLS-1$
            "guiId"); //$NON-NLS-1$
        CN_OID_MAP.put(
            "1.2.840.113549.1.9.1", //$NON-NLS-1$
            "emailAddress"); //$NON-NLS-1$

        KEY_USAGES.put(KeyUsage.digitalSignature, "digitalSignature"); //$NON-NLS-1$
        KEY_USAGES.put(KeyUsage.nonRepudiation, "nonRepudation"); //$NON-NLS-1$
        KEY_USAGES.put(KeyUsage.keyEncipherment, "keyEncipherment"); //$NON-NLS-1$
        KEY_USAGES.put(KeyUsage.dataEncipherment, "dataEncipherment"); //$NON-NLS-1$
        KEY_USAGES.put(KeyUsage.keyAgreement, "keyAgreement"); //$NON-NLS-1$
        KEY_USAGES.put(KeyUsage.keyCertSign, "keyCertSign"); //$NON-NLS-1$
        KEY_USAGES.put(KeyUsage.cRLSign, "cRLSign"); //$NON-NLS-1$
        KEY_USAGES.put(KeyUsage.encipherOnly, "encipherOnly"); //$NON-NLS-1$
        KEY_USAGES.put(KeyUsage.decipherOnly, "decipherOnly"); //$NON-NLS-1$
    }

    private static final String SHA256 = "SHA-256"; //$NON-NLS-1$

    @Inject
    private DateFormatter dateFormatter;


    /**
     * @param princ
     * @return the prinipal name formatted
     */
    public static String formatPrincipalName ( X500Principal princ ) {
        return StringUtils.replace(
            princ.getName(X500Principal.RFC2253, CN_OID_MAP),
            ",", //$NON-NLS-1$
            ", "); //$NON-NLS-1$
    }


    /**
     * 
     * @param cert
     * @return a formatted version of the validity range
     */
    public String formatValidity ( X509Certificate cert ) {

        DateTime notAfter = new DateTime(cert.getNotAfter());
        DateTime notBefore = new DateTime(cert.getNotBefore());

        String formattedNotAfter = this.dateFormatter.formatDateTimeLocal(notAfter);
        String formattedNotBefore = this.dateFormatter.formatDateTimeLocal(notBefore);

        return BaseMessages.format("crypto.cert.validityRangeFmt", formattedNotBefore, formattedNotAfter); //$NON-NLS-1$
    }


    /**
     * 
     * @param cert
     * @return whether the certificate is in it's validity period
     */
    public static boolean isInValidityPeriod ( X509Certificate cert ) {
        try {
            cert.checkValidity();
            return true;
        }
        catch ( CertificateException e ) {
            log.trace("Certificate expired", e); //$NON-NLS-1$
            return false;
        }
    }


    /**
     * 
     * @param cert
     * @return a CSS style to apply to the validity range, red color if not valid
     */
    public static String getValidityStyle ( X509Certificate cert ) {
        try {
            cert.checkValidity();
            return StringUtils.EMPTY;
        }
        catch ( CertificateException e ) {
            log.trace("Certificate expired", e); //$NON-NLS-1$
            return "color:red"; //$NON-NLS-1$
        }
    }


    /**
     * 
     * @param cert
     * @return basic constraints formatted
     */
    public static String formatBasicConstraints ( X509Certificate cert ) {
        int basicConstraints = cert.getBasicConstraints();

        if ( basicConstraints == -1 ) {
            return BaseMessages.get("crypto.cert.noCa"); //$NON-NLS-1$
        }
        else if ( basicConstraints == Integer.MAX_VALUE ) {
            return BaseMessages.get("crypto.cert.unlimitedCa"); //$NON-NLS-1$
        }
        else {
            return BaseMessages.format("crypto.cert.limitedCaFmt", basicConstraints); //$NON-NLS-1$
        }
    }


    /**
     * 
     * @param cert
     * @return key usage formatted
     */
    public static String formatKeyUsage ( X509Certificate cert ) {
        JcaX509CertificateHolder holder;
        try {
            holder = new JcaX509CertificateHolder(cert);
        }
        catch ( CertificateEncodingException e ) {
            log.warn("Failed to parse certificiate", e); //$NON-NLS-1$
            return StringUtils.EMPTY;
        }

        KeyUsage ku = KeyUsage.fromExtensions(holder.getExtensions());
        if ( ku == null ) {
            return StringUtils.EMPTY;
        }

        boolean first = true;
        StringBuilder sb = new StringBuilder();

        for ( Entry<Integer, String> usage : KEY_USAGES.entrySet() ) {
            if ( ku.hasUsages(usage.getKey()) ) {
                if ( !first ) {
                    sb.append(", "); //$NON-NLS-1$
                }
                else {
                    first = false;
                }
                sb.append(usage.getValue());
            }
        }
        return sb.toString();
    }


    /**
     * 
     * @param cert
     * @return subject alternative names formatted
     */
    public static List<String> mapSubjectAltNames ( X509Certificate cert ) {

        Collection<List<?>> subjectAlternativeNames;
        try {
            subjectAlternativeNames = cert.getSubjectAlternativeNames();
        }
        catch ( CertificateParsingException e ) {
            log.warn("Failed to parse SANs", e); //$NON-NLS-1$
            return Collections.EMPTY_LIST;
        }

        if ( subjectAlternativeNames == null ) {
            return Collections.EMPTY_LIST;
        }

        List<String> res = new ArrayList<>();

        for ( List<?> gnEntry : subjectAlternativeNames ) {
            int type = (Integer) gnEntry.get(0);
            Object data = gnEntry.get(1);

            String formatted = formatGeneralName(type, data);
            res.add(formatted);
        }

        return res;
    }


    /**
     * @param type
     * @param data
     * @return
     */
    protected static String formatGeneralName ( int type, Object data ) {
        String typeName = GENERAL_NAME_TYPES[ type ];
        String formattedData;
        if ( data instanceof String ) {
            formattedData = (String) data;
        }
        else {
            formattedData = formatBinarySANData(type, (byte[]) data);
        }
        return String.format("%s:%s", typeName, formattedData); //$NON-NLS-1$
    }


    /**
     * @param gn
     * @return a formatted general name
     */
    public static String formatGeneralName ( GeneralName gn ) {
        String typeName = GENERAL_NAME_TYPES[ gn.getTagNo() ];
        return String.format("%s:%s", typeName, gn.getName()); //$NON-NLS-1$
    }


    private static String formatBinarySANData ( int type, byte[] data ) {
        if ( GeneralName.otherName == type ) {
            ASN1Sequence seq = ASN1Sequence.getInstance(data);
            ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier) seq.getObjectAt(0);
            ASN1Encodable payload = seq.getObjectAt(1);
            try {
                return String.format("%s:%s", oid.getId(), Hex.encodeHexString(payload.toASN1Primitive().getEncoded())); //$NON-NLS-1$
            }
            catch ( IOException e ) {
                log.warn("Failed to format SAN data", e); //$NON-NLS-1$
                return oid.getId();
            }
        }

        return Hex.encodeHexString(data);
    }


    /**
     * 
     * @param cert
     * @return EKU oids mapped to known strings
     */
    public static List<String> mapExtendedKeyUsage ( X509Certificate cert ) {
        List<String> ekuOids;
        try {
            ekuOids = cert.getExtendedKeyUsage();
        }
        catch ( CertificateParsingException e ) {
            log.warn("Failed to parse EKUs", e); //$NON-NLS-1$
            return Collections.EMPTY_LIST;
        }
        if ( ekuOids == null ) {
            return Collections.EMPTY_LIST;
        }

        List<String> res = new ArrayList<>();
        for ( String oid : ekuOids ) {
            res.add(translateEKU(oid));
        }
        return res;
    }


    /**
     * 
     * @return known EKU OIDs
     */
    public static List<String> getExtendedKeyUsages () {
        return new ArrayList<>(EXTENDED_KEY_USAGES.keySet());
    }


    /**
     * @param oid
     * @return the translated EKU
     */
    public static String translateEKU ( String oid ) {
        if ( EXTENDED_KEY_USAGES.containsKey(oid) ) {
            return EXTENDED_KEY_USAGES.get(oid);
        }
        return oid;
    }


    /**
     * 
     * @return known key usages
     */
    public static Collection<String> getKeyUsages () {
        return KEY_USAGES.values();
    }


    /**
     * @param ku
     * @return the bitstring index of the key usage
     */
    public static String getKeyUsageVal ( String ku ) {
        for ( Entry<Integer, String> k : KEY_USAGES.entrySet() ) {
            if ( k.getValue().equals(ku) ) {
                return String.valueOf(k.getKey());
            }
        }
        return StringUtils.EMPTY;
    }


    /**
     * 
     * @param cert
     * @return certificate fingerprint (SHA256) in hex encoding
     */
    public static String formatFingerprintSHA256 ( X509Certificate cert ) {
        try {
            MessageDigest dgst = MessageDigest.getInstance(SHA256);
            return HashUtil.hexToDotted(Hex.encodeHexString(dgst.digest(cert.getEncoded())).toUpperCase(), true);
        }
        catch (
            CertificateEncodingException |
            NoSuchAlgorithmException e ) {
            log.warn("Failed to produce certificate fingerprint", e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * @param pubKey
     * @return a SHA256 fingerprint of the DER encoded public key
     */
    public static String formatPubkeyFingerprintSHA256 ( PublicKey pubKey ) {
        try {
            MessageDigest dgst = MessageDigest.getInstance(SHA256);
            return HashUtil.hexToDotted(Hex.encodeHexString(dgst.digest(pubKey.getEncoded())).toUpperCase(), true);
        }
        catch ( NoSuchAlgorithmException e ) {
            log.warn("Failed to produce pubkey fingerprint", e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * @param cert
     * @return the serial as hex string
     */
    public static String formatSerial ( X509Certificate cert ) {
        return HashUtil.hexToDotted(Hex.encodeHexString(cert.getSerialNumber().toByteArray()), false);
    }


    /**
     * 
     * @param cert
     * @return the extensions formatted as strings
     */
    public static List<String> formatExtensions ( X509Certificate cert ) {
        List<String> res = new ArrayList<>();
        formatExtensions(cert, res, true, cert.getCriticalExtensionOIDs());
        formatExtensions(cert, res, false, cert.getNonCriticalExtensionOIDs());
        return res;
    }


    /**
     * 
     * @param cert
     * @return whether there are no extensions present
     */
    public static boolean emptyExtensions ( X509Certificate cert ) {
        if ( cert.getNonCriticalExtensionOIDs() != null && !cert.getNonCriticalExtensionOIDs().isEmpty() ) {
            for ( String oid : cert.getNonCriticalExtensionOIDs() ) {
                if ( !IGNORED_EXTENSION_OIDS.contains(oid) ) {
                    return false;
                }
            }
        }
        if ( cert.getCriticalExtensionOIDs() != null && !cert.getCriticalExtensionOIDs().isEmpty() ) {
            for ( String oid : cert.getCriticalExtensionOIDs() ) {
                if ( !IGNORED_EXTENSION_OIDS.contains(oid) ) {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * @param cert
     * @param res
     * @param oids
     */
    protected static void formatExtensions ( X509Certificate cert, List<String> res, boolean critical, Set<String> oids ) {
        if ( oids != null ) {
            for ( String oid : oids ) {
                if ( !IGNORED_EXTENSION_OIDS.contains(oid) ) {
                    res.add(formatExtension(oid, critical, cert.getExtensionValue(oid)));
                }
            }
        }
    }


    /**
     * @param oid
     * @param b
     * @param extensionValue
     * @return
     */
    private static String formatExtension ( String oid, boolean critical, byte[] extensionValue ) {
        if ( !EXTENSION_FORMATTERS.containsKey(oid) ) {
            return String.format("%s critical:%s size:%d", oid, critical, extensionValue.length); //$NON-NLS-1$
        }
        return EXTENSION_FORMATTERS.get(oid).format(extensionValue);
    }


    /**
     * 
     * @param cert
     * @return formatted EKUs
     */
    public static String formatExtendedKeyUsage ( X509Certificate cert ) {
        List<String> mapped = mapExtendedKeyUsage(cert);
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for ( String usage : mapped ) {
            if ( !first ) {
                sb.append(", "); //$NON-NLS-1$
            }
            else {
                first = false;
            }
            sb.append(usage);
        }
        return sb.toString();
    }

}
