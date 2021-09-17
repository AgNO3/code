/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.10.2014 by mbechler
 */
package eu.agno3.runtime.security.web;


import org.apache.shiro.web.env.WebEnvironment;


/**
 * @author mbechler
 *
 */
public interface SecurityInitializer {

    /**
     * @param environment
     */
    void initWebEnvironment ( WebEnvironment environment );

}
