/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.04.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.realms.units;


import org.apache.commons.lang3.StringUtils;

import eu.agno3.orchestrator.agent.realms.RealmManager;
import eu.agno3.orchestrator.agent.realms.RealmsManager;
import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.NoSuchServiceException;
import eu.agno3.orchestrator.system.base.execution.Status;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidUnitConfigurationException;
import eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;
import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.net.krb5.RealmType;


/**
 * @author mbechler
 *
 */
public class RemoveRealm extends AbstractExecutionUnit<StatusOnlyResult, RemoveRealm, RemoveRealmConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = -5690348895588694254L;
    private String realmName;
    private RealmType realmType;


    /**
     * @return the realmName
     */
    public String getRealmName () {
        return this.realmName;
    }


    /**
     * @param realmName
     *            the realmName to set
     */
    void setRealmName ( String realmName ) {
        this.realmName = realmName;
    }


    /**
     * @return the realmType
     */
    public RealmType getRealmType () {
        return this.realmType;
    }


    /**
     * @param realmType
     *            the realmType to set
     */
    public void setRealmType ( RealmType realmType ) {
        this.realmType = realmType;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit#validate(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void validate ( Context context ) throws ExecutionException {
        super.validate(context);

        if ( StringUtils.isBlank(this.realmName) ) {
            throw new InvalidUnitConfigurationException("realmName is required"); //$NON-NLS-1$
        }

        if ( this.realmType == null ) {
            throw new InvalidUnitConfigurationException("realmType is required"); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#prepare(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public StatusOnlyResult prepare ( Context context ) throws ExecutionException {
        return new StatusOnlyResult(Status.SKIPPED);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#execute(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public StatusOnlyResult execute ( Context context ) throws ExecutionException {

        try {
            RealmManager realmManager = getRealmsManager(context).getRealmManager(this.realmName, this.realmType);

            if ( !realmManager.exists() ) {
                return new StatusOnlyResult(Status.SKIPPED);
            }

            realmManager.delete();
        }
        catch ( KerberosException e ) {
            throw new ExecutionException("Failed to remove realm", e); //$NON-NLS-1$
        }

        return new StatusOnlyResult(Status.SUCCESS);
    }


    /**
     * @param context
     * @return
     * @throws ExecutionException
     */
    private static RealmsManager getRealmsManager ( Context context ) throws ExecutionException {
        try {
            return context.getConfig().getService(RealmsManager.class);
        }
        catch ( NoSuchServiceException e ) {
            throw new ExecutionException("RealmsManager not available", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public RemoveRealmConfigurator createConfigurator () {
        return new RemoveRealmConfigurator(this);
    }

}
