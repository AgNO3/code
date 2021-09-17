/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 13, 2017 by mbechler
 */
package eu.agno3.orchestrator.agent.realms.units;


import org.apache.commons.lang3.StringUtils;

import eu.agno3.orchestrator.agent.realms.ADRealmManager;
import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.Status;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidUnitConfigurationException;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;
import eu.agno3.runtime.net.ad.ADException;
import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.net.krb5.RealmType;
import eu.agno3.runtime.security.credentials.WrappedCredentials;


/**
 * @author mbechler
 *
 */
public class JoinAD extends RealmExecutionUnit<StatusOnlyResult, JoinAD, JoinADConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = 8187940563552122335L;
    private String user;
    private String password;
    private boolean joinUsingMachinePassword;
    private String machinePassword;
    private boolean force;
    private WrappedCredentials credentials;


    /**
     * @return the user
     */
    public String getUser () {
        return this.user;
    }


    /**
     * @param username
     */
    void setUser ( String username ) {
        this.user = username;
    }


    /**
     * @return the force
     */
    public boolean isForce () {
        return this.force;
    }


    /**
     * @param force
     *            the force to set
     */
    void setForce ( boolean force ) {
        this.force = force;
    }


    /**
     * @return the password
     */
    public String getPassword () {
        return this.password;
    }


    /**
     * @param password
     */
    void setPassword ( String password ) {
        this.password = password;
    }


    /**
     * @return the credentials
     */
    public WrappedCredentials getCredentials () {
        return this.credentials;
    }


    /**
     * @param wrapped
     */
    void setCredentials ( WrappedCredentials wrapped ) {
        this.credentials = wrapped;
    }


    /**
     * @return the joinUsingMachinePassword
     */
    public boolean isJoinUsingMachinePassword () {
        return this.joinUsingMachinePassword;
    }


    /**
     * @param b
     */
    void setJoinUsingMachinePassword ( boolean b ) {
        this.joinUsingMachinePassword = b;
    }


    /**
     * @return the machinePassword
     */
    public String getMachinePassword () {
        return this.machinePassword;
    }


    /**
     * @param machinePassword
     */
    void setMachinePassword ( String machinePassword ) {
        this.machinePassword = machinePassword;
    }


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

        if ( !isJoinUsingMachinePassword() ) {
            if ( getCredentials() == null && ( StringUtils.isBlank(getUser()) || StringUtils.isBlank(getPassword()) ) ) {
                throw new InvalidUnitConfigurationException("Join credentials are required"); //$NON-NLS-1$
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

            if ( !isForce() && realmManager.isJoined() ) {
                context.getOutput().info("Already joined to AD domain " + this.realmName); //$NON-NLS-1$
                return new StatusOnlyResult(Status.SKIPPED);
            }

            context.getOutput().info("Joining AD domain " + this.realmName); //$NON-NLS-1$
            if ( isJoinUsingMachinePassword() ) {
                realmManager.joinDomainWithMachinePassword(getMachinePassword());
            }
            else {

                WrappedCredentials creds = getCredentials();
                if ( creds != null ) {
                    realmManager.joinDomain(creds);
                }
                else {
                    realmManager.joinDomain(getUser(), getPassword());
                }
            }
        }
        catch (
            KerberosException |
            ADException e ) {
            String msg = "Failed to join domain " + getRealmName(); //$NON-NLS-1$
            context.getOutput().error(msg, e);
            throw new ExecutionException(msg, e);
        }

        return new StatusOnlyResult(Status.SUCCESS);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public JoinADConfigurator createConfigurator () {
        return new JoinADConfigurator(this);
    }

}
