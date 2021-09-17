/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.12.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.state;


/**
 * @author mbechler
 *
 */
public interface JobStateObservable {

    /**
     * @param l
     */
    void registerStateListener ( JobStateListener l );


    /**
     * @param l
     */
    void unregisterStateListener ( JobStateListener l );
}
