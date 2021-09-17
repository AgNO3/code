/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.10.2014 by mbechler
 */
package eu.agno3.runtime.crypto.keystore.internal;


import java.security.KeyStore;
import java.security.Provider;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.KeyManagerFactorySpi;
import javax.net.ssl.ManagerFactoryParameters;


/**
 * @author mbechler
 *
 */
public class NoKeyManagerFactory extends KeyManagerFactory {

    /**
     * 
     */
    public NoKeyManagerFactory () {
        super(new NoKeyManagerFactorySpi(), new Provider(NoKeyManagerFactory.class.getName(), 0.1, "") { //$NON-NLS-1$

            private static final long serialVersionUID = 2117744701439659520L;
        }, "NoKey"); //$NON-NLS-1$
    }

    private static final class NoKeyManagerFactorySpi extends KeyManagerFactorySpi {

        /**
         * 
         */
        NoKeyManagerFactorySpi () {}


        /**
         * {@inheritDoc}
         *
         * @see javax.net.ssl.KeyManagerFactorySpi#engineGetKeyManagers()
         */
        @Override
        protected KeyManager[] engineGetKeyManagers () {
            return new KeyManager[] {};
        }


        /**
         * {@inheritDoc}
         *
         * @see javax.net.ssl.KeyManagerFactorySpi#engineInit(javax.net.ssl.ManagerFactoryParameters)
         */
        @Override
        protected void engineInit ( ManagerFactoryParameters spec ) {}


        /**
         * {@inheritDoc}
         *
         * @see javax.net.ssl.KeyManagerFactorySpi#engineInit(java.security.KeyStore, char[])
         */
        @Override
        protected void engineInit ( KeyStore ks, char[] password ) {}

    }

}
