/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.02.2015 by mbechler
 */
package eu.agno3.runtime.crypto.random.internal;


import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.crypto.CryptoRuntimeException;
import eu.agno3.runtime.crypto.random.SecureRandomProvider;
import eu.agno3.runtime.util.config.ConfigUtil;
import eu.agno3.runtime.util.test.TestUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = SecureRandomProvider.class, configurationPid = "csprng" )
public class SecureRandomProviderImpl implements SecureRandomProvider {

    /**
     * 
     */
    private static final String NATIVE_PRNG_BLOCKING = "NativePRNGBlocking"; //$NON-NLS-1$
    private static final String NATIVE_PRNG_NON_BLOCKING = "NativePRNGNonBlocking"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(SecureRandomProviderImpl.class);

    /**
     * 
     */
    private static final String SHA1PRNG = "SHA1PRNG"; //$NON-NLS-1$
    private static final long SEED_TIMEOUT = 30 * 1000;

    private SecureRandom seedGenerator;
    private int seedSize = 20;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) throws NoSuchAlgorithmException {
        boolean nonBlocking = ConfigUtil.parseBoolean(ctx.getProperties(), "initNonBlocking", true); //$NON-NLS-1$
        boolean useBlockingSeed = nonBlocking || !TestUtil.isUnderTest();
        if ( log.isInfoEnabled() ) {
            log.info(String.format("Seeding CSPRNG (blocking: %s)", useBlockingSeed)); //$NON-NLS-1$
        }
        long start = System.currentTimeMillis();
        SecureRandom sg = SecureRandom.getInstance(SHA1PRNG);
        if ( useBlockingSeed ) {
            SecureRandom initialSeedGenerator = SecureRandom.getInstance(NATIVE_PRNG_BLOCKING);
            Thread seedThread = new Thread(new SeedingRunnable(initialSeedGenerator, sg, this.seedSize), "CSPRNG seed thread"); //$NON-NLS-1$

            seedThread.start();

            try {
                seedThread.join(SEED_TIMEOUT);
                if ( seedThread.isAlive() ) {
                    log.error("CSPRNG seeding takes long, the system seems to not have enough entropy available, waiting until completed..."); //$NON-NLS-1$
                    seedThread.join();
                }

            }
            catch ( InterruptedException e ) {
                log.warn("Interrupted waiting for seed thread", e); //$NON-NLS-1$
                return;
            }

        }
        else {
            SecureRandom initialSeedGenerator = SecureRandom.getInstance(NATIVE_PRNG_NON_BLOCKING);
            sg.setSeed(this.generateSeed(initialSeedGenerator));
        }

        if ( log.isInfoEnabled() ) {
            log.info(String.format("Seeded CSPRNG, took %.2f s", ( System.currentTimeMillis() - start ) / 1000.0f)); //$NON-NLS-1$
        }
        this.seedGenerator = sg;
    }


    /**
     * {@inheritDoc}
     * 
     *
     * @see eu.agno3.runtime.crypto.random.SecureRandomProvider#getSecureRandom()
     */
    @Override
    public SecureRandom getSecureRandom () {
        try {
            SecureRandom instance = SecureRandom.getInstance(NATIVE_PRNG_NON_BLOCKING);
            SecureRandom sg = this.seedGenerator;

            if ( sg == null ) {
                throw new CryptoRuntimeException("Seed generator is not properly initialized"); //$NON-NLS-1$
            }

            instance.setSeed(generateSeed(sg));
            return instance;
        }
        catch ( NoSuchAlgorithmException e ) {
            throw new CryptoRuntimeException("Incompatible platform", e); //$NON-NLS-1$
        }

    }


    /**
     * @param sg
     * @return
     */
    private byte[] generateSeed ( SecureRandom sg ) {
        byte[] seed = new byte[this.seedSize];
        sg.nextBytes(seed);
        return seed;
    }

    private static class SeedingRunnable implements Runnable {

        private SecureRandom seedGenerator;
        private SecureRandom initialSeedGenerator;
        private int seedSize;


        /**
         * @param initialSeedGenerator
         * @param seedGenerator
         * @param seedSize
         */
        public SeedingRunnable ( SecureRandom initialSeedGenerator, SecureRandom seedGenerator, int seedSize ) {
            this.initialSeedGenerator = initialSeedGenerator;
            this.seedGenerator = seedGenerator;
            this.seedSize = seedSize;
        }


        /**
         * {@inheritDoc}
         *
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run () {
            this.seedGenerator.setSeed(this.initialSeedGenerator.generateSeed(this.seedSize));
        }

    }

}
