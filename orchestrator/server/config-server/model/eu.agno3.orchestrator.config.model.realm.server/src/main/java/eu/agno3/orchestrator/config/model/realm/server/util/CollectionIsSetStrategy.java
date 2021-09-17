/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.util;


import java.lang.reflect.Method;
import java.util.Collection;


/**
 * @author mbechler
 * 
 */
public class CollectionIsSetStrategy implements IsSetStrategy {

    /**
     * 
     */
    public CollectionIsSetStrategy () {}


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.server.util.IsSetStrategy#isSet(java.lang.Object,
     *      java.lang.reflect.Method, java.lang.Object)
     */
    @Override
    public boolean isSet ( Object value, Method m, Object obj ) {

        if ( value == null ) {
            return false;
        }

        if ( ! ( value instanceof Collection ) ) {
            throw new IllegalArgumentException("Can only handle collections"); //$NON-NLS-1$
        }

        return ! ( (Collection<?>) value ).isEmpty();
    }

}