/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.12.2015 by mbechler
 */
package eu.agno3.runtime.crypto.pkcs7;


import java.util.Set;

import org.bouncycastle.cms.CMSSignedData;
import org.joda.time.DateTime;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.TrustConfiguration;


/**
 * @author mbechler
 *
 */
public interface PKCS7Verifier {

    /**
     * @param tc
     * @param signed
     * @param date
     * @param keyUsage
     * @param ekus
     * @throws CryptoException
     */
    void verify ( TrustConfiguration tc, CMSSignedData signed, DateTime date, boolean[] keyUsage, Set<String> ekus ) throws CryptoException;


    /**
     * @param tc
     * @param signed
     * @param date
     * @param keyUsage
     * @param ekus
     * @return whether there is a valid signature
     * @throws CryptoException
     */
    boolean validate ( TrustConfiguration tc, CMSSignedData signed, DateTime date, boolean[] keyUsage, Set<String> ekus ) throws CryptoException;

}
