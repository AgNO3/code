/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 26, 2016 by mbechler
 */
package eu.agno3.orchestrator.config.web.validation.tls.internal;


import java.security.PublicKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.web.PublicKeyPinMode;
import eu.agno3.orchestrator.config.web.SSLClientConfiguration;
import eu.agno3.orchestrator.config.web.validation.SSLEndpointConfigTestFactory;
import eu.agno3.orchestrator.config.web.validation.TLSTestContext;
import eu.agno3.orchestrator.types.entities.crypto.PublicKeyEntry;
import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.TLSContextProvider;
import eu.agno3.runtime.crypto.truststore.AllInvalidTrustManager;
import eu.agno3.runtime.ldap.filter.FilterBuilder;


/**
 * @author mbechler
 *
 */
@Component ( service = SSLEndpointConfigTestFactory.class )
public class SSLEndpointConfigTestFactoryImpl implements SSLEndpointConfigTestFactory {

    private static final Logger log = Logger.getLogger(SSLEndpointConfigTestFactoryImpl.class);

    private TLSContextProvider contextProvider;
    private HostnameVerifier hostnameVerifier;
    private BundleContext bundleContext;

    private TrustManagerFactory globalTrust;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.bundleContext = ctx.getBundleContext();
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        this.bundleContext = null;
    }


    @Reference
    protected synchronized void setTLSContextProvider ( TLSContextProvider tcp ) {
        this.contextProvider = tcp;
    }


    protected synchronized void unsetTLSContextProvider ( TLSContextProvider tcp ) {
        if ( this.contextProvider == tcp ) {
            this.contextProvider = null;
        }
    }


    @Reference ( target = "(instanceId=default)" )
    protected synchronized void setDefaultHostnameVerifier ( HostnameVerifier hv ) {
        this.hostnameVerifier = hv;
    }


    protected synchronized void unsetDefaultHostnameVerifier ( HostnameVerifier hv ) {
        if ( this.hostnameVerifier == hv ) {
            this.hostnameVerifier = null;
        }
    }


    @Reference ( target = "(instanceId=global)" )
    protected synchronized void setGlobalTrustManager ( TrustManagerFactory tmf ) {
        this.globalTrust = tmf;
    }


    protected synchronized void unsetGlobalTrustManager ( TrustManagerFactory tmf ) {
        if ( this.globalTrust == tmf ) {
            this.globalTrust = null;
        }
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws CryptoException
     *
     * @see eu.agno3.orchestrator.config.web.validation.SSLEndpointConfigTestFactory#adaptSSLClient(eu.agno3.orchestrator.config.web.SSLClientConfiguration)
     */
    @Override
    public TLSTestContext adaptSSLClient ( SSLClientConfiguration sec ) throws CryptoException {
        Objects.requireNonNull(sec);
        boolean tmFallback = false;
        Set<PublicKey> pinnedKeys = makePinnedPublicKeys(sec.getPinnedPublicKeys());
        X509TrustManager rtm;
        if ( sec.getPublicKeyPinMode() == PublicKeyPinMode.ADDITIVE ) {
            rtm = getRuntimeTrustManager(sec);

            if ( rtm == null ) {
                log.warn("Matching trust manager not found, falling back to global trust"); //$NON-NLS-1$
                tmFallback = true;
                rtm = getGlobalTrust();
            }
        }
        else {
            rtm = new AllInvalidTrustManager();
        }

        TestingTrustManager tm = new TestingTrustManager(sec, rtm, pinnedKeys);
        TrustManager[] trustManagers = new TrustManager[] {
            tm
        };

        TestingHostnameVerifier hv = new TestingHostnameVerifier(sec, this.hostnameVerifier);

        AdaptedTLSConfiguration cfg = new AdaptedTLSConfiguration(sec, trustManagers, hv, pinnedKeys);

        if ( tmFallback ) {
            return new TLSTestContextImpl(this.contextProvider.getContext(cfg), tm, hv, sec.getTruststoreAlias(), "client"); //$NON-NLS-1$
        }

        return new TLSTestContextImpl(this.contextProvider.getContext(cfg), tm, hv);
    }


    /**
     * @param pinnedPublicKeys
     * @return
     */
    private static Set<PublicKey> makePinnedPublicKeys ( Set<PublicKeyEntry> pinnedPublicKeys ) {
        Set<PublicKey> pinned = new HashSet<>();
        if ( pinnedPublicKeys == null || pinnedPublicKeys.isEmpty() ) {
            return pinned;
        }
        for ( PublicKeyEntry e : pinnedPublicKeys ) {
            pinned.add(e.getPublicKey());
        }
        return pinned;
    }


    /**
     * @return
     */
    private X509TrustManager getGlobalTrust () {
        return createTrustManager(this.globalTrust);
    }


    /**
     * @param sec
     * @return
     */
    private X509TrustManager getRuntimeTrustManager ( SSLClientConfiguration sec ) {
        BundleContext ctx = this.bundleContext;
        if ( ctx != null ) {
            try {
                Collection<ServiceReference<TrustManagerFactory>> refs = ctx
                        .getServiceReferences(TrustManagerFactory.class, FilterBuilder.get().eq("instanceId", sec.getTruststoreAlias()).toString()); //$NON-NLS-1$

                if ( refs.isEmpty() ) {
                    log.debug("No references found"); //$NON-NLS-1$
                    return null;
                }
                else if ( refs.size() > 1 ) {
                    log.debug("Multiple trust manager instances found"); //$NON-NLS-1$
                    return null;
                }

                ServiceReference<TrustManagerFactory> ref = refs.iterator().next();
                TrustManagerFactory service = ctx.getService(ref);
                return createTrustManager(service);
            }
            catch ( InvalidSyntaxException e ) {
                log.error("Failed to lookup trust managers", e); //$NON-NLS-1$
            }
        }
        return null;
    }


    private static X509TrustManager createTrustManager ( TrustManagerFactory service ) {
        if ( service == null ) {
            return null;
        }
        TrustManager[] tms = service.getTrustManagers();

        if ( tms == null || tms.length != 1 ) {
            log.debug("Found trustmanagers cannot be used " + Arrays.toString(tms)); //$NON-NLS-1$
            return null;
        }

        if ( ! ( tms[ 0 ] instanceof X509TrustManager ) ) {
            log.debug("Not an X509 trust manager"); //$NON-NLS-1$
            return null;
        }

        return (X509TrustManager) tms[ 0 ];
    }

}
