/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.04.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.realms.units;


import org.apache.commons.lang3.StringUtils;

import eu.agno3.orchestrator.agent.realms.RealmsManager;
import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.NoSuchServiceException;
import eu.agno3.orchestrator.system.base.execution.Result;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidUnitConfigurationException;
import eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit;
import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.net.krb5.RealmType;


/**
 * @author mbechler
 * @param <TResult>
 * @param <TExecutionUnit>
 * @param <TConfigurator>
 *
 */
public abstract class RealmExecutionUnit <TResult extends Result, TExecutionUnit extends RealmExecutionUnit<TResult, TExecutionUnit, TConfigurator>, TConfigurator extends RealmConfigurator<TResult, TExecutionUnit, TConfigurator>>
        extends AbstractExecutionUnit<TResult, TExecutionUnit, TConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = -1566074131895289636L;
    protected String realmName;
    protected RealmType realmType;


    /**
     * @param context
     * @return
     * @throws ExecutionException
     */
    protected static RealmsManager getRealmsManager ( Context context ) throws ExecutionException {
        try {
            return context.getConfig().getService(RealmsManager.class);
        }
        catch ( NoSuchServiceException e ) {
            throw new ExecutionException("RealmsManager not available", e); //$NON-NLS-1$
        }
    }


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
    protected void setRealmName ( String realmName ) {
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
    protected void setRealmType ( RealmType realmType ) {
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

    }


    /**
     * @param context
     * @return
     * @throws ExecutionException
     * @throws KerberosException
     */
    protected boolean checkRealmExists ( Context context ) throws ExecutionException, KerberosException {
        if ( getRealmsManager(context).exists(this.realmName) ) {
            return true;
        }
        else if ( getRealmsManager(context).exists(this.realmName) ) {
            throw new ExecutionException("Realm exists but is handled by a different manager"); //$NON-NLS-1$
        }

        return false;
    }


    /**
     * 
     */
    public RealmExecutionUnit () {
        super();
    }

}