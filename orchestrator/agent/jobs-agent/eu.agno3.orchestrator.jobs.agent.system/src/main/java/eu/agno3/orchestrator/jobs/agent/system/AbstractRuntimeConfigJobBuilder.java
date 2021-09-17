/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.07.2015 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.system;


import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.jobs.ConfigurationJob;
import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReferenceImpl;
import eu.agno3.orchestrator.jobs.agent.service.RuntimeServiceManager;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.cfgfiles.ConfigFilesManagerFactory;


/**
 * @author mbechler
 * @param <TConfig>
 * @param <TJob>
 *
 */
public abstract class AbstractRuntimeConfigJobBuilder <TConfig extends ConfigurationInstance, TJob extends ConfigurationJob>
        extends AbstractConfigJobBuilder<TConfig, TJob> {

    private ConfigFilesManagerFactory configFilesManagerFactory;


    @Reference
    protected synchronized void setConfigFilesManagerFactory ( ConfigFilesManagerFactory cfmf ) {
        this.configFilesManagerFactory = cfmf;
    }


    protected synchronized void unsetConfigFilesManagerFactory ( ConfigFilesManagerFactory cfmf ) {
        if ( this.configFilesManagerFactory == cfmf ) {
            this.configFilesManagerFactory = null;
        }
    }


    /**
     * @param ctx
     * @throws JobBuilderException
     * @throws Exception
     */
    @Override
    protected void buildConfigJob ( @NonNull JobBuilder b, @NonNull ConfigurationJobContext<TConfig, TJob> ctx ) throws Exception {

        RuntimeServiceManager rsm;
        if ( ctx.job().isBootstrapping() ) {
            rsm = this.getServiceManager().getSingletonServiceManager(getServiceType(), RuntimeServiceManager.class);
        }
        else {
            rsm = this.getServiceManager()
                    .getServiceManager(StructuralObjectReferenceImpl.fromObject(ctx.job().getService()), RuntimeServiceManager.class);
        }
        if ( rsm == null ) {
            throw new JobBuilderException("Failed to get service manager"); //$NON-NLS-1$
        }

        ConfigFilesManagerFactory cfmf = this.configFilesManagerFactory;
        if ( cfmf == null ) {
            throw new JobBuilderException("Config files manager factory"); //$NON-NLS-1$
        }
        buildConfigJob(b, new RuntimeConfigContext<>(rsm, cfmf, b, ctx, this.getConfig(), ctx.job().isBootstrapping()));
    }


    /**
     * @param b
     * @param rtCtx
     * @throws Exception
     */
    protected abstract void buildConfigJob ( @NonNull JobBuilder b, @NonNull RuntimeConfigContext<TConfig, TJob> rtCtx ) throws Exception;
}
