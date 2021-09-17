/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 13, 2017 by mbechler
 */
package eu.agno3.orchestrator.agent.realms.units;


import eu.agno3.orchestrator.agent.realms.ADRealmManager;
import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.Status;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidUnitConfigurationException;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;
import eu.agno3.runtime.net.ad.ADException;
import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.net.krb5.RealmType;


/**
 * @author mbechler
 *
 */
public class RekeyAD extends RealmExecutionUnit<StatusOnlyResult, RekeyAD, RekeyADConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = 8187940563552122335L;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.units.RealmExecutionUnit#validate(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void validate ( Context context ) throws ExecutionException {
        super.validate(context);

        if ( getRealmType() != RealmType.AD ) {
            throw new InvalidUnitConfigurationException("Only valid for AD realms"); //$NON-NLS-1$
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
        try {
            if ( !checkRealmExists(context) ) {
                throw new ExecutionException("Realm does not exist " + this.getRealmName()); //$NON-NLS-1$
            }
            ADRealmManager realmManager = (ADRealmManager) getRealmsManager(context).getRealmManager(this.realmName, RealmType.AD);

            if ( !realmManager.isJoined() ) {
                context.getOutput().info("Cannot rekey, not joined to domain " + this.realmName); //$NON-NLS-1$
                return new StatusOnlyResult(Status.SKIPPED);
            }

            realmManager.rekey();
        }
        catch (
            KerberosException |
            ADException e ) {
            throw new ExecutionException("Failed to rekey machine account in domain " + getRealmName(), e); //$NON-NLS-1$
        }

        return new StatusOnlyResult(Status.SUCCESS);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public RekeyADConfigurator createConfigurator () {
        return new RekeyADConfigurator(this);
    }

}
