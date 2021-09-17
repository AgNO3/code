/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.12.2015 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.system;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.model.jobs.ConfigurationJob;
import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;


/**
 * @author mbechler
 * @param <TConfig>
 * @param <TJob>
 *
 */
public interface ConfigJobBuilder <TConfig extends ConfigurationInstance, TJob extends ConfigurationJob> {

    /**
     * @param b
     * @param j
     * @throws JobBuilderException
     */
    void addTo ( @NonNull JobBuilder b, @NonNull TJob j ) throws JobBuilderException;

}
