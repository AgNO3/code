/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.12.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.server;


import eu.agno3.orchestrator.jobs.msg.JobOutputLevel;


/**
 * @author mbechler
 *
 */
public interface JobOutputBuffer {

    /**
     * 
     * @return whether the buffers are complete
     */
    boolean isEof ();


    /**
     * @return the combined output
     */
    String getCombinedOutput ();


    /**
     * @param l
     * @return the level output
     */
    String getLevelOutput ( JobOutputLevel l );


    /**
     * @param offset
     * @return the combined output starting at offset
     */
    String getCombinedOutput ( long offset );


    /**
     * @param l
     * @param offset
     * @return the level output starting at offset
     */
    String getLevelOutput ( JobOutputLevel l, long offset );

}