/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.12.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.system.internal;


import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.connector.QueueingEventProducer;
import eu.agno3.orchestrator.config.model.events.ServiceConfigAppliedEvent;
import eu.agno3.orchestrator.config.model.events.ServiceConfigFailedEvent;
import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.jobs.agent.system.ConfigEventProducer;
import eu.agno3.orchestrator.system.base.SystemService;
import eu.agno3.orchestrator.system.base.SystemServiceType;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    ConfigEventProducer.class, SystemService.class
} )
@SystemServiceType ( ConfigEventProducer.class )
public class ConfigEventProducerImpl implements ConfigEventProducer {

    private static final Logger log = Logger.getLogger(ConfigEventProducerImpl.class);

    private QueueingEventProducer eventProducer;


    @Reference
    protected synchronized void setEventProducer ( QueueingEventProducer evp ) {
        this.eventProducer = evp;
    }


    protected synchronized void unsetEventProducer ( QueueingEventProducer evp ) {
        if ( this.eventProducer == evp ) {
            this.eventProducer = null;
        }
    }


    @Override
    public void configApplied ( @Nullable StructuralObject anchor, ServiceStructuralObject service, ConfigurationInstance config ) {
        ServiceConfigAppliedEvent appliedEv = new ServiceConfigAppliedEvent(this.eventProducer.getMessageSource());
        appliedEv.setService(service);
        appliedEv.setAnchor(anchor);
        if ( config.getRevision() == null ) {
            log.warn("Config revision not set"); //$NON-NLS-1$
            return;
        }
        appliedEv.setRevision(config.getRevision());

        if ( log.isDebugEnabled() ) {
            log.debug(String.format(
                "Publishing config applied event for revision %d of service %s", //$NON-NLS-1$
                appliedEv.getRevision(),
                appliedEv.getService()));
        }
        this.eventProducer.publish(appliedEv);
    }


    @Override
    public void configFailed ( @Nullable StructuralObject anchor, ServiceStructuralObject service, ConfigurationInstance config ) {
        ServiceConfigFailedEvent failedEv = new ServiceConfigFailedEvent(this.eventProducer.getMessageSource());
        failedEv.setService(service);
        failedEv.setAnchor(anchor);
        if ( config.getRevision() == null ) {
            log.warn("Config revision not set"); //$NON-NLS-1$
            return;
        }
        failedEv.setRevision(config.getRevision());

        if ( log.isDebugEnabled() ) {
            log.debug(String.format(
                "Publishing config failed event for revision %d of service %s", //$NON-NLS-1$
                failedEv.getRevision(),
                failedEv.getService()));
        }
        this.eventProducer.publish(failedEv);
    }
}
