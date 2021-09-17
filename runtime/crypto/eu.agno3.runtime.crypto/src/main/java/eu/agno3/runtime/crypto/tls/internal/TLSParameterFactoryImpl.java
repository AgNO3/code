/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 26, 2016 by mbechler
 */
package eu.agno3.runtime.crypto.tls.internal;


import java.util.Arrays;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;

import org.apache.log4j.Logger;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.ExtendedHostnameVerifier;
import eu.agno3.runtime.crypto.tls.InternalTLSConfiguration;


/**
 * @author mbechler
 *
 */
public class TLSParameterFactoryImpl implements TLSParameterFactory {

    private static final Logger log = Logger.getLogger(TLSParameterFactoryImpl.class);


    /**
     * @param cfg
     * @param supportedCipherSuites
     * @param supportedProtocols
     * @return SSL parameters to set
     * @throws CryptoException
     */
    @Override
    public SSLParameters makeSSLParameters ( InternalTLSConfiguration cfg, String[] supportedCipherSuites, String[] supportedProtocols )
            throws CryptoException {

        String[] usableCiphers = CipherSelectionUtils.getUsableCiphers(cfg, supportedCipherSuites);
        String[] usableProtocols = CipherSelectionUtils.getUsableProtocols(cfg, supportedProtocols);

        if ( log.isTraceEnabled() ) {
            log.trace("Usable protocols are " + Arrays.toString(usableProtocols)); //$NON-NLS-1$
            log.trace("Usable ciphers are " + Arrays.toString(usableCiphers)); //$NON-NLS-1$
        }

        SSLParameters params = createSSLParameters(cfg.getHostnameVerifier(), usableCiphers, usableProtocols);
        params.setNeedClientAuth(cfg.getRequireClientAuth());
        params.setWantClientAuth(cfg.getRequestClientAuth());

        params.setUseCipherSuitesOrder(cfg.useServerCipherPreferences());

        if ( cfg.isEnableServerSNI() ) {
            params.setSNIMatchers(cfg.getSniMatchers());
        }

        return cfg.adaptParameters(params);
    }


    /**
     * @param hostnameVerifier
     * @param usableCiphers
     * @param usableProtocols
     * @return
     */
    SSLParameters createSSLParameters ( HostnameVerifier hostnameVerifier, String[] usableCiphers, String[] usableProtocols ) {
        if ( hostnameVerifier instanceof ExtendedHostnameVerifier ) {
            ExtendedHostnameVerifier ehv = (ExtendedHostnameVerifier) hostnameVerifier;
            if ( ehv.isBypassBuiltinChecks() ) {
                return new NoEndpointIdenficationSSLParameters(usableCiphers, usableProtocols);
            }
        }
        return new SSLParameters(usableCiphers, usableProtocols);
    }
}
