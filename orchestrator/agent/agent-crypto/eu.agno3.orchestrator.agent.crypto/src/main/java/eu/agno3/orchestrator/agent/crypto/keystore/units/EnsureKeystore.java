/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.12.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.keystore.units;


import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.attribute.UserPrincipal;

import eu.agno3.orchestrator.agent.crypto.keystore.KeystoreManager;
import eu.agno3.orchestrator.agent.crypto.keystore.KeystoresManager;
import eu.agno3.orchestrator.crypto.keystore.KeystoreManagerException;
import eu.agno3.orchestrator.system.account.util.UnixAccountException;
import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.Status;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class EnsureKeystore extends AbstractKeyStoreExecutionUnit<StatusOnlyResult, EnsureKeystore, EnsureKeystoreConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = 4390514366313537716L;

    private transient UserPrincipal user;
    private String userLazy;

    private String validationTrustStore;
    private boolean internal;


    /**
     * @return the user
     * @throws IOException
     */
    public UserPrincipal getUser () throws IOException {
        if ( this.user != null ) {
            return this.user;
        }

        if ( this.userLazy != null ) {
            return FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByName(this.userLazy);
        }

        return null;
    }


    /**
     * @param user
     *            the user to set
     */
    void setUser ( UserPrincipal user ) {
        this.user = user;
    }


    /**
     * @param userLazy
     *            the userLazy to set
     */
    void setUserLazy ( String userLazy ) {
        this.userLazy = userLazy;
    }


    /**
     * @return the validationTrustStore
     */
    public String getValidationTrustStore () {
        return this.validationTrustStore;
    }


    /**
     * @param validationTrustStore
     */
    void setValidationTruststore ( String validationTrustStore ) {
        this.validationTrustStore = validationTrustStore;
    }


    /**
     * @param internal
     *            the internal to set
     */
    void setInternal ( boolean internal ) {
        this.internal = internal;
    }


    /**
     * @return the internal
     */
    public boolean isInternal () {
        return this.internal;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#prepare(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public StatusOnlyResult prepare ( Context context ) throws ExecutionException {
        return new StatusOnlyResult(Status.SUCCESS);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#execute(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public StatusOnlyResult execute ( Context context ) throws ExecutionException {
        KeystoresManager keystoresManager = getKeystoresManager(context);
        boolean dryRun = ( this.user == null && this.userLazy == null ) || context.getConfig().isDryRun();
        if ( !dryRun && !keystoresManager.hasKeyStore(getKeystoreName()) ) {
            context.getOutput().info("Creating keystore " + getKeystoreName()); //$NON-NLS-1$

            try {
                keystoresManager.createKeyStore(this.getKeystoreName(), this.internal);
            }
            catch ( KeystoreManagerException e ) {
                throw new ExecutionException("Failed to create keystore", e); //$NON-NLS-1$
            }
            this.ensureConfig(context, keystoresManager, dryRun);
            return new StatusOnlyResult(Status.SUCCESS);
        }

        this.ensureConfig(context, keystoresManager, dryRun);
        return new StatusOnlyResult(Status.SUCCESS);
    }


    /**
     * @param context
     * @param keystoresManager
     * @param dryRun
     * @throws KeystoreManagerException
     */
    private void ensureConfig ( Context context, KeystoresManager keystoresManager, boolean dryRun ) throws ExecutionException {

        try ( KeystoreManager keyStoreManager = keystoresManager.getKeyStoreManager(this.getKeystoreName()) ) {
            if ( !dryRun && !keyStoreManager.getAllowedUsers().contains(this.getUser()) ) {
                context.getOutput().info(String.format("Allowing keystore access to %s for %s", getKeystoreName(), getUser().getName())); //$NON-NLS-1$
                keyStoreManager.allowUser(this.getUser());
            }

            String curValidationTs = keystoresManager.getValidationTruststoreName(this.getKeystoreName());

            if ( this.validationTrustStore == null && curValidationTs != null ) {
                keyStoreManager.setValidationTruststoreName(null);
            }
            else if ( this.validationTrustStore != null && !this.validationTrustStore.equals(curValidationTs) ) {
                keyStoreManager.setValidationTruststoreName(this.validationTrustStore);
            }
        }
        catch (
            KeystoreManagerException |
            UnixAccountException |
            IOException e ) {
            throw new ExecutionException("Failed to setup keystore config", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public EnsureKeystoreConfigurator createConfigurator () {
        return new EnsureKeystoreConfigurator(this);
    }


    private void writeObject ( ObjectOutputStream oos ) throws IOException {
        if ( this.user != null ) {
            this.userLazy = this.user.getName();
        }

        oos.defaultWriteObject();
    }
}
