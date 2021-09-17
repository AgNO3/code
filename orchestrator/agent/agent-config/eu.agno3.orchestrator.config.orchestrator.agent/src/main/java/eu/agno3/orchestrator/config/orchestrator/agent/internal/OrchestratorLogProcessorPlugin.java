/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.02.2016 by mbechler
 */
package eu.agno3.orchestrator.config.orchestrator.agent.internal;


import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.orchestrator.desc.OrchestratorServiceTypeDescriptor;
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
public class OrchestratorLogProcessorPlugin implements LogProcessorPlugin {

    private static final Logger log = Logger.getLogger(OrchestratorLogProcessorPlugin.class);

    private ConfigRepository configRepository;
    private UUID orchServiceId;
    private boolean orchServiceIdLoaded;


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
    private UUID getOrchestratorServiceId () {
        if ( !this.orchServiceIdLoaded ) {
            try {
                this.orchServiceIdLoaded = true;
                ServiceStructuralObject hcService = this.configRepository
                        .getSingletonServiceByType(OrchestratorServiceTypeDescriptor.ORCHESTRATOR_SERVICE_TYPE);
                if ( hcService != null ) {
                    this.orchServiceId = hcService.getId();
                }
            }
            catch ( ConfigRepositoryException e ) {
                log.debug("Failed to get orchestrator service", e); //$NON-NLS-1$
            }
        }
        return this.orchServiceId;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.logsink.LogProcessorPlugin#getPriority()
     */
    @Override
    public float getPriority () {
        return 0f;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.logsink.LogProcessorPlugin#matches(java.util.Map)
     */
    @Override
    public boolean matches ( Map<String, Object> ev ) {
        return !ev.containsKey(LogFields.OBJECT_ID) && ( "orchserver.service".equals(ev.get(LogFields.SYSTEMD_UNIT)) || //$NON-NLS-1$
                "orchserver".equals(ev.get(LogFields.TAG)) ); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.logsink.LogProcessorPlugin#process(eu.agno3.orchestrator.system.logsink.ProcessorContext,
     *      java.util.Map)
     */
    @Override
    public LogAction process ( ProcessorContext ctx, Map<String, Object> ev ) {
        UUID ocServiceId = getOrchestratorServiceId();
        if ( ocServiceId != null ) {
            ev.put(LogFields.OBJECT_ID, ocServiceId);
        }
        return LogAction.IGNORE;
    }

}
