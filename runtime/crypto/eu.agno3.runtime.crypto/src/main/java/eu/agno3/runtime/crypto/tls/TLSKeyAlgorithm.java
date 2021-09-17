/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.03.2015 by mbechler
 */
package eu.agno3.runtime.crypto.tls;


import java.util.HashMap;
import java.util.Map;

import eu.agno3.runtime.crypto.CryptoException;


/**
 * @author mbechler
 *
 */
@SuppressWarnings ( "javadoc" )
public enum TLSKeyAlgorithm {

    ECDHE_ECSDA(true, false, false),
    ECDHE_RSA(true, false, false),
    DHE_RSA(true, false, false),
    DHE_DSS(true, false, false),
    RSA(false, false, false),
    ECDH_ECDSA(false, false, false),
    ECDH_RSA(false, false, false),
    ECDH_anon(false, false, true),
    DH_anon(false, false, true),
    RSA_EXPORT(false, true, false),
    DH_anon_EXPORT(false, true, true),
    KRB5(false, false, false),
    KRB5_EXPORT(false, true, false);

    private static final Map<String, TLSKeyAlgorithm> OPENSSL_MAP = new HashMap<>();

    static {
        OPENSSL_MAP.put("ECDHE-ECSDA", ECDHE_ECSDA); //$NON-NLS-1$
        OPENSSL_MAP.put("ECDHE-RSA", ECDHE_RSA); //$NON-NLS-1$
        OPENSSL_MAP.put("DHE-RSA", DHE_RSA); //$NON-NLS-1$
        OPENSSL_MAP.put("EDH-RSA", DHE_RSA); //$NON-NLS-1$
        OPENSSL_MAP.put("DHE-DSS", DHE_DSS); //$NON-NLS-1$
        OPENSSL_MAP.put("EDH-DSS", DHE_DSS); //$NON-NLS-1$
        OPENSSL_MAP.put("RSA", RSA); //$NON-NLS-1$
        OPENSSL_MAP.put("ECDH-RSA", ECDH_RSA); //$NON-NLS-1$
        OPENSSL_MAP.put("ECDH-ECDSA", ECDH_ECDSA); //$NON-NLS-1$
    }

    private boolean anon;
    private boolean export;
    private boolean pfs;


    private TLSKeyAlgorithm ( boolean pfs, boolean export, boolean anon ) {
        this.pfs = pfs;
        this.export = export;
        this.anon = anon;
    }


    /**
     * @return the pfs
     */
    public boolean isPfs () {
        return this.pfs;
    }


    /**
     * @return the export
     */
    public boolean isExport () {
        return this.export;
    }


    /**
     * @return the anon
     */
    public boolean isAnon () {
        return this.anon;
    }


    public static TLSKeyAlgorithm fromString ( String val ) {
        return valueOf(val);
    }


    public static String toString ( TLSKeyAlgorithm algo ) {
        if ( algo == null ) {
            return null;
        }
        return algo.name();
    }


    /**
     * @param string
     * @return
     * @throws CryptoException
     */
    public static TLSKeyAlgorithm mapOpenSSL ( String string ) throws CryptoException {
        TLSKeyAlgorithm alg = OPENSSL_MAP.get(string);
        if ( alg == null ) {
            throw new CryptoException("Cannot map OpenSSL key algo " + string); //$NON-NLS-1$
        }
        return alg;
    }
}
