/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.01.2017 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service.impl;


import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.events.ConfigTestResultUpdateEvent;
import eu.agno3.runtime.messaging.listener.EventListener;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    EventListener.class
}, property = "eventType=eu.agno3.orchestrator.config.model.events.ConfigTestResultUpdateEvent" )
public class ConfigTestResultEventListener implements EventListener<ConfigTestResultUpdateEvent> {

    private static final Logger log = Logger.getLogger(ConfigTestResultEventListener.class);

    private ConfigTestResultCache resultCache;


    @Reference
    protected synchronized void setResultCache ( ConfigTestResultCache ctrc ) {
        this.resultCache = ctrc;
    }


    protected synchronized void unsetResultCache ( ConfigTestResultCache ctrc ) {
        if ( this.resultCache == ctrc ) {
            this.resultCache = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.EventListener#onEvent(eu.agno3.runtime.messaging.msg.EventMessage)
     */
    @Override
    public void onEvent ( @NonNull ConfigTestResultUpdateEvent event ) {
        log.debug("Got config test result event"); //$NON-NLS-1$
        this.resultCache.update(event.getSequence(), event.getResult());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.EventListener#getEventType()
     */
    @Override
    public @NonNull Class<ConfigTestResultUpdateEvent> getEventType () {
        return ConfigTestResultUpdateEvent.class;
    }

}
