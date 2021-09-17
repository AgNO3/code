/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.06.2015 by mbechler
 */
package eu.agno3.runtime.security.ratelimit.impl;


import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import eu.agno3.runtime.security.ratelimit.RateLimitCounter;
import eu.agno3.runtime.security.ratelimit.RateLimiter;


/**
 * @author mbechler
 * @param <T>
 *
 */
public class RateLimiterImpl <T> implements RateLimiter<T> {

    private static final Logger log = Logger.getLogger(RateLimiterImpl.class);

    private Map<T, RateLimitCounterImpl> entries;

    private int threshold;

    private float base;


    /**
     * @param tableSize
     * @param threshold
     * @param base
     * 
     */
    public RateLimiterImpl ( int tableSize, int threshold, float base ) {
        this.threshold = threshold;
        this.base = base;
        this.entries = Collections.synchronizedMap(new LRUMap<>(tableSize));
    }


    /**
     * 
     */
    @Override
    public void maintenance () {
        Set<T> toRemovePrinc = new HashSet<>();
        for ( Entry<T, RateLimitCounterImpl> e : this.entries.entrySet() ) {
            RateLimitCounterImpl counter = e.getValue();
            counter.flip();

            if ( counter.getFailCount() == 0 ) {
                toRemovePrinc.add(e.getKey());
            }
        }

        for ( T toRemove : toRemovePrinc ) {
            this.entries.remove(toRemove);
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ratelimit.RateLimiter#get(java.lang.Object)
     */
    @Override
    public RateLimitCounterImpl get ( T obj ) {
        RateLimitCounterImpl princCounter = this.entries.get(obj);
        if ( princCounter == null ) {
            princCounter = new RateLimitCounterImpl();
            this.entries.put(obj, princCounter);
        }
        return princCounter;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ratelimit.RateLimiter#fail(java.lang.Object)
     */
    @Override
    public void fail ( T obj ) {
        get(obj).fail();
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ratelimit.RateLimiter#success(java.lang.Object)
     */
    @Override
    public void success ( T obj ) {
        get(obj).success();
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ratelimit.RateLimiter#makeDelay(java.lang.Object)
     */
    @Override
    public int makeDelay ( T obj ) {
        return makeDelay(this.threshold, this.base, get(obj));
    }


    /**
     * @param threshold
     * @param base
     * @param counter
     * @return a delay for the given parameters
     */
    public static int makeDelay ( int threshold, float base, RateLimitCounter counter ) {
        DateTime lastFail = counter.getLastFail();
        int failAttempts = counter.getFailCount();
        int successAttempts = counter.getSuccessCount();

        if ( lastFail == null || failAttempts < threshold ) {
            return 0;
        }

        Duration lastFailDur = new Duration(lastFail, DateTime.now());
        long lastFailSeconds = lastFailDur.getStandardSeconds();
        double val = Math.exp(failAttempts - successAttempts) / Math.exp(base);
        long valInt = Math.round(val);

        int remain = 0;
        if ( valInt >= lastFailSeconds ) {
            remain = (int) ( valInt - lastFailSeconds );
        }

        if ( log.isTraceEnabled() ) {
            log.trace(String.format("Remaining %d s, failAttempts %d, lastFail %s", remain, failAttempts, lastFail)); //$NON-NLS-1$
        }

        return remain;
    }

}
