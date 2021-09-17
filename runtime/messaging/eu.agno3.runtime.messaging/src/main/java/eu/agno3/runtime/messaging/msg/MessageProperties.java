/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.01.2014 by mbechler
 */
package eu.agno3.runtime.messaging.msg;


/**
 * @author mbechler
 * 
 */
public final class MessageProperties {

    private MessageProperties () {}

    /**
     * JMS message property for status (success,error)
     */
    public static final String STATUS = "status"; //$NON-NLS-1$
    /**
     * JMS message property for message type
     */
    public static final String TYPE = "type"; //$NON-NLS-1$
    /**
     * JMS message property for ttl
     */
    public static final String TTL = "ttl"; //$NON-NLS-1$

    /**
     * JMS message property for source
     */
    public static final String SOURCE = "source"; //$NON-NLS-1$

}
