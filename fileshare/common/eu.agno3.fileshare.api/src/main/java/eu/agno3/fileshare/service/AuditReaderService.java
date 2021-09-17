/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.06.2015 by mbechler
 */
package eu.agno3.fileshare.service;


import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.runtime.eventlog.impl.MapEvent;


/**
 * @author mbechler
 *
 */
public interface AuditReaderService {

    /**
     * @return whether reading of audit events is available
     */
    boolean isAvailable ();


    /**
     * @param entityId
     * @param start
     * @param end
     * @param filterActions
     * @param offset
     * @param pageSize
     * @return all relevant entity ids
     * @throws FileshareException
     */
    List<MapEvent> getAllEntityEvents ( EntityKey entityId, DateTime start, DateTime end, Set<String> filterActions, int offset, int pageSize )
            throws FileshareException;


    /**
     * @param entityId
     * @param start
     * @param end
     * @param filterActions
     * @return the number of matching events
     * @throws FileshareException
     */
    long getEntityEventCount ( EntityKey entityId, DateTime start, DateTime end, Set<String> filterActions ) throws FileshareException;


    /**
     * @return the number of days that events are retained
     */
    int getRetentionTimeDays ();

}
