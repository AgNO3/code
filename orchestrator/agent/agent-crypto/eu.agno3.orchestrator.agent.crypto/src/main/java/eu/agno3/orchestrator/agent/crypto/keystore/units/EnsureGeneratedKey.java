/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.01.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.keystore.units;


import org.apache.commons.lang3.StringUtils;

import eu.agno3.orchestrator.agent.crypto.keystore.KeystoreManager;
import eu.agno3.orchestrator.agent.realms.KeyStoreEntry;
import eu.agno3.orchestrator.crypto.keystore.KeystoreManagerException;
import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.Status;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidUnitConfigurationException;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;
import eu.agno3.runtime.crypto.keystore.KeyType;


/**
 * @author mbechler
 *
 */
public class EnsureGeneratedKey extends AbstractKeyStoreExecutionUnit<StatusOnlyResult, EnsureGeneratedKey, EnsureGeneratedKeyConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = -4692113647239363342L;

    private String keyAlias;

    private KeyType keyType = KeyType.RSA2048;

    // kept for serialization compat
    @SuppressWarnings ( "unused" )
    private String algo = "RSA"; //$NON-NLS-1$
    @SuppressWarnings ( "unused" )
    private int bitSize = 2048;


    /**
     * @return the keyAlias
     */
    public String getKeyAlias () {
        return this.keyAlias;
    }


    /**
     * @param keyAlias
     *            the keyAlias to set
     */
    void setKeyAlias ( String keyAlias ) {
        this.keyAlias = keyAlias;
    }


    /**
     * @return the keyType
     */
    public KeyType getKeyType () {
        return this.keyType;
    }


    /**
     * @param keyType
     *            the keyType to set
     */
    void setKeyType ( KeyType keyType ) {
        this.keyType = keyType;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.keystore.units.AbstractKeyStoreExecutionUnit#validate(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void validate ( Context context ) throws ExecutionException {
        super.validate(context);

        if ( StringUtils.isBlank(this.getKeyAlias()) ) {
            throw new InvalidUnitConfigurationException("KeyAlias is required"); //$NON-NLS-1$
        }
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

        try ( KeystoreManager keyStoreManager = getKeystoresManager(context).getKeyStoreManager(this.getKeystoreName()) ) {
            KeyStoreEntry entry = keyStoreManager.getEntry(this.getKeyAlias());

            if ( entry == null ) {
                keyStoreManager.generateKey(this.getKeyAlias(), this.getKeyType());
            }

        }
        catch ( KeystoreManagerException e ) {
            throw new ExecutionException(String.format(
                "Failed to generate %s in %s", //$NON-NLS-1$
                this.getKeyAlias(),
                this.getKeystoreName()), e);
        }

        return new StatusOnlyResult(Status.SUCCESS);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public EnsureGeneratedKeyConfigurator createConfigurator () {
        return new EnsureGeneratedKeyConfigurator(this);
    }

}
