/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 28, 2016 by mbechler
 */
package eu.agno3.orchestrator.bootstrap;


import java.util.List;

import javax.persistence.EntityManager;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.hostconfig.HostConfiguration;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.context.ConfigUpdateInfo;
import eu.agno3.orchestrator.jobs.Job;


/**
 * @author mbechler
 *
 */
public interface BootstrapPlugin {

    /**
     * 
     * @param instance
     * @return whether this plugin manages the context
     */
    boolean isPrimary ( InstanceStructuralObject instance );


    /**
     * 
     * @param instance
     * @return whether this plugin contributes to the context
     */
    boolean appliesTo ( InstanceStructuralObject instance );


    /**
     * @param bootstrapInstance
     * @return context for
     */
    BootstrapContext createContext ( InstanceStructuralObject bootstrapInstance );


    /**
     * @param em
     * @param instance
     * @param ctx
     * @throws ModelObjectException
     * @throws ModelServiceException
     */
    void setupContext ( @NonNull EntityManager em, @NonNull InstanceStructuralObjectImpl instance, BootstrapContext ctx )
            throws ModelObjectException, ModelServiceException;;


    /**
     * @param em
     * @param instance
     * @param ctx
     * @param info
     * @throws ModelObjectException
     * @throws ModelServiceException
     */
    void completeContext ( @NonNull EntityManager em, @NonNull InstanceStructuralObjectImpl instance, BootstrapContext ctx, ConfigUpdateInfo info )
            throws ModelObjectException, ModelServiceException;;


    /**
     * @param instance
     * @param em
     * @param ctx
     * @param weights
     * @param jobs
     * @throws ModelObjectException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    void contributeJobs ( @NonNull EntityManager em, @NonNull InstanceStructuralObjectImpl instance, BootstrapContext ctx, List<Float> weights,
            List<Job> jobs ) throws ModelObjectException, ModelServiceException;


    /**
     * @param em
     * @param instance
     * @param hc
     * @throws ModelObjectException
     * @throws ModelServiceException
     */
    void setupServices ( @NonNull EntityManager em, @NonNull InstanceStructuralObjectImpl instance, HostConfiguration hc )
            throws ModelObjectException, ModelServiceException;


    /**
     * @param em
     * @param ctx
     * @throws ModelObjectException
     * @throws ModelServiceException
     */
    void contributeAutoRun ( BootstrapContext ctx ) throws ModelObjectException, ModelServiceException;
}
