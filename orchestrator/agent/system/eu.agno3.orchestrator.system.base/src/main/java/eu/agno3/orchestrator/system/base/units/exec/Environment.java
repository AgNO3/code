/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.exec;


import java.io.Serializable;
import java.util.Map;


/**
 * @author mbechler
 * 
 */
public interface Environment extends Serializable {

    /**
     * @return the process environment as a map
     */
    Map<String, String> getEnv ();

}
