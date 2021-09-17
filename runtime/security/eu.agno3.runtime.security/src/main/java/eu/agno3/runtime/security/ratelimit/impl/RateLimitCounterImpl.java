/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.03.2015 by mbechler
 */
package eu.agno3.runtime.security.ratelimit.impl;


import org.joda.time.DateTime;

import eu.agno3.runtime.security.ratelimit.RateLimitCounter;


/**
 * @author mbechler
 *
 */
public class RateLimitCounterImpl implements RateLimitCounter {

    private int successCount;
    private int lastSuccessCount;
    private DateTime lastSucess;
    private int failCount;
    private DateTime lastFail;
    private int lastFailCount;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ratelimit.RateLimitCounter#getSuccessCount()
     */
    @Override
    public int getSuccessCount () {
        return this.successCount;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ratelimit.RateLimitCounter#getFailCount()
     */
    @Override
    public int getFailCount () {
        return this.failCount;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ratelimit.RateLimitCounter#getLastFail()
     */
    @Override
    public DateTime getLastFail () {
        return this.lastFail;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ratelimit.RateLimitCounter#getLastSucess()
     */
    @Override
    public DateTime getLastSucess () {
        return this.lastSucess;
    }


    /**
     * Record a failed attempt
     */
    @Override
    public synchronized void fail () {
        this.failCount++;
        this.lastFail = DateTime.now();
    }


    /**
     * Record a successful attempt
     */
    @Override
    public synchronized void success () {
        this.successCount++;
        this.lastSucess = DateTime.now();
    }


    /**
     * Hard reset all counters to zero
     */
    public synchronized void reset () {
        this.lastSucess = null;
        this.successCount = 0;
        this.lastFail = null;
        this.failCount = 0;
    }


    /**
     * 
     */
    @Override
    public synchronized void flip () {
        this.failCount -= this.lastFailCount;
        this.successCount -= this.lastSuccessCount;

        this.lastFailCount = this.failCount;
        this.lastSuccessCount = this.successCount;
    }

}
