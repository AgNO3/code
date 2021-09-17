/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.10.2014 by mbechler
 */
package eu.agno3.runtime.crypto.truststore.internal;


import java.security.KeyStore;
import java.security.Provider;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.TrustManagerFactorySpi;

import org.osgi.service.component.annotations.Component;


/**
 * @author mbechler
 *
 */
@Component ( service = TrustManagerFactory.class, property = "instanceId=noVerify" )
public class NoVerifyTrustManagerFactory extends TrustManagerFactory {

    /**
     * 
     */
    public NoVerifyTrustManagerFactory () {
        super(new NoVerifyTrustManagerSpi(), new Provider(NoVerifyTrustManagerFactory.class.getName(), 0.1, "") { //$NON-NLS-1$

                private static final long serialVersionUID = -2828701184126892635L;
            },
            "NoVerify"); //$NON-NLS-1$
    }

    private static final class NoVerifyTrustManagerSpi extends TrustManagerFactorySpi {

        /**
         * 
         */
        NoVerifyTrustManagerSpi () {}


        /**
         * {@inheritDoc}
         *
         * @see javax.net.ssl.TrustManagerFactorySpi#engineGetTrustManagers()
         */
        @Override
        protected TrustManager[] engineGetTrustManagers () {
            return new TrustManager[] {
                new NoVerifyX509TrustManager()
            };
        }


        /**
         * {@inheritDoc}
         *
         * @see javax.net.ssl.TrustManagerFactorySpi#engineInit(java.security.KeyStore)
         */
        @Override
        protected void engineInit ( KeyStore ks ) {}


        /**
         * {@inheritDoc}
         *
         * @see javax.net.ssl.TrustManagerFactorySpi#engineInit(javax.net.ssl.ManagerFactoryParameters)
         */
        @Override
        protected void engineInit ( ManagerFactoryParameters spec ) {}

    }

}
