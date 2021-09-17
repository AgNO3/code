/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.10.2014 by mbechler
 */
package eu.agno3.runtime.crypto.truststore.internal;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertSelector;
import java.security.cert.CertStore;
import java.security.cert.CertificateException;
import java.security.cert.PKIXCertPathChecker;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.PKIXParameterFactory;
import eu.agno3.runtime.crypto.tls.TrustConfiguration;
import eu.agno3.runtime.crypto.truststore.AbstractTruststoreConfig;
import eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig;


/**
 * @author mbechler
 *
 */
@Component ( service = TrustConfiguration.class, configurationPolicy = ConfigurationPolicy.REQUIRE, configurationPid = TrustConfiguration.PID )
public class TrustConfigurationImpl extends AbstractTruststoreConfig implements TrustConfiguration {

    private String id;
    private KeyStore ts;
    private boolean checkRevocation;
    private String keyStoreFile;
    private String keyStorePass;


    /**
     * 
     */
    public TrustConfigurationImpl () {}


    /**
     * @param id
     */
    public TrustConfigurationImpl ( String id ) {
        this.id = id;
    }


    @Activate
    @Modified
    protected synchronized void activate ( ComponentContext ctx ) throws CryptoException {
        parseId(ctx);

        String ksFile = (String) ctx.getProperties().get(TrustConfiguration.STORE);
        if ( StringUtils.isBlank(ksFile) ) {
            throw new CryptoException("No key store given for " + this.id); //$NON-NLS-1$
        }
        this.keyStoreFile = ksFile.trim();

        String ksPass = (String) ctx.getProperties().get(TrustConfiguration.STOREPASS);
        if ( StringUtils.isBlank(ksPass) ) {
            throw new CryptoException("No key store password given for " + this.id); //$NON-NLS-1$
        }
        this.keyStorePass = ksPass.trim();

        parseRevocationParams(ctx);
        loadTrustStore();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.AbstractTruststoreConfig#setPKIXParameterFactory(eu.agno3.runtime.crypto.tls.PKIXParameterFactory)
     */
    @Override
    @Reference
    protected synchronized void setPKIXParameterFactory ( PKIXParameterFactory ppf ) {
        super.setPKIXParameterFactory(ppf);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.AbstractTruststoreConfig#unsetPKIXParameterFactory(eu.agno3.runtime.crypto.tls.PKIXParameterFactory)
     */
    @Override
    protected synchronized void unsetPKIXParameterFactory ( PKIXParameterFactory ppf ) {
        super.unsetPKIXParameterFactory(ppf);
    }


    /**
     * @param ctx
     */
    protected void parseRevocationParams ( ComponentContext ctx ) {
        String revocationSpec = (String) ctx.getProperties().get(TrustConfiguration.CHECK_REVOCATION);
        if ( !StringUtils.isBlank(revocationSpec) && Boolean.FALSE.toString().equals(revocationSpec.trim()) ) {
            this.checkRevocation = false;
        }
        else {
            this.checkRevocation = true;
        }
    }


    /**
     * @param ctx
     * @throws CryptoException
     */
    protected void parseId ( ComponentContext ctx ) throws CryptoException {
        String idSpec = (String) ctx.getProperties().get(TrustConfiguration.ID);
        if ( StringUtils.isBlank(idSpec) ) {
            throw new CryptoException("No trust instanceId given"); //$NON-NLS-1$
        }

        this.id = idSpec.trim();
    }


    /**
     * @param keyStoreSpec
     * @param keyStorePassSpec
     * @throws CryptoException
     */
    protected void loadTrustStore () throws CryptoException {
        if ( !StringUtils.isBlank(this.keyStoreFile) ) {
            loadTrustStore(new File(this.keyStoreFile), this.keyStorePass);
        }
    }


    /**
     * @param keyStorePassSpec
     * @throws CryptoException
     */
    protected void loadTrustStore ( File ksFile, String keyStorePassSpec ) throws CryptoException {
        try ( InputStream is = new FileInputStream(ksFile) ) {
            this.ts = KeyStore.getInstance("JKS"); //$NON-NLS-1$
            this.ts.load(is, keyStorePassSpec.trim().toCharArray());
        }
        catch (
            IOException |
            NoSuchAlgorithmException |
            CertificateException |
            KeyStoreException e ) {
            throw new CryptoException("Failed to load trust store", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.truststore.AbstractTruststoreConfig#reload()
     */
    @Override
    protected void reload () throws CryptoException {
        loadTrustStore();
        super.reload();
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TrustConfiguration#getTrustStore()
     */
    @Override
    public KeyStore getTrustStore () {
        return this.ts;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TrustConfiguration#getId()
     */
    @Override
    public String getId () {
        return this.id;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TrustConfiguration#isCheckRevocation()
     */
    @Override
    public boolean isCheckRevocation () {
        return this.checkRevocation;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TrustConfiguration#getConstraint()
     */
    @Override
    public CertSelector getConstraint () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TrustConfiguration#getExtraCertPathCheckers()
     */
    @Override
    public PKIXCertPathChecker[] getExtraCertPathCheckers () {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws CryptoException
     * 
     *
     * @see eu.agno3.runtime.crypto.tls.TrustConfiguration#getExtraCertStores()
     */
    @Override
    public CertStore[] getExtraCertStores () throws CryptoException {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TrustConfiguration#getRevocationConfig()
     */
    @Override
    public RevocationConfig getRevocationConfig () {
        return null;
    }
}
