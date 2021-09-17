/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.addressing;


/**
 * @author mbechler
 * 
 */
public final class JMSQueueMessageTarget implements MessageTarget {

    private final String queueName;


    /**
     * @param queueName
     */
    public JMSQueueMessageTarget ( String queueName ) {
        this.queueName = queueName;
    }


    /**
     * @return the queueName
     */
    public String getQueueName () {
        return this.queueName;
    }
}
