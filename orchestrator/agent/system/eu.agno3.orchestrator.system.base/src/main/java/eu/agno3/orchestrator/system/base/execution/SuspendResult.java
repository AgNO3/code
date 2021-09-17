/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution;


/**
 * @author mbechler
 *
 */
public interface SuspendResult extends Result {

    /**
     * 
     * @return the job suspend data
     */
    SuspendData getSuspendData ();


    /**
     * @return number of units to execute after the suspending one before actually suspending
     */
    int getSuspendAfter ();
}
