/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.03.2015 by mbechler
 */
package eu.agno3.runtime.security.ratelimit.internal;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.Duration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import eu.agno3.runtime.security.ratelimit.LoginRateLimiter;
import eu.agno3.runtime.security.ratelimit.RateLimitCounter;
import eu.agno3.runtime.security.ratelimit.RateLimiter;
import eu.agno3.runtime.security.ratelimit.impl.RateLimitCounterImpl;
import eu.agno3.runtime.security.ratelimit.impl.RateLimiterImpl;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * 
 * 
 * Perform rate limiting on login attempts
 * 
 * Rate limiting login attempts is necessary to prevent brute force password cracking attempts as well as
 * resource exhaustion due to computationally expensive password checkers.
 * 
 * Three counters are used:
 * a) per source (ip address), if laxSourceCheck is enabled this counter will be ignored if there are any
 * successful attempts for this source
 * 
 * b) per user (also for non-existant users), if not disabled via disableUserLockout
 * 
 * c) global, if enabled via enableGlobalDelay. The effects of this counter will only be applied if a) or c) are
 * actively limiting. Therefor it will only cause additional delays if the whole system encounters a large number of
 * failed attempts.
 * 
 * Each counter records the number of failed and successful login attempts in the last interval.
 * 
 * If the number of failed login attempts exceeds the threshold for the counter then
 * all matching logins will be disabled using exponential backoff until
 * 
 * lastFailedLoginAttempt + base ** (failedAttempts - successfulAttempts)
 * 
 * 
 * Scenarios:
 * - bruteforcing a single user's password from a single host:
 * -> both the source and the user limit will kick in
 * - bruteforcing multiple user passwords from a single host:
 * -> the source limit will kick in
 * - bruteforcing a single user from multiple hosts:
 * -> the user limit will kick in
 * - bruteforcing multiple users from multiple hosts:
 * -> if the number of users or hosts is sufficiently low the corresponding limit will kick in +
 * an additional penalty from the total number of attempts is applied. Otherwise this is simply undetectable,
 * and a general global limit would result into really easy DOS.
 * 
 * 
 * To prevent resource exhaustion counters are stored in a LRU fashion. The downside to this is that it
 * might be possible to push counters out of storage using a large number of hosts or even non-existant users.
 * 
 * @author mbechler
 *
 */
@Component ( service = LoginRateLimiter.class, configurationPid = LoginRateLimiterImpl.PID )
public class LoginRateLimiterImpl implements LoginRateLimiter, Runnable {

    /**
     * 
     */
    public static final String PID = "security.loginRate"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(LoginRateLimiterImpl.class);

    private static final int SOURCE_MAP_SIZE = 1024;
    private static final int PRINCIPAL_MAP_SIZE = 1024;

    private static final long DEFAULT_INTERVAL = 60;

    private RateLimiter<String> sourceLimiter;
    private RateLimiter<Object> principalLimiter;

    private boolean laxSourceCheck = true;
    private boolean enableGlobalDelay = true;
    private boolean disableUserLockout = false;

    private int sourceThrottleThreshold = 3;
    private float sourceThrottleBase = 2.0f;

    private int princThrottleThreshold = 2;
    private float princThrottleBase = 2.0f;

    private int globalThreshold = 100;
    private float globalBase = 1.1f;

    private RateLimitCounterImpl globalCounter = new RateLimitCounterImpl();

    private ScheduledExecutorService executor;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        parsePrincipalOptions(ctx);
        parseSourceOptions(ctx);
        parseGlobalOptions(ctx);
        Duration interval = ConfigUtil.parseDuration(ctx.getProperties(), "interval", Duration.standardSeconds(DEFAULT_INTERVAL)); //$NON-NLS-1$

        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(null);
        try {
            this.executor = Executors.newSingleThreadScheduledExecutor();
            this.executor.scheduleAtFixedRate(this, interval.getStandardSeconds(), interval.getStandardSeconds(), TimeUnit.SECONDS);
        }
        finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        if ( this.executor != null ) {
            this.executor.shutdown();
            try {
                this.executor.awaitTermination(10, TimeUnit.SECONDS);
            }
            catch ( InterruptedException e ) {
                log.warn("Clean up runner failed to shutdown within 10s", e); //$NON-NLS-1$
            }
            this.executor = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run () {
        log.debug("Cleaning up"); //$NON-NLS-1$

        if ( this.sourceLimiter != null ) {
            this.sourceLimiter.maintenance();
        }

        if ( this.principalLimiter != null && !this.disableUserLockout ) {
            this.principalLimiter.maintenance();
        }

        this.globalCounter.flip();
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ratelimit.LoginRateLimiter#recordFailAttempt(java.lang.Object, java.lang.String)
     */
    @Override
    public int recordFailAttempt ( Object up, String sourceAddress ) {

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Recording fail attempt for %s / %s", up, sourceAddress)); //$NON-NLS-1$
        }

        if ( this.sourceLimiter != null && sourceAddress != null ) {
            this.sourceLimiter.fail(sourceAddress);
        }

        if ( this.principalLimiter != null && !this.disableUserLockout && up != null ) {
            this.principalLimiter.fail(up);
        }

        if ( this.enableGlobalDelay ) {
            this.globalCounter.fail();
        }

        return getNextLoginDelay(up, sourceAddress);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ratelimit.LoginRateLimiter#recordSuccessAttempt(java.lang.Object,
     *      java.lang.String)
     */
    @Override
    public void recordSuccessAttempt ( Object up, String sourceAddress ) {

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Recording success attempt for %s / %s", up, sourceAddress)); //$NON-NLS-1$
        }

        if ( this.sourceLimiter != null && sourceAddress != null ) {
            this.sourceLimiter.success(sourceAddress);
        }

        if ( this.principalLimiter != null && !this.disableUserLockout && up != null ) {
            this.principalLimiter.success(up);
        }

        if ( this.enableGlobalDelay ) {
            this.globalCounter.success();
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ratelimit.LoginRateLimiter#preventLogin(java.lang.Object, java.lang.String)
     */
    @Override
    public boolean preventLogin ( Object up, String sourceAddress ) {
        return getNextLoginDelay(up, sourceAddress) > 0;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ratelimit.LoginRateLimiter#getNextLoginDelay(java.lang.Object, java.lang.String)
     */
    @Override
    public int getNextLoginDelay ( Object up, String sourceAddress ) {
        int sourceLoginDelay = 0;
        int principalLoginDelay = 0;
        if ( this.sourceLimiter != null && sourceAddress != null ) {
            RateLimitCounter sourceCounter = this.sourceLimiter.get(sourceAddress);

            if ( !this.laxSourceCheck || sourceCounter.getSuccessCount() == 0 ) {
                sourceLoginDelay = this.sourceLimiter.makeDelay(sourceAddress);
                if ( log.isTraceEnabled() ) {
                    log.trace(String.format("Source login delay %d s for %d fail %d success", //$NON-NLS-1$
                        sourceLoginDelay,
                        sourceCounter.getFailCount(),
                        sourceCounter.getSuccessCount()));
                }
            }
        }

        if ( this.principalLimiter != null && !this.disableUserLockout && up != null ) {
            principalLoginDelay = this.principalLimiter.makeDelay(up);
            if ( log.isTraceEnabled() ) {
                log.trace(String.format("Principal login delay %d s", //$NON-NLS-1$
                    principalLoginDelay));
            }
        }

        int globalDelay = 0;
        if ( this.enableGlobalDelay ) {
            globalDelay = RateLimiterImpl.makeDelay(this.globalThreshold, this.globalBase, this.globalCounter);
            if ( log.isTraceEnabled() ) {
                log.trace(String.format("Global login delay %d s for %d fail %d success", //$NON-NLS-1$
                    globalDelay,
                    this.globalCounter.getFailCount(),
                    this.globalCounter.getSuccessCount()));
            }
        }

        if ( sourceLoginDelay > 0 || principalLoginDelay > 0 ) {
            return Math.max(sourceLoginDelay, principalLoginDelay) + globalDelay;
        }
        return 0;
    }


    /**
     * @param ctx
     */
    private void parseGlobalOptions ( ComponentContext ctx ) {

        String enableGlobalDelayAttr = (String) ctx.getProperties().get("enableGlobalDelay"); //$NON-NLS-1$
        if ( !StringUtils.isEmpty(enableGlobalDelayAttr) ) {
            this.enableGlobalDelay = Boolean.parseBoolean(enableGlobalDelayAttr);
        }

        String globalThrottleBaseAttr = (String) ctx.getProperties().get("globalThrottleBase"); //$NON-NLS-1$
        if ( !StringUtils.isEmpty(globalThrottleBaseAttr) ) {
            this.globalBase = Float.parseFloat(globalThrottleBaseAttr);
        }

        String globalThrottleThresholdAttr = (String) ctx.getProperties().get("globalThrottleThreshold"); //$NON-NLS-1$
        if ( !StringUtils.isEmpty(globalThrottleThresholdAttr) ) {
            this.globalThreshold = Integer.parseInt(globalThrottleThresholdAttr);
        }
    }


    /**
     * @param ctx
     */
    private void parseSourceOptions ( ComponentContext ctx ) {

        String alwaysCheckSourceAttr = (String) ctx.getProperties().get("laxSourceCheck"); //$NON-NLS-1$
        if ( !StringUtils.isEmpty(alwaysCheckSourceAttr) ) {
            this.laxSourceCheck = Boolean.parseBoolean(alwaysCheckSourceAttr);
        }

        String sourceThrottleThresholdAttr = (String) ctx.getProperties().get("sourceThrottleThreshold"); //$NON-NLS-1$
        if ( !StringUtils.isEmpty(sourceThrottleThresholdAttr) ) {
            this.sourceThrottleThreshold = Integer.parseInt(sourceThrottleThresholdAttr);
        }

        String sourceThrottleBaseAttr = (String) ctx.getProperties().get("sourceThrottleBase"); //$NON-NLS-1$
        if ( !StringUtils.isEmpty(sourceThrottleBaseAttr) ) {
            this.sourceThrottleBase = Float.parseFloat(sourceThrottleBaseAttr);
        }

        int sourceMapSize = SOURCE_MAP_SIZE;
        String sourceMapSizeAttr = (String) ctx.getProperties().get("sourceMapSize"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(sourceMapSizeAttr) ) {
            sourceMapSize = Integer.parseInt(sourceMapSizeAttr);
        }
        this.sourceLimiter = new RateLimiterImpl<>(sourceMapSize, this.sourceThrottleThreshold, this.sourceThrottleBase);

    }


    /**
     * @param ctx
     */
    private void parsePrincipalOptions ( ComponentContext ctx ) {

        String disableUserLockoutAttr = (String) ctx.getProperties().get("disableUserLockout"); //$NON-NLS-1$
        if ( !StringUtils.isEmpty(disableUserLockoutAttr) ) {
            this.disableUserLockout = Boolean.parseBoolean(disableUserLockoutAttr);
        }

        String princThrottleThresholdAttr = (String) ctx.getProperties().get("principalThrottleThreshold"); //$NON-NLS-1$
        if ( !StringUtils.isEmpty(princThrottleThresholdAttr) ) {
            this.princThrottleThreshold = Integer.parseInt(princThrottleThresholdAttr);
        }

        String princThrottleBaseAttr = (String) ctx.getProperties().get("principalThrottleBase"); //$NON-NLS-1$
        if ( !StringUtils.isEmpty(princThrottleBaseAttr) ) {
            this.princThrottleBase = Float.parseFloat(princThrottleBaseAttr);
        }

        int principalMapSize = PRINCIPAL_MAP_SIZE;
        String principalMapSizeAttr = (String) ctx.getProperties().get("principalMapSize"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(principalMapSizeAttr) ) {
            principalMapSize = Integer.parseInt(principalMapSizeAttr);
        }
        this.principalLimiter = new RateLimiterImpl<>(principalMapSize, this.princThrottleThreshold, this.princThrottleBase);
    }
}
