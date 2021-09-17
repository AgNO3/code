/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.06.2014 by mbechler
 */
package eu.agno3.runtime.messaging.addressing;


/**
 * @author mbechler
 * 
 */
public interface MessageSourceRegistry {

    /**
     * 
     * @param spec
     * @return a message source for the specification
     */
    MessageSource getMessageSource ( String spec );
}
