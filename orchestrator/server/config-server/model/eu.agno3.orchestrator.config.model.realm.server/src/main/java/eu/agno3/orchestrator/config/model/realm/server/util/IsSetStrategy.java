/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.util;


import java.lang.reflect.Method;


/**
 * 
 * @author mbechler
 * 
 */
public interface IsSetStrategy {

    /**
     * 
     * @param value
     * @param m
     * @param obj
     * @return whether the property is set
     */
    boolean isSet ( Object value, Method m, Object obj );
}