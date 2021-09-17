/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.02.2016 by mbechler
 */
package eu.agno3.fileshare.orch.agent;


import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.orch.common.config.desc.FileshareServiceTypeDescriptor;
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
public class FileshareLogProcessorPlugin implements LogProcessorPlugin {

    private static final Logger log = Logger.getLogger(FileshareLogProcessorPlugin.class);

    private ConfigRepository configRepository;
    private UUID fileshareServiceId;
    private boolean fileshareServiceIdLoaded;


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
    private UUID getFileshareServiceId () {
        if ( !this.fileshareServiceIdLoaded ) {
            try {
                this.fileshareServiceIdLoaded = true;
                ServiceStructuralObject fsService = this.configRepository
                        .getSingletonServiceByType(FileshareServiceTypeDescriptor.FILESHARE_SERVICE_TYPE);
                if ( fsService != null ) {
                    this.fileshareServiceId = fsService.getId();
                }
            }
            catch ( ConfigRepositoryException e ) {
                log.debug("Failed to get fileshare service", e); //$NON-NLS-1$
            }
        }
        return this.fileshareServiceId;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.logsink.LogProcessorPlugin#getPriority()
     */
    @Override
    public float getPriority () {
        return 10f;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.logsink.LogProcessorPlugin#matches(java.util.Map)
     */
    @Override
    public boolean matches ( Map<String, Object> ev ) {
        return !ev.containsKey(LogFields.OBJECT_ID) && ( "fileshare.service".equals(ev.get(LogFields.SYSTEMD_UNIT)) || //$NON-NLS-1$
                "fileshare".equals(ev.get(LogFields.TAG)) ); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.logsink.LogProcessorPlugin#process(eu.agno3.orchestrator.system.logsink.ProcessorContext,
     *      java.util.Map)
     */
    @Override
    public LogAction process ( ProcessorContext ctx, Map<String, Object> ev ) {
        UUID fsServiceId = getFileshareServiceId();
        if ( fsServiceId != null ) {
            ev.put(LogFields.OBJECT_ID, fsServiceId);
        }
        return LogAction.IGNORE;
    }

}
