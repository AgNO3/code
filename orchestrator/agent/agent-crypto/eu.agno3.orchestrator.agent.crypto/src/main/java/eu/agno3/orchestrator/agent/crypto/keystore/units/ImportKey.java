/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.12.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.keystore.units;


import java.security.KeyPair;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.orchestrator.agent.crypto.keystore.KeystoreManager;
import eu.agno3.orchestrator.agent.crypto.keystore.KeystoresManager;
import eu.agno3.orchestrator.agent.realms.KeyStoreEntry;
import eu.agno3.orchestrator.agent.realms.RSAKeyEntry;
import eu.agno3.orchestrator.crypto.keystore.KeystoreManagerException;
import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.Status;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidUnitConfigurationException;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class ImportKey extends AbstractKeyStoreExecutionUnit<StatusOnlyResult, ImportKey, ImportKeyConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = -170147808657387949L;

    private String alias;
    private KeyPair keyPair;
    private X509Certificate[] chain;

    private transient boolean updateKey;
    private transient boolean updateChain;
    private transient boolean changesDetermined;


    /**
     * @return the alias
     */
    public String getAlias () {
        return this.alias;
    }


    /**
     * @param alias
     *            the alias to set
     */
    void setAlias ( String alias ) {
        this.alias = alias;
    }


    /**
     * @return the keyPair
     */
    public KeyPair getKeyPair () {
        return this.keyPair;
    }


    /**
     * @param keyPair
     *            the keyPair to set
     */
    void setKeyPair ( KeyPair keyPair ) {
        this.keyPair = keyPair;
    }


    /**
     * @return the chain
     */
    public X509Certificate[] getChain () {
        if ( this.chain != null ) {
            return Arrays.copyOf(this.chain, this.chain.length);
        }
        return null;
    }


    /**
     * @param chain
     *            the chain to set
     */
    void setChain ( X509Certificate[] chain ) {
        if ( chain != null ) {
            this.chain = Arrays.copyOf(chain, chain.length);
        }
        else {
            this.chain = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.keystore.units.AbstractKeyStoreExecutionUnit#validate(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void validate ( Context context ) throws ExecutionException {
        super.validate(context);

        if ( StringUtils.isBlank(this.alias) ) {
            throw new InvalidUnitConfigurationException("Key alias is required"); //$NON-NLS-1$
        }

        RSAPublicKey newPublic = (RSAPublicKey) this.keyPair.getPublic();
        if ( this.keyPair == null || this.keyPair.getPrivate() == null || newPublic == null ) {
            throw new InvalidUnitConfigurationException("Key pair is required"); //$NON-NLS-1$
        }

        if ( this.chain != null && this.chain.length > 0 ) {
            X509Certificate eeCert = this.chain[ 0 ];
            RSAPublicKey chainPublic = (RSAPublicKey) eeCert.getPublicKey();
            if ( newPublic.getModulus().compareTo(chainPublic.getModulus()) != 0
                    || newPublic.getPublicExponent().compareTo(chainPublic.getPublicExponent()) != 0 ) {
                throw new InvalidUnitConfigurationException("Mismatch between end user cert and key pair"); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#prepare(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public StatusOnlyResult prepare ( Context context ) throws ExecutionException {

        try {
            KeystoresManager keystoresManager = getKeystoresManager(context);
            if ( !keystoresManager.hasKeyStore(getKeystoreName()) ) {
                this.updateKey = true;
                this.updateChain = true;
                return new StatusOnlyResult(Status.SKIPPED);
            }

            try ( KeystoreManager keyStoreManager = keystoresManager.getKeyStoreManager(this.getKeystoreName()) ) {
                determineChanges(keyStoreManager);
                return new StatusOnlyResult(Status.SUCCESS);
            }
        }
        catch ( KeystoreManagerException e ) {
            throw new ExecutionException("Failed to get keystore manager", e); //$NON-NLS-1$
        }

    }


    /**
     * @param keyStoreManager
     * @throws ExecutionException
     */
    private void determineChanges ( KeystoreManager keyStoreManager ) throws ExecutionException {
        try {
            KeyStoreEntry entry = keyStoreManager.getEntry(getAlias());
            if ( entry == null ) {
                this.updateKey = true;
                this.updateChain = true;

            }
            if ( entry != null && this.keyPair != null && !pubkeysEqual(this.keyPair.getPublic(), entry) ) {
                this.updateKey = true;
            }
            if ( entry != null && !certificateChainMatches(entry) ) {
                this.updateChain = true;
            }

            this.changesDetermined = true;
        }
        catch ( KeystoreManagerException e ) {
            throw new ExecutionException("Failed to get existing key", e); //$NON-NLS-1$
        }
    }


    /**
     * @param public1
     * @param entry
     * @return
     */
    private static boolean pubkeysEqual ( PublicKey pub, KeyStoreEntry entry ) {
        if ( entry instanceof RSAKeyEntry && pub instanceof RSAPublicKey ) {
            return ( (RSAKeyEntry) entry ).getPublicKey().equals(pub);
        }
        return false;
    }


    /**
     * @param entry
     * @return
     */
    private boolean certificateChainMatches ( KeyStoreEntry entry ) {
        Certificate[] echain = entry.getCertificateChain();
        return isNullOrEmpty(this.chain) && isNullOrEmpty(echain)
                || ( !isNullOrEmpty(this.chain) && !isNullOrEmpty(echain) && Arrays.equals(this.chain, echain) );
    }


    private static boolean isNullOrEmpty ( Certificate[] chain ) {
        return chain == null || chain.length == 0;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#execute(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public StatusOnlyResult execute ( Context context ) throws ExecutionException {
        try {
            KeystoresManager keystoresManager = getKeystoresManager(context);
            try ( KeystoreManager keyStoreManager = keystoresManager.getKeyStoreManager(this.getKeystoreName()) ) {

                if ( !this.changesDetermined ) {
                    determineChanges(keyStoreManager);
                }

                if ( this.updateKey ) {
                    try {
                        if ( keyStoreManager.getEntry(getAlias()) != null ) {
                            context.getOutput().info(String.format("Removing old key %s/%s", getKeystoreName(), getAlias())); //$NON-NLS-1$
                            keyStoreManager.deleteKey(getAlias());
                        }
                        context.getOutput().info(String.format("Adding new key %s/%s", getKeystoreName(), getAlias())); //$NON-NLS-1$
                        keyStoreManager.importKey(getAlias(), this.keyPair, this.chain);
                    }
                    catch ( KeystoreManagerException e ) {
                        throw new ExecutionException(String.format("Failed to import key  %s/%s", getKeystoreName(), getAlias()), e); //$NON-NLS-1$
                    }
                }
                else if ( this.updateChain ) {
                    try {
                        context.getOutput().info(String.format("Updating certificate chain %s/%s", getKeystoreName(), getAlias())); //$NON-NLS-1$
                        keyStoreManager.updateCertificateChain(getAlias(), this.chain);
                    }
                    catch ( KeystoreManagerException e ) {
                        throw new ExecutionException(String.format("Failed to update certificate chain %s/%s", getKeystoreName(), getAlias()), e); //$NON-NLS-1$
                    }
                }
                return new StatusOnlyResult(Status.SUCCESS);
            }
        }
        catch ( KeystoreManagerException e ) {
            if ( context.getConfig().isDryRun() ) {
                context.getOutput().error("Ignoring keystore import error", e); //$NON-NLS-1$
                return new StatusOnlyResult(Status.SKIPPED);
            }
            throw new ExecutionException("Failed to get keystore manager", e); //$NON-NLS-1$
        }

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public ImportKeyConfigurator createConfigurator () {
        return new ImportKeyConfigurator(this);
    }

}
