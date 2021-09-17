/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.12.2015 by mbechler
 */
package eu.agno3.runtime.crypto.pkcs7.internal;


import java.util.Set;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.joda.time.DateTime;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.pkcs7.PKCS7Verifier;
import eu.agno3.runtime.crypto.tls.TrustChecker;
import eu.agno3.runtime.crypto.tls.TrustConfiguration;


/**
 * @author mbechler
 *
 */
@Component ( service = PKCS7Verifier.class )
public class PKCS7VerifierImpl implements PKCS7Verifier {

    private TrustChecker trustChecker;


    @Reference
    protected synchronized void setTrustChecker ( TrustChecker tc ) {
        this.trustChecker = tc;
    }


    protected synchronized void unsetTrustChecker ( TrustChecker tc ) {
        if ( this.trustChecker == tc ) {
            this.trustChecker = null;
        }
    }


    @Override
    public void verify ( TrustConfiguration tc, CMSSignedData signed, DateTime date, boolean[] keyUsage, Set<String> ekus ) throws CryptoException {
        boolean verified = validate(tc, signed, date, keyUsage, ekus);

        if ( !verified ) {
            throw new CryptoException("Signature verification failed"); //$NON-NLS-1$
        }
    }


    @Override
    public boolean validate ( TrustConfiguration tc, CMSSignedData signed, DateTime date, boolean[] keyUsage, Set<String> ekus )
            throws CryptoException {
        try {
            return signed.verifySignatures(new TrustCheckingVerifierProvider(tc, this.trustChecker, signed.getCertificates(), date, keyUsage, ekus));
        }
        catch ( CMSException e ) {
            throw new CryptoException("Internal failure during signature verification", e); //$NON-NLS-1$
        }
    }
}
