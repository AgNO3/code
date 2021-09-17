/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.broker.auth;


/**
 * @author mbechler
 * 
 */
public enum DestinationAccess {

    /**
     * Create a destination
     */
    CREATE,

    /**
     * Delete a destination
     */
    DELETE,

    /**
     * Consume from a destination
     */
    CONSUME,

    /**
     * Produce to a destination
     */
    PRODUCE

}
