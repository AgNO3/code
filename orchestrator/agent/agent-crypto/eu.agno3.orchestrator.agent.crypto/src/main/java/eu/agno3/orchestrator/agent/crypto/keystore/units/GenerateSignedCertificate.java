/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.12.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.keystore.units;


import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.joda.time.DateTime;

import eu.agno3.orchestrator.agent.crypto.X509UtilSystemService;
import eu.agno3.orchestrator.agent.crypto.keystore.KeystoreManager;
import eu.agno3.orchestrator.crypto.keystore.KeystoreManagerException;
import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.NoSuchServiceException;
import eu.agno3.orchestrator.system.base.execution.Status;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidUnitConfigurationException;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;
import eu.agno3.runtime.crypto.x509.CertExtension;


/**
 * @author mbechler
 *
 */
public class GenerateSignedCertificate extends
        AbstractKeyStoreExecutionUnit<StatusOnlyResult, GenerateSignedCertificate, GenerateSignedCertificateConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = -6133323184308887152L;
    private String alias;
    private String signingKeystore;

    private PKCS10CertificationRequest csr;
    private Set<CertExtension> extensions = Collections.EMPTY_SET;
    private X500Name subject;
    private DateTime validFrom = DateTime.now();
    private DateTime validTo;
    private String signingAlias;
    private X509Certificate[] extraChain = new X509Certificate[] {};


    /**
     * @return the signingKeystore
     */
    public String getSigningKeystore () {
        if ( this.signingKeystore == null ) {
            return this.getKeystoreName();
        }
        return this.signingKeystore;
    }


    /**
     * @param signingKeystore
     *            the signingKeystore to set
     */
    void setSigningKeystore ( String signingKeystore ) {
        this.signingKeystore = signingKeystore;
    }


    /**
     * @return the key alias used for signing
     */
    public String getSigningAlias () {
        if ( this.signingAlias != null ) {
            return this.signingAlias;
        }
        return this.getAlias();
    }


    /**
     * @param signingAlias
     *            the signingAlias to set
     */
    void setSigningAlias ( String signingAlias ) {
        this.signingAlias = signingAlias;
    }


    /**
     * @return the alias
     */
    public String getAlias () {
        return this.alias;
    }


    /**
     * @param alias
     */
    void setAlias ( String alias ) {
        this.alias = alias;
    }


    /**
     * @return the certificate extensions to add
     */
    public Set<CertExtension> getExtensions () {
        return this.extensions;
    }


    /**
     * @param extensions
     *            the extensions to set
     */
    void setExtensions ( Set<CertExtension> extensions ) {
        this.extensions = extensions;
    }


    /**
     * @return the cert subject
     */
    public X500Name getSubject () {
        return this.subject;
    }


    /**
     * @param subject
     *            the subject to set
     */
    void setSubject ( X500Name subject ) {
        this.subject = subject;
    }


    /**
     * @return the validFrom
     */
    public DateTime getValidFrom () {
        return this.validFrom;
    }


    /**
     * @param validFrom
     *            the validFrom to set
     */
    void setValidFrom ( DateTime validFrom ) {
        this.validFrom = validFrom;
    }


    /**
     * @return the validTo
     */
    public DateTime getValidTo () {
        return this.validTo;
    }


    /**
     * @param validTo
     *            the validTo to set
     */
    void setValidTo ( DateTime validTo ) {
        this.validTo = validTo;
    }


    /**
     * @return the extraChain
     */
    public X509Certificate[] getExtraChain () {
        return this.extraChain;
    }


    /**
     * @param extraChain
     *            the extraChain to set
     */
    void setExtraChain ( X509Certificate[] extraChain ) {
        this.extraChain = extraChain;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.keystore.units.AbstractKeyStoreExecutionUnit#validate(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void validate ( Context context ) throws ExecutionException {
        super.validate(context);

        if ( StringUtils.isBlank(this.getAlias()) ) {
            throw new InvalidUnitConfigurationException("Alias is required"); //$NON-NLS-1$
        }

        if ( this.getSubject() == null ) {
            throw new InvalidUnitConfigurationException("Subject is required"); //$NON-NLS-1$
        }

        getX509Util(context);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#prepare(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public StatusOnlyResult prepare ( Context context ) throws ExecutionException {

        try {
            if ( !getKeystoresManager(context).hasKeyStore(this.getKeystoreName()) ) {
                return new StatusOnlyResult(Status.SKIPPED);
            }
            this.csr = generateCSR(context);
            return new StatusOnlyResult(Status.SUCCESS);
        }
        catch ( KeystoreManagerException e ) {
            throw new ExecutionException("Failed to generate certificate", e); //$NON-NLS-1$
        }

    }


    /**
     * @param context
     * @return
     * @throws KeystoreManagerException
     * @throws ExecutionException
     */
    private PKCS10CertificationRequest generateCSR ( Context context ) throws KeystoreManagerException, ExecutionException {
        try ( KeystoreManager keyStoreManager = getKeystoresManager(context).getKeyStoreManager(this.getKeystoreName()) ) {
            return keyStoreManager.getCSR(this.getAlias(), this.getSubject(), this.getExtensions(), Collections.EMPTY_MAP);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#execute(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public StatusOnlyResult execute ( Context context ) throws ExecutionException {

        if ( this.csr == null ) {
            try {
                this.csr = generateCSR(context);
            }
            catch ( KeystoreManagerException e ) {
                throw new ExecutionException("Failed to generate CSR", e); //$NON-NLS-1$
            }
        }

        context.getOutput().info(String.format("Signing certificate %s:%s using CA %s:%s", //$NON-NLS-1$
            this.getKeystoreName(),
            this.getAlias(),
            this.getSigningKeystore(),
            this.getSigningAlias()));
        try {
            signAndUpdateCertificate(context);
            return new StatusOnlyResult(Status.SUCCESS);
        }
        catch ( KeystoreManagerException e ) {
            throw new ExecutionException("Failed to sign certificate request", e); //$NON-NLS-1$
        }

    }


    /**
     * @param context
     * @throws KeystoreManagerException
     * @throws ExecutionException
     */
    private void signAndUpdateCertificate ( Context context ) throws KeystoreManagerException, ExecutionException {
        try ( KeystoreManager storeKsm = getKeystoresManager(context).getKeyStoreManager(getKeystoreName());
              KeystoreManager signKsm = getSigningKeystoreManager(context) ) {
            X509UtilSystemService x509util = getX509Util(context);
            X509Certificate signed = signKsm.signUsingKey(
                this.getSigningAlias(),
                this.csr,
                x509util.makeRandomSerial(),
                this.validFrom,
                this.validTo,
                this.getExtensions());
            storeKsm.updateCertificateChain(this.getAlias(), makeNewCertificateChain(signed));
        }
    }


    /**
     * @param signed
     * @return
     */
    private X509Certificate[] makeNewCertificateChain ( X509Certificate signed ) {
        X509Certificate[] newChain = new X509Certificate[this.extraChain.length + 1];
        newChain[ 0 ] = signed;
        for ( int i = 0; i < this.extraChain.length; i++ ) {
            newChain[ i + 1 ] = this.extraChain[ i ];
        }
        return newChain;
    }


    /**
     * @param context
     * @return
     * @throws ExecutionException
     * @throws NoSuchServiceException
     */
    private static X509UtilSystemService getX509Util ( Context context ) throws ExecutionException {
        try {
            return context.getConfig().getService(X509UtilSystemService.class);
        }
        catch ( NoSuchServiceException e ) {
            throw new ExecutionException("No x509 util service available", e); //$NON-NLS-1$
        }
    }


    /**
     * @param context
     * @throws ExecutionException
     * @throws KeystoreManagerException
     */
    private KeystoreManager getSigningKeystoreManager ( Context context ) throws KeystoreManagerException, ExecutionException {
        return getKeystoresManager(context).getKeyStoreManager(getSigningKeystore());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public GenerateSignedCertificateConfigurator createConfigurator () {
        return new GenerateSignedCertificateConfigurator(this);
    }

}
