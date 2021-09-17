/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.09.2015 by mbechler
 */
package eu.agno3.runtime.messaging.listener;


import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;


/**
 * @author mbechler
 *
 */
public class FixedTopicStrategy implements DestinationStrategy {

    private String eventTopic;


    /**
     * @param eventTopic
     */
    public FixedTopicStrategy ( String eventTopic ) {
        this.eventTopic = eventTopic;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.DestinationStrategy#getDestination(eu.agno3.runtime.messaging.listener.BaseListener,
     *      javax.jms.Session)
     */
    @Override
    public Destination getDestination ( BaseListener listener, Session s ) throws JMSException {
        return s.createTopic(this.eventTopic);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.DestinationStrategy#getDestinationId(eu.agno3.runtime.messaging.listener.BaseListener)
     */
    @Override
    public String getDestinationId ( BaseListener listener ) {
        return "topic://" + this.eventTopic; //$NON-NLS-1$
    }

}
