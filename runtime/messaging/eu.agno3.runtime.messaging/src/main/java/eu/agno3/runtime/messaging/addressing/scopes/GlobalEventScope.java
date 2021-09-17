/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.addressing.scopes;


import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.messaging.addressing.EventScope;


/**
 * @author mbechler
 * 
 */
@Component ( service = EventScope.class, property = "eventScope=global" )
public class GlobalEventScope implements EventScope {

    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.addressing.EventScope#getParent()
     */
    @Override
    public EventScope getParent () {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.addressing.EventScope#getEventTopic()
     */
    @Override
    public String getEventTopic () {
        return "events-global"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        return this.getClass().hashCode();
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {
        if ( obj instanceof EventScope ) {
            return this.getClass().equals(obj.getClass());
        }
        return false;
    }
}
