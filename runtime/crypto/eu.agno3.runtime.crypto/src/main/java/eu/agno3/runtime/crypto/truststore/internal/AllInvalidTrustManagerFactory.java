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

import eu.agno3.runtime.crypto.truststore.AllInvalidTrustManager;


/**
 * @author mbechler
 *
 */
@Component ( service = TrustManagerFactory.class, property = "instanceId=allInvalid" )
public class AllInvalidTrustManagerFactory extends TrustManagerFactory {

    /**
     * 
     */
    public AllInvalidTrustManagerFactory () {
        this("allInvalid"); //$NON-NLS-1$
    }


    /**
     * @param ts
     * 
     */
    public AllInvalidTrustManagerFactory ( String ts ) {
        super(new AllInvalidTruststManagerSpi(ts), new Provider(AllInvalidTrustManagerFactory.class.getName(), 0.1, "") { //$NON-NLS-1$

            private static final long serialVersionUID = -2828701184126892635L;
        }, "AllInvalid"); //$NON-NLS-1$
    }

    private static final class AllInvalidTruststManagerSpi extends TrustManagerFactorySpi {

        private String trustStore;


        /**
         * 
         */
        AllInvalidTruststManagerSpi ( String ts ) {
            this.trustStore = ts;
        }


        /**
         * {@inheritDoc}
         *
         * @see javax.net.ssl.TrustManagerFactorySpi#engineGetTrustManagers()
         */
        @Override
        protected TrustManager[] engineGetTrustManagers () {
            return new TrustManager[] {
                new AllInvalidTrustManager(this.trustStore)
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
