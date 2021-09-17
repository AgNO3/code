/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.04.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.realms.units;


import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.attribute.UserPrincipal;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import eu.agno3.orchestrator.agent.realms.RealmManager;
import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.Status;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidUnitConfigurationException;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;
import eu.agno3.runtime.net.krb5.KerberosException;


/**
 * @author mbechler
 *
 */
public class EnsureConfiguredRealm extends RealmExecutionUnit<StatusOnlyResult, EnsureConfiguredRealm, EnsureRealmConfiguredConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = -2503958741115495118L;

    private Properties properties;

    private List<String> defaultAllowedUsers;


    /**
     * @return the properties
     */
    public Properties getProperties () {
        return this.properties;
    }


    /**
     * @param properties
     *            the properties to set
     */
    void setProperties ( Properties properties ) {
        this.properties = properties;
    }


    /**
     * @param users
     */
    void setDefaultAllowedUsers ( List<String> users ) {
        this.defaultAllowedUsers = users;
    }


    /**
     * @return the defaultAllowedUsers
     */
    public List<String> getDefaultAllowedUsers () {
        return this.defaultAllowedUsers;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.units.RealmExecutionUnit#validate(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void validate ( Context context ) throws ExecutionException {
        super.validate(context);
        if ( this.getRealmType() == null ) {
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
     * @return
     */
    private Set<UserPrincipal> makeDefaultAllowedUsers ( Context context ) {
        Set<UserPrincipal> princs = new HashSet<>();
        List<String> allowed = getDefaultAllowedUsers();
        if ( allowed != null ) {
            for ( String allow : allowed ) {
                try {
                    princs.add(FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByName(allow));
                }
                catch ( IOException e ) {
                    context.getOutput().error("Failed to add allowed user " + allow, e); //$NON-NLS-1$
                }
            }
        }
        return princs;
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
            if ( !checkRealmExists(context) ) {
                realmManager.create(this.properties != null ? this.properties : new Properties(), makeDefaultAllowedUsers(context));
            }
            else if ( this.properties != null ) {
                realmManager.updateConfig(this.properties, makeDefaultAllowedUsers(context));
            }
        }
        catch ( KerberosException e ) {
            throw new ExecutionException("Realm creation/update failed", e); //$NON-NLS-1$
        }

        return new StatusOnlyResult(Status.SUCCESS);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public EnsureRealmConfiguredConfigurator createConfigurator () {
        return new EnsureRealmConfiguredConfigurator(this);
    }

}
