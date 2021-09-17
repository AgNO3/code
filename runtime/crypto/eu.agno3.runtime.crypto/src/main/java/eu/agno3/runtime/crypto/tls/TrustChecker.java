/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.09.2015 by mbechler
 */
package eu.agno3.runtime.crypto.tls;


import java.security.cert.Certificate;
import java.security.cert.PKIXCertPathBuilderResult;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import eu.agno3.runtime.crypto.CryptoException;


/**
 * @author mbechler
 *
 */
public interface TrustChecker {

    /**
     * @param cfg
     * @param root
     * @param other
     * @param chain
     * @param date
     * @param keyUsage
     * @param ekus
     * @return the validation result, if valid
     * @throws CryptoException
     */
    PKIXCertPathBuilderResult validate ( TrustConfiguration cfg, Certificate root, Collection<Certificate> other, Date date, boolean[] keyUsage,
            Set<String> ekus ) throws CryptoException;


    /**
     * 
     * @param cfg
     * @param chain
     * @param date
     * @param keyUsage
     * @param ekus
     * @return the validation result, if valid
     * @throws CryptoException
     */
    PKIXCertPathBuilderResult validateChain ( TrustConfiguration cfg, List<Certificate> chain, Date date, boolean[] keyUsage, Set<String> ekus )
            throws CryptoException;

}
