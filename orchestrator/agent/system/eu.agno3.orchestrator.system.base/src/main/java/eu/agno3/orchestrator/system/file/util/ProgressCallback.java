/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.file.util;


/**
 * @author mbechler
 * 
 */
public interface ProgressCallback {

    /**
     * Called when to task starts
     * 
     * @param size
     */
    void start ( long size );


    /**
     * Called when progress is achieved
     * 
     * @param pos
     */
    void progress ( long pos );


    /**
     * Called when the task is done
     */
    void finished ();
}
