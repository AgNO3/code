/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.03.2017 by mbechler
 */
package eu.agno3.runtime.jmx;

/**
 * @author mbechler
 *
 */
public interface MethodPermissionMapper {

    /**
     * @param me
     * @return permission required for calling method (instead of CALL)
     */
    JMXPermissions map ( MethodEntry me );

}
