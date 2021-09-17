/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.01.2017 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service.impl;


import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.model.validation.ConfigTestResultImpl;


/**
 * @author mbechler
 *
 */
@Component ( service = ConfigTestResultCache.class )
public class ConfigTestResultCacheImpl implements ConfigTestResultCache {

    private static final Logger log = Logger.getLogger(ConfigTestResultCacheImpl.class);

    private static final int CACHE_SIZE = 20;
    private Map<UUID, ResultCacheEntry> entries = Collections.synchronizedMap(new LRUMap<>(CACHE_SIZE));


    @Override
    public void update ( long sequence, ConfigTestResultImpl r ) {
        UUID testId = r.getTestId();
        if ( testId == null ) {
            return;
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Updating result for %s to %s", testId, r.getState())); //$NON-NLS-1$
        }

        ResultCacheEntry e = this.entries.get(testId);
        if ( e == null ) {
            this.entries.put(testId, new ResultCacheEntry(sequence, r));
        }
        else {
            e.update(sequence, r);
        }
    }


    @Override
    public ConfigTestResultImpl get ( UUID testId ) {
        ResultCacheEntry re = this.entries.get(testId);
        if ( re == null ) {
            return null;
        }
        return re.getResult();
    }

    private static class ResultCacheEntry {

        private ConfigTestResultImpl result;
        private long sequence;


        /**
         * @param sequence
         * @param r
         * 
         */
        public ResultCacheEntry ( long sequence, ConfigTestResultImpl r ) {
            this.result = r;
            this.sequence = sequence;
        }


        synchronized void update ( long seq, ConfigTestResultImpl r ) {
            if ( seq <= this.sequence ) {
                return;
            }

            this.result = r;
        }


        /**
         * @return the result
         */
        public ConfigTestResultImpl getResult () {
            return this.result;
        }

    }
}
