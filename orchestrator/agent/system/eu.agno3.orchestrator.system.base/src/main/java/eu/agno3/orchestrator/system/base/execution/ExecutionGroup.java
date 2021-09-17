/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution;


import java.util.List;


/**
 * @author mbechler
 *
 */
public interface ExecutionGroup extends BaseExecutable {

    /**
     * 
     * @return the units to execute before main
     */
    List<BaseExecutable> getBefore ();


    /**
     * 
     * @return the units to execute
     */
    List<BaseExecutable> getMain ();


    /**
     * 
     * @return the units to execute after main
     */
    List<BaseExecutable> getAfter ();
}
