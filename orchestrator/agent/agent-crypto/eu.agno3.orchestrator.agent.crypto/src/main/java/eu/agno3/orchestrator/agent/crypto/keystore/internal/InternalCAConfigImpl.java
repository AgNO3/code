/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.12.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.keystore.internal;


import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.joda.time.Duration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.crypto.InternalCAConfig;
import eu.agno3.orchestrator.agent.crypto.keystore.KeystoreManager;
import eu.agno3.orchestrator.agent.crypto.keystore.KeystoresManager;
import eu.agno3.orchestrator.agent.realms.KeyStoreEntry;
import eu.agno3.orchestrator.crypto.keystore.KeystoreManagerException;
import eu.agno3.runtime.crypto.CryptoException;


/**
 * @author mbechler
 *
 */
@Component ( service = InternalCAConfig.class, configurationPid = InternalCAConfig.PID, configurationPolicy = ConfigurationPolicy.REQUIRE )
public class InternalCAConfigImpl implements InternalCAConfig {

    /**
     * 
     */
    private static final int TWO_YEARS = 2 * 60 * 60 * 24 * 365;

    private static final Logger log = Logger.getLogger(InternalCAConfigImpl.class);

    private static final Set<KeyPurposeId> DEFAULT_ALLOWED_EKUS = new HashSet<>();


    static {
        DEFAULT_ALLOWED_EKUS.add(KeyPurposeId.id_kp_clientAuth);
        DEFAULT_ALLOWED_EKUS.add(KeyPurposeId.id_kp_serverAuth);
        DEFAULT_ALLOWED_EKUS.add(KeyPurposeId.id_kp_scvpClient);
        DEFAULT_ALLOWED_EKUS.add(KeyPurposeId.id_kp_scvpServer);
    }

    private KeystoresManager keystoresManager;

    private String caKeystoreName;
    private String caKeyAlias;

    private Set<KeyPurposeId> allowedEkus = DEFAULT_ALLOWED_EKUS;

    private Duration maximumLifetime = new Duration(TWO_YEARS);

    private X509Certificate[] caChain;
    private X509Certificate caCert;


    @Reference
    protected synchronized void setKeystoresManager ( KeystoresManager ksm ) {
        this.keystoresManager = ksm;
    }


    protected synchronized void unsetKeystoresManager ( KeystoresManager ksm ) {
        if ( this.keystoresManager == ksm ) {
            this.keystoresManager = null;
        }
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) throws KeystoreManagerException {

        String keyStoreSpec = (String) ctx.getProperties().get("keystore"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(keyStoreSpec) ) {
            String ksName = keyStoreSpec.trim();
            if ( !this.keystoresManager.hasKeyStore(ksName) ) {
                log.error("Keystore does not exist " + ksName); //$NON-NLS-1$
                return;
            }
            this.caKeystoreName = ksName;
        }
        else {
            log.error("Keystore name required"); //$NON-NLS-1$
        }

        String keyAliasSpec = (String) ctx.getProperties().get("keyAlias"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(keyAliasSpec) ) {
            String keyAlias = keyAliasSpec.trim();
            try ( KeystoreManager keyStoreManager = this.keystoresManager.getKeyStoreManager(this.caKeystoreName) ) {
                KeyStoreEntry entry = keyStoreManager.getEntry(keyAlias);
                if ( entry == null ) {
                    log.error("Key does not exist " + keyAlias); //$NON-NLS-1$
                    return;
                }
                this.caKeyAlias = keyAlias;
                Certificate[] certificateChain = entry.getCertificateChain();
                this.caCert = (X509Certificate) certificateChain[ 0 ];
                this.caChain = new X509Certificate[certificateChain.length - 1];

                for ( int i = 1; i < certificateChain.length; i++ ) {
                    this.caChain[ i - 1 ] = (X509Certificate) certificateChain[ i ];
                }
            }
        }
        else {
            log.error("No key alias given"); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws CryptoException
     *
     * @see eu.agno3.orchestrator.agent.crypto.InternalCAConfig#getCaKeyAlias()
     */
    @Override
    public String getCaKeyAlias () throws CryptoException {
        if ( this.caKeyAlias == null ) {
            throw new CryptoException("No ca key configured"); //$NON-NLS-1$
        }
        return this.caKeyAlias;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws CryptoException
     *
     * @see eu.agno3.orchestrator.agent.crypto.InternalCAConfig#getCaKeystoreName()
     */
    @Override
    public String getCaKeystoreName () throws CryptoException {
        if ( this.caKeyAlias == null ) {
            throw new CryptoException("No ca keystore configured"); //$NON-NLS-1$
        }
        return this.caKeystoreName;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.InternalCAConfig#getCaCertificate()
     */
    @Override
    public X509Certificate getCaCertificate () {
        return this.caCert;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.InternalCAConfig#getCaExtraChain()
     */
    @Override
    public X509Certificate[] getCaExtraChain () throws CryptoException {
        return Arrays.copyOf(this.caChain, this.caChain.length);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.InternalCAConfig#getKeyUsageMask()
     */
    @Override
    public int getKeyUsageMask () {
        return KeyUsage.dataEncipherment | KeyUsage.decipherOnly | KeyUsage.digitalSignature | KeyUsage.encipherOnly | KeyUsage.keyAgreement
                | KeyUsage.keyEncipherment | KeyUsage.nonRepudiation;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.InternalCAConfig#isExtendedKeyUsageAllowed(org.bouncycastle.asn1.x509.KeyPurposeId)
     */
    @Override
    public boolean isExtendedKeyUsageAllowed ( KeyPurposeId oid ) {
        return this.allowedEkus.contains(oid);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.InternalCAConfig#getMaximumLifetime()
     */
    @Override
    public Duration getMaximumLifetime () {
        return this.maximumLifetime;
    }
}
