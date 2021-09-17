/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 19, 2017 by mbechler
 */
package eu.agno3.fileshare.service.audit.internal;


import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.service.AuditReaderService;
import eu.agno3.runtime.eventlog.impl.MapEvent;


/**
 * @author mbechler
 *
 */
@Component ( service = AuditReaderService.class, configurationPolicy = ConfigurationPolicy.REQUIRE, configurationPid = "audit.reader.none" )
public class NoAuditReaderService extends BaseAuditReaderServiceImpl {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.AuditReaderService#isAvailable()
     */
    @Override
    public boolean isAvailable () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.AuditReaderService#getRetentionTimeDays()
     */
    @Override
    public int getRetentionTimeDays () {
        return 0;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.audit.internal.BaseAuditReaderServiceImpl#fetchEntityEvents(eu.agno3.fileshare.model.EntityKey,
     *      org.joda.time.DateTime, org.joda.time.DateTime, java.util.Set, int, int)
     */
    @Override
    protected List<MapEvent> fetchEntityEvents ( EntityKey entityId, DateTime start, DateTime end, Set<String> filterActions, int offset,
            int pageSize ) {
        return Collections.EMPTY_LIST;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.audit.internal.BaseAuditReaderServiceImpl#countEntityEvents(eu.agno3.fileshare.model.EntityKey,
     *      org.joda.time.DateTime, org.joda.time.DateTime, java.util.Set)
     */
    @Override
    protected long countEntityEvents ( EntityKey entityId, DateTime start, DateTime end, Set<String> filterActions ) {
        return 0;
    }

}
