/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.11.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.update;


import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.jobs.agent.system.JobBuilderException;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.update.UpdateDescriptor;


/**
 * @author mbechler
 *
 */
public interface UpdateJobFactory {

    /**
     * @param b
     * @param allowReboot
     * @param stream
     * @param descriptor
     * @throws JobBuilderException
     */
    public void buildJob ( @NonNull JobBuilder b, boolean allowReboot, String stream, UpdateDescriptor descriptor ) throws JobBuilderException;


    /**
     * @param updateDescriptor
     * @return extra unit classloaders for service reconfiguration
     */
    public Collection<ClassLoader> getReconfigurationClassLoaders ( UpdateDescriptor updateDescriptor );
}
