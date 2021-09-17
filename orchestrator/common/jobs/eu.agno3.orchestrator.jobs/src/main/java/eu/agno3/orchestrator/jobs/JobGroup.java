/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.04.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs;


/**
 * @author mbechler
 * 
 */
public interface JobGroup {

    /**
     * @return whether to check for conflicts in the whole system or only at a specific target
     */
    boolean isCheckGlobalConflicts ();


    /**
     * 
     * @param j1
     * @param j2
     * @return whether the given two jobs cannot run concurrently
     */
    boolean conflicts ( Job j1, Job j2 );


    /**
     * @return an identifier for this group
     */
    String getId ();

}
