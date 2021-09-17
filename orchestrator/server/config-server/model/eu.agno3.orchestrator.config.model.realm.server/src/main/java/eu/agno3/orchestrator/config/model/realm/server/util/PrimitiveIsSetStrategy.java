/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.util;


import java.lang.reflect.Method;
import java.util.Map;

import eu.agno3.orchestrator.config.model.realm.EmptyCheckableObject;


/**
 * @author mbechler
 * 
 */
public class PrimitiveIsSetStrategy implements IsSetStrategy {

    /**
     * 
     */
    public PrimitiveIsSetStrategy () {}


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.server.util.IsSetStrategy#isSet(java.lang.Object,
     *      java.lang.reflect.Method, java.lang.Object)
     */
    @Override
    public boolean isSet ( Object value, Method m, Object obj ) {
        return value != null && !isEmptyValue(value) && !isEmptyMap(value);
    }


    /**
     * @param value
     * @return whether this is a empty map
     */
    private static boolean isEmptyMap ( Object value ) {
        if ( value instanceof Map ) {
            return ( (Map<?, ?>) value ).isEmpty();
        }
        return false;
    }


    /**
     * @param value
     * @return whether this is an empty value
     */
    private static boolean isEmptyValue ( Object value ) {
        if ( value instanceof EmptyCheckableObject ) {
            return ( (EmptyCheckableObject) value ).isEmpty();
        }
        return false;
    }
}