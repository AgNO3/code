/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution;


/**
 * @author mbechler
 * 
 */
public interface RunnerFactory {

    /**
     * Create a runner instance
     * 
     * @return a new runner instance
     */
    Runner createRunner ();

}