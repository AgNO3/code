/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.agent;


import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import eu.agno3.orchestrator.config.hostconfig.jobs.SetAdminPasswordJob;
import eu.agno3.orchestrator.jobs.JobType;
import eu.agno3.orchestrator.jobs.agent.system.AbstractSystemJobRunnableFactory;
import eu.agno3.orchestrator.jobs.agent.system.JobBuilderException;
import eu.agno3.orchestrator.jobs.exec.JobRunnableFactory;
import eu.agno3.orchestrator.system.base.execution.ExecutionConfig;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.RunnerFactory;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;
import eu.agno3.orchestrator.system.base.units.exec.Exec;
import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.security.credentials.CredentialType;
import eu.agno3.runtime.security.credentials.CredentialUnwrapper;
import eu.agno3.runtime.security.credentials.UnwrappedCredentials;
import eu.agno3.runtime.security.credentials.UsernamePasswordCredential;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    JobRunnableFactory.class
}, property = "jobType=eu.agno3.orchestrator.config.hostconfig.jobs.SetAdminPasswordJob" )
@JobType ( SetAdminPasswordJob.class )
public class SetAdminPasswordJobBuilder extends AbstractSystemJobRunnableFactory<SetAdminPasswordJob> {

    private static final Logger log = Logger.getLogger(SetAdminPasswordJobBuilder.class);
    private static final String ADMIN_USER = "admin"; //$NON-NLS-1$
    private static final String CHPASSWD = "/usr/sbin/chpasswd"; //$NON-NLS-1$

    private CredentialUnwrapper credentialUnwrapper;


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY )
    protected synchronized void setCredentialUnwrapper ( CredentialUnwrapper cu ) {
        this.credentialUnwrapper = cu;
    }


    protected synchronized void unsetCredentialUnwrapper ( CredentialUnwrapper cu ) {
        if ( this.credentialUnwrapper == cu ) {
            this.credentialUnwrapper = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractSystemJobRunnableFactory#setExecutionConfig(eu.agno3.orchestrator.system.base.execution.ExecutionConfig)
     */
    @Reference
    @Override
    protected void setExecutionConfig ( ExecutionConfig cfg ) {
        super.setExecutionConfig(cfg);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractSystemJobRunnableFactory#unsetExecutionConfig(eu.agno3.orchestrator.system.base.execution.ExecutionConfig)
     */
    @Override
    protected void unsetExecutionConfig ( ExecutionConfig cfg ) {
        super.unsetExecutionConfig(cfg);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractSystemJobRunnableFactory#setRunnerFactory(eu.agno3.orchestrator.system.base.execution.RunnerFactory)
     */
    @Reference
    @Override
    protected void setRunnerFactory ( RunnerFactory factory ) {
        super.setRunnerFactory(factory);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractSystemJobRunnableFactory#unsetRunnerFactory(eu.agno3.orchestrator.system.base.execution.RunnerFactory)
     */
    @Override
    protected void unsetRunnerFactory ( RunnerFactory factory ) {
        super.unsetRunnerFactory(factory);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractSystemJobRunnableFactory#buildJob(eu.agno3.orchestrator.system.base.execution.JobBuilder,
     *      eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    protected void buildJob ( @NonNull JobBuilder b, @NonNull SetAdminPasswordJob j ) throws JobBuilderException {
        try {
            String adminPassword = unwrapPassword(j);
            log.info("Setting local administrator password"); //$NON-NLS-1$
            if ( adminPassword != null ) {
                b.add(Exec.class).cmd(CHPASSWD).stdin(new ChpasswdInputProvider(ADMIN_USER, adminPassword));
            }
        }
        catch ( UnitInitializationFailedException e ) {
            throw new JobBuilderException("Failed to build password change job", e); //$NON-NLS-1$
        }
    }


    /**
     * @param j
     * @return
     * @throws JobBuilderException
     */
    private String unwrapPassword ( @NonNull SetAdminPasswordJob j ) throws JobBuilderException {
        if ( j.getNewCredentials() != null ) {
            CredentialUnwrapper cu = this.credentialUnwrapper;
            if ( cu == null ) {
                throw new JobBuilderException("Credential unwrapper unavailable"); //$NON-NLS-1$
            }
            try {
                UnwrappedCredentials unwrapped = cu.unwrap(j.getNewCredentials());
                if ( unwrapped.getType() != CredentialType.USERNAME_PASSWORD ) {
                    throw new JobBuilderException("Invalid credential type " + unwrapped.getType()); //$NON-NLS-1$
                }
                return ( (UsernamePasswordCredential) unwrapped ).getPassword();
            }
            catch (
                CryptoException |
                IOException e ) {
                throw new JobBuilderException("Failed to unwrap credentials", e); //$NON-NLS-1$
            }
        }
        return j.getAdminPassword();
    }
}
