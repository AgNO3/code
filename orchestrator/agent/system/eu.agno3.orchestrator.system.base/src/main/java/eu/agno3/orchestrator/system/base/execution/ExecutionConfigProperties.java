/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution;


import java.nio.file.Path;


/**
 * @author mbechler
 * 
 */
public interface ExecutionConfigProperties {

    /**
     * 
     * @return whether this is a dry run, no actual changes shall be made
     */
    boolean isDryRun ();


    /**
     * 
     * @return a prefix for all file operations
     */
    Path getPrefix ();


    /**
     * 
     * @return whether this job should always create the used directories
     */
    boolean isAlwaysCreateTargets ();


    /**
     * Only for testing purposes
     * 
     * @return whether this job should verify it's environment (e.g. check whether executables exist)
     */
    boolean isNoVerifyEnv ();

}
