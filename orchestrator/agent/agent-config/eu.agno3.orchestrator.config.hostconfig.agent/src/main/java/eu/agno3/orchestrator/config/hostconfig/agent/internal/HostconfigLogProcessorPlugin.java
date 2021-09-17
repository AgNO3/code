/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.02.2016 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.agent.internal;


import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.hostconfig.desc.HostConfigServiceTypeDescriptor;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.jobs.agent.system.ConfigRepository;
import eu.agno3.orchestrator.jobs.agent.system.ConfigRepositoryException;
import eu.agno3.orchestrator.system.logging.LogFields;
import eu.agno3.orchestrator.system.logsink.LogAction;
import eu.agno3.orchestrator.system.logsink.LogProcessorPlugin;
import eu.agno3.orchestrator.system.logsink.ProcessorContext;


/**
 * @author mbechler
 *
 */
@Component ( service = LogProcessorPlugin.class )
public class HostconfigLogProcessorPlugin implements LogProcessorPlugin {

    private static final Logger log = Logger.getLogger(HostconfigLogProcessorPlugin.class);

    private ConfigRepository configRepository;
    private UUID hostconfigServiceId;
    private boolean hostconfigServiceIdLoaded;


    @Reference
    protected synchronized void setConfigRepository ( ConfigRepository cr ) {
        this.configRepository = cr;
    }


    protected synchronized void unsetConfigRepository ( ConfigRepository cr ) {
        if ( this.configRepository == cr ) {
            this.configRepository = null;
        }
    }


    /**
     * @return
     */
    private UUID getHostconfigServiceId () {
        if ( !this.hostconfigServiceIdLoaded ) {
            try {
                this.hostconfigServiceIdLoaded = true;
                ServiceStructuralObject hcService = this.configRepository
                        .getSingletonServiceByType(HostConfigServiceTypeDescriptor.HOSTCONFIG_SERVICE_TYPE);
                if ( hcService != null ) {
                    this.hostconfigServiceId = hcService.getId();
                }
            }
            catch ( ConfigRepositoryException e ) {
                log.debug("Failed to get hostconfig service", e); //$NON-NLS-1$
            }
        }
        return this.hostconfigServiceId;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.logsink.LogProcessorPlugin#getPriority()
     */
    @Override
    public float getPriority () {
        return -100f;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.logsink.LogProcessorPlugin#matches(java.util.Map)
     */
    @Override
    public boolean matches ( Map<String, Object> ev ) {
        return !ev.containsKey(LogFields.OBJECT_ID);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.logsink.LogProcessorPlugin#process(eu.agno3.orchestrator.system.logsink.ProcessorContext,
     *      java.util.Map)
     */
    @Override
    public LogAction process ( ProcessorContext ctx, Map<String, Object> ev ) {
        UUID hcServiceId = getHostconfigServiceId();
        if ( hcServiceId != null ) {
            ev.put(LogFields.OBJECT_ID, hcServiceId);
        }
        return LogAction.IGNORE;
    }

}
