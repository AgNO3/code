/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.10.2014 by mbechler
 */
package eu.agno3.runtime.crypto.tls.internal;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.TLSConfiguration;


/**
 * @author mbechler
 *
 */
public final class CipherSelectionUtils {

    /**
     * 
     */
    private CipherSelectionUtils () {}


    /**
     * @param cfg
     * @param supportedCipherSuites
     * @return the usable ciphers
     * @throws CryptoException
     */
    public static String[] getUsableCiphers ( TLSConfiguration cfg, String[] supportedCipherSuites ) throws CryptoException {
        List<String> usableCiphers = CipherSelectionUtils.selectCiphers(cfg.getCipherSuites(), supportedCipherSuites);

        if ( usableCiphers.isEmpty() ) {
            throw new CryptoException("No usable ciphers found"); //$NON-NLS-1$
        }
        return usableCiphers.toArray(new String[] {});
    }


    /**
     * @param cfg
     * @param supportedProtocols
     * @return the usable protocols
     * @throws CryptoException
     */
    public static String[] getUsableProtocols ( TLSConfiguration cfg, String[] supportedProtocols ) throws CryptoException {
        List<String> usableCiphers = CipherSelectionUtils.selectCiphers(cfg.getProtocols(), supportedProtocols);

        if ( usableCiphers.isEmpty() ) {
            throw new CryptoException(
                String.format("No usable protcols found, requested %s, supported are %s", cfg.getProtocols(), Arrays.toString(supportedProtocols))); //$NON-NLS-1$
        }
        return usableCiphers.toArray(new String[] {});
    }


    /**
     * @param requested
     * @param supported
     * @return the intersection of requested and supported ciphers
     */
    public static List<String> selectCiphers ( List<String> requested, String[] supported ) {
        Set<String> usableCiphers = new HashSet<>(Arrays.asList(supported));
        List<String> ciphers = new ArrayList<>();
        for ( String pref : requested ) {
            if ( usableCiphers.contains(pref) ) {
                ciphers.add(pref);
            }
        }

        return ciphers;
    }

}
