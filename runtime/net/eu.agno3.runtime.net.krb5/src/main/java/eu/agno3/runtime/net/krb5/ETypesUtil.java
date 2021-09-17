/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.04.2015 by mbechler
 */
package eu.agno3.runtime.net.krb5;


import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import sun.security.krb5.EncryptedData;


/**
 * @author mbechler
 *
 */
public final class ETypesUtil {

    private static final Map<Integer, String> ETYPE_NAMES = new HashMap<>();

    /**
     * All supported etypes
     */
    public static final String[] ETYPES = new String[] {
        "aes256-cts-hmac-sha1-96", //$NON-NLS-1$
        "aes128-cts-hmac-sha1-96", //$NON-NLS-1$
        "arcfour-hmac-md5", //$NON-NLS-1$
        "des3-cbc-sha1", //$NON-NLS-1$
        "des-cbc-md5", //$NON-NLS-1$
    };

    /**
     * All etype codes
     */
    public static final int[] ALL_ETYPE_CODES = new int[] {
        EncryptedData.ETYPE_AES256_CTS_HMAC_SHA1_96, EncryptedData.ETYPE_AES128_CTS_HMAC_SHA1_96, EncryptedData.ETYPE_ARCFOUR_HMAC,
        EncryptedData.ETYPE_ARCFOUR_HMAC_EXP, EncryptedData.ETYPE_DES3_CBC_HMAC_SHA1_KD, EncryptedData.ETYPE_DES_CBC_MD5,
        EncryptedData.ETYPE_DES_CBC_MD4, EncryptedData.ETYPE_DES_CBC_CRC
    };


    static {
        ETYPE_NAMES.put(EncryptedData.ETYPE_AES256_CTS_HMAC_SHA1_96, "aes256-cts-hmac-sha1-96"); //$NON-NLS-1$
        ETYPE_NAMES.put(EncryptedData.ETYPE_AES128_CTS_HMAC_SHA1_96, "aes128-cts-hmac-sha1-96"); //$NON-NLS-1$
        ETYPE_NAMES.put(EncryptedData.ETYPE_ARCFOUR_HMAC, "arcfour-hmac-md5"); //$NON-NLS-1$
        ETYPE_NAMES.put(EncryptedData.ETYPE_ARCFOUR_HMAC_EXP, "arcfour-hmac-md5-exp"); //$NON-NLS-1$
        ETYPE_NAMES.put(EncryptedData.ETYPE_DES3_CBC_HMAC_SHA1_KD, "des3-cbc-sha1"); //$NON-NLS-1$
        ETYPE_NAMES.put(EncryptedData.ETYPE_DES_CBC_MD5, "des-cbc-md5"); //$NON-NLS-1$
        ETYPE_NAMES.put(EncryptedData.ETYPE_DES_CBC_MD4, "des-cbc-md4"); //$NON-NLS-1$
        ETYPE_NAMES.put(EncryptedData.ETYPE_DES_CBC_CRC, "des-cbc-crc"); //$NON-NLS-1$

    }

    /**
     * Default etypes for WEAK setting
     */
    public static final Set<Integer> DEFAULT_WEAK_ETYPES = Collections.unmodifiableSet(
        new HashSet<>(
            Arrays.asList(
                EncryptedData.ETYPE_AES256_CTS_HMAC_SHA1_96,
                EncryptedData.ETYPE_AES128_CTS_HMAC_SHA1_96,
                EncryptedData.ETYPE_DES3_CBC_HMAC_SHA1_KD,
                EncryptedData.ETYPE_ARCFOUR_HMAC,
                EncryptedData.ETYPE_DES_CBC_MD5,
                EncryptedData.ETYPE_DES_CBC_MD4)));

    /**
     * Default etypes for LEGACY setting
     */
    public static final Set<Integer> DEFAULT_LEGACY_ETYPES = Collections.unmodifiableSet(
        new HashSet<>(
            Arrays.asList(
                EncryptedData.ETYPE_AES256_CTS_HMAC_SHA1_96,
                EncryptedData.ETYPE_AES128_CTS_HMAC_SHA1_96,
                EncryptedData.ETYPE_DES3_CBC_HMAC_SHA1_KD,
                EncryptedData.ETYPE_ARCFOUR_HMAC)));

    /**
     * Default etypes for secure setting
     */
    public static final Set<Integer> DEFAULT_ETYPES = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList(EncryptedData.ETYPE_AES256_CTS_HMAC_SHA1_96, EncryptedData.ETYPE_AES128_CTS_HMAC_SHA1_96)));

    /**
     * Default etypes for secure (256) setting
     */
    public static final Set<Integer> DEFAULT_ETYPES_256 = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList(EncryptedData.ETYPE_AES256_CTS_HMAC_SHA1_96)));


    /**
     * 
     */
    private ETypesUtil () {}


    /**
     * @param etypes
     * @return a MIT config string with the etypes
     * @throws KerberosException
     */
    public static String makeETypesString ( Set<Integer> etypes ) throws KerberosException {
        List<String> strTypes = new LinkedList<>();
        for ( int etype : etypes ) {
            strTypes.add(mapEType(etype));
        }
        return StringUtils.join(strTypes, ' ');
    }


    /**
     * @param etypeCodes
     * @return array of etype strings
     * @throws KerberosException
     */
    public static String[] mapETypes ( int[] etypeCodes ) throws KerberosException {
        String[] ets = new String[etypeCodes.length];
        for ( int i = 0; i < etypeCodes.length; i++ ) {
            ets[ i ] = mapEType(etypeCodes[ i ]);
        }
        return ets;
    }


    /**
     * @param etypeCodes
     * @return array of etype strings
     * @throws KerberosException
     */
    public static Collection<String> mapETypes ( Collection<Integer> etypeCodes ) throws KerberosException {
        List<String> res = new LinkedList<>();
        for ( Integer et : etypeCodes ) {
            if ( et == null ) {
                continue;
            }
            res.add(mapEType(et));
        }
        return res;
    }


    /**
     * @param etype
     * @return the etype number or null
     */
    public static Integer eTypeFromMITString ( String etype ) {
        for ( Entry<Integer, String> e : ETYPE_NAMES.entrySet() ) {
            if ( e.getValue().equalsIgnoreCase(etype) ) {
                return e.getKey();
            }
        }
        return null;
    }


    /**
     * @param etype
     * @return a MIT etype efor the given
     * @throws KerberosException
     */
    public static String mapEType ( int etype ) throws KerberosException {
        String etypeName = ETYPE_NAMES.get(etype);
        if ( etypeName == null ) {
            throw new KerberosException("Unknown EType " + etype); //$NON-NLS-1$
        }
        return etypeName;
    }


    /**
     * @param eType
     * @return the algorithm name for the etype
     */
    public static String getAlgoFromEtype ( int eType ) {
        switch ( eType ) {
        case EncryptedData.ETYPE_DES_CBC_CRC:
        case EncryptedData.ETYPE_DES_CBC_MD5:
            return "DES"; //$NON-NLS-1$

        case EncryptedData.ETYPE_DES3_CBC_HMAC_SHA1_KD:
            return "DESede"; //$NON-NLS-1$

        case EncryptedData.ETYPE_ARCFOUR_HMAC:
            return "ArcFourHmac"; //$NON-NLS-1$

        case EncryptedData.ETYPE_AES128_CTS_HMAC_SHA1_96:
            return "AES128"; //$NON-NLS-1$

        case EncryptedData.ETYPE_AES256_CTS_HMAC_SHA1_96:
            return "AES256"; //$NON-NLS-1$

        case EncryptedData.ETYPE_NULL:
            return "NULL"; //$NON-NLS-1$

        default:
            throw new IllegalArgumentException("Unsupported encryption type: " + eType); //$NON-NLS-1$
        }
    }


    /**
     * 
     * @param algo
     * @return the etype from the algorithm name
     */
    public static int getEtypeFromAlgo ( String algo ) {
        switch ( algo ) {
        case "DES": //$NON-NLS-1$
            return EncryptedData.ETYPE_DES_CBC_MD5;
        case "DESede": //$NON-NLS-1$
            return EncryptedData.ETYPE_DES3_CBC_HMAC_SHA1_KD;
        case "ArcFourHmac": //$NON-NLS-1$
            return EncryptedData.ETYPE_ARCFOUR_HMAC;
        case "AES128": //$NON-NLS-1$
            return EncryptedData.ETYPE_AES128_CTS_HMAC_SHA1_96;
        case "AES256": //$NON-NLS-1$
            return EncryptedData.ETYPE_AES256_CTS_HMAC_SHA1_96;
        case "NULL": //$NON-NLS-1$
            return EncryptedData.ETYPE_NULL;
        default:
            throw new IllegalArgumentException("Unsupported encryption type: " + algo); //$NON-NLS-1$
        }
    }

}
