/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.04.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.realms.units;


import java.io.IOException;
import java.nio.file.FileSystems;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.orchestrator.agent.realms.ADRealmManager;
import eu.agno3.orchestrator.agent.realms.KeyTabManager;
import eu.agno3.orchestrator.agent.realms.RealmManager;
import eu.agno3.orchestrator.system.account.util.UnixAccountException;
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
public class EnsureKeytabAccess extends RealmExecutionUnit<StatusOnlyResult, EnsureKeytabAccess, EnsureKeytabAccessConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = -2503958741115495118L;

    private String keytab;
    private String user;


    /**
     * @return the keytab
     */
    public String getKeytab () {
        return this.keytab;
    }


    /**
     * @param keytab
     *            the keytab to set
     */
    void setKeytab ( String keytab ) {
        this.keytab = keytab;
    }


    /**
     * @param username
     */
    void setUser ( String username ) {
        this.user = username;
    }


    /**
     * @return the user
     */
    public String getUser () {
        return this.user;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.units.RealmExecutionUnit#validate(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void validate ( Context context ) throws ExecutionException {
        super.validate(context);

        if ( StringUtils.isBlank(this.keytab) && this.getRealmType() != RealmType.AD ) {
            throw new InvalidUnitConfigurationException("keytab is required"); //$NON-NLS-1$
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
            if ( checkRealmExists(context) ) {
                return new StatusOnlyResult(Status.SKIPPED);
            }

        }
        catch ( KerberosException e ) {
            throw new ExecutionException("Realm check failed", e); //$NON-NLS-1$
        }

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
            RealmManager realmManager = getRealmsManager(context).getRealmManager(this.realmName);
            if ( !checkRealmExists(context) ) {
                throw new ExecutionException("Realm does not exist " + this.getRealmName()); //$NON-NLS-1$
            }

            if ( StringUtils.isBlank(getKeytab()) && realmManager.getType() == RealmType.AD ) {
                // use AD host keytab
                ADRealmManager adrm = (ADRealmManager) realmManager;

                if ( !adrm.isJoined() ) {
                    throw new ExecutionException("Not joined to domain " + this.getRealmName()); //$NON-NLS-1$
                }

                if ( !StringUtils.isBlank(getUser()) ) {
                    adrm.allowHostUser(FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByName(this.getUser()));
                }
            }
            else {
                KeyTabManager ktm = realmManager.getKeytabManager(this.getKeytab());
                if ( !ktm.exists() ) {
                    throw new ExecutionException("Keytab does not exist " + this.getKeytab()); //$NON-NLS-1$
                }

                if ( !StringUtils.isBlank(this.getUser()) ) {
                    ktm.allowUser(FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByName(this.getUser()));
                }
            }
        }
        catch (
            KerberosException |
            ADException |
            UnixAccountException |
            IOException e ) {
            throw new ExecutionException("Keytab check/creation failed", e); //$NON-NLS-1$
        }

        return new StatusOnlyResult(Status.SUCCESS);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public EnsureKeytabAccessConfigurator createConfigurator () {
        return new EnsureKeytabAccessConfigurator(this);
    }

}
