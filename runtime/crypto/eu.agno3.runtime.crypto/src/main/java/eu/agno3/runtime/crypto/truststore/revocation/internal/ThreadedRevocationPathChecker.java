/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.truststore.revocation.internal;


import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.PKIXCertPathChecker;
import java.util.Collection;
import java.util.Set;


/**
 * @author mbechler
 *
 */
public class ThreadedRevocationPathChecker extends PKIXCertPathChecker {

    private ThreadLocal<PKIXCertPathChecker> perThread;
    private PKIXCertPathCheckerFactory factory;


    /**
     * @param factory
     * 
     */
    public ThreadedRevocationPathChecker ( PKIXCertPathCheckerFactory factory ) {
        this.perThread = new PKIXCertPathCheckerThreadLocal(factory);
        this.factory = factory;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.security.cert.PKIXCertPathChecker#init(boolean)
     */
    @Override
    public void init ( boolean forward ) throws CertPathValidatorException {
        if ( forward ) {
            throw new CertPathValidatorException("Cannot do forward checking"); //$NON-NLS-1$
        }

        this.perThread.get().init(forward);
    }


    /**
     * {@inheritDoc}
     *
     * @see java.security.cert.PKIXCertPathChecker#isForwardCheckingSupported()
     */
    @Override
    public boolean isForwardCheckingSupported () {
        return this.factory.isForwardCheckingSupported();
    }


    /**
     * {@inheritDoc}
     *
     * @see java.security.cert.PKIXCertPathChecker#getSupportedExtensions()
     */
    @Override
    public Set<String> getSupportedExtensions () {
        return this.factory.getSupportedExtensions();
    }


    /**
     * {@inheritDoc}
     *
     * @see java.security.cert.PKIXCertPathChecker#check(java.security.cert.Certificate, java.util.Collection)
     */
    @Override
    public void check ( Certificate cert, Collection<String> unresolvedCritExts ) throws CertPathValidatorException {
        this.perThread.get().check(cert, unresolvedCritExts);
    }


    /**
     * {@inheritDoc}
     *
     * @see java.security.cert.PKIXCertPathChecker#clone()
     */
    @Override
    public Object clone () {
        return this.perThread.get().clone();
    }
}
