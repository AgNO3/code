/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.truststore.revocation.internal;


import java.security.cert.PKIXCertPathChecker;


/**
 * @author mbechler
 *
 */
public class PKIXCertPathCheckerThreadLocal extends ThreadLocal<PKIXCertPathChecker> {

    private PKIXCertPathCheckerFactory factory;


    /**
     * @param factory
     * 
     */
    public PKIXCertPathCheckerThreadLocal ( PKIXCertPathCheckerFactory factory ) {
        this.factory = factory;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.ThreadLocal#initialValue()
     */
    @Override
    protected PKIXCertPathChecker initialValue () {
        return this.factory.createInstance();
    }
}
