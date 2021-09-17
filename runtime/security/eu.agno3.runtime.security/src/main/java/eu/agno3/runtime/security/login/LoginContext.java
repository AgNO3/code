/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2015 by mbechler
 */
package eu.agno3.runtime.security.login;


import java.io.Serializable;
import java.util.Map;


/**
 * @author mbechler
 *
 */
public interface LoginContext extends Serializable {

    /**
     * @return the context properties as a map
     */
    Map<? extends String, ? extends Serializable> getProperties ();

}
