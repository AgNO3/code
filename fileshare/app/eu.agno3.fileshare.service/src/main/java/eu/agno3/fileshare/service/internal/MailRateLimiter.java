/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.06.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import java.util.Dictionary;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import eu.agno3.fileshare.exceptions.MailRateLimitingException;
import eu.agno3.fileshare.model.notify.MailRecipient;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.security.ratelimit.RateLimitCounter;
import eu.agno3.runtime.security.ratelimit.RateLimiter;
import eu.agno3.runtime.security.ratelimit.impl.RateLimitCounterImpl;
import eu.agno3.runtime.security.ratelimit.impl.RateLimiterImpl;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = MailRateLimiter.class, configurationPid = "registration.rate" )
public class MailRateLimiter implements Runnable {

    /**
     * 
     */
    private static final int DEFAULT_THRESHOLD = 3;
    private static final float DEFAULT_BASE = 2.0f;

    private static final Logger log = Logger.getLogger(MailRateLimiter.class);

    private static final long DEFAULT_INTERVAL = 60;
    private static final int DEFAULT_TABLE_SIZE = 1024;

    private RateLimiter<UserPrincipal> userLimiter;
    private RateLimiter<String> targetLimiter;
    private RateLimiter<String> registrationSourceLimiter;
    private RateLimiter<UserPrincipal> resetLimiter;
    private RateLimitCounter registrationGlobalCounter = new RateLimitCounterImpl();

    private ScheduledExecutorService executor;
    private int registrationGlobalThreshold;
    private float registrationGlobalBase;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        long interval = DEFAULT_INTERVAL;
        String intervalAttr = (String) ctx.getProperties().get("interval"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(intervalAttr) ) {
            interval = Long.parseLong(intervalAttr);
        }

        createLimiters(ctx.getProperties());

        this.executor = Executors.newSingleThreadScheduledExecutor();
        this.executor.scheduleAtFixedRate(this, interval, interval, TimeUnit.SECONDS);
    }


    /**
     * @param properties
     */
    private void createLimiters ( Dictionary<String, Object> cfg ) {

        this.userLimiter = new RateLimiterImpl<>(ConfigUtil.parseInt(cfg, "userMapSize", DEFAULT_TABLE_SIZE), //$NON-NLS-1$
            ConfigUtil.parseInt(cfg, "userThreshold", 10), //$NON-NLS-1$
            ConfigUtil.parseFloat(cfg, "userBase", DEFAULT_BASE)); //$NON-NLS-1$

        this.targetLimiter = new RateLimiterImpl<>(ConfigUtil.parseInt(cfg, "targetMapSize", DEFAULT_TABLE_SIZE), //$NON-NLS-1$
            ConfigUtil.parseInt(cfg, "targetThreshold", DEFAULT_THRESHOLD), //$NON-NLS-1$
            ConfigUtil.parseFloat(cfg, "targetBase", DEFAULT_BASE)); //$NON-NLS-1$

        this.registrationSourceLimiter = new RateLimiterImpl<>(ConfigUtil.parseInt(cfg, "registrationSourceMapSize", DEFAULT_TABLE_SIZE), //$NON-NLS-1$
            ConfigUtil.parseInt(cfg, "registrationSourceThreshold", DEFAULT_THRESHOLD), //$NON-NLS-1$
            ConfigUtil.parseFloat(cfg, "registrationSourceBase", DEFAULT_BASE)); //$NON-NLS-1$

        this.resetLimiter = new RateLimiterImpl<>(ConfigUtil.parseInt(cfg, "resetMapSize", DEFAULT_TABLE_SIZE), //$NON-NLS-1$
            ConfigUtil.parseInt(cfg, "resetThreshold", 1), //$NON-NLS-1$
            ConfigUtil.parseFloat(cfg, "resetBase", DEFAULT_BASE)); //$NON-NLS-1$

        this.registrationGlobalThreshold = ConfigUtil.parseInt(cfg, "registrationGlobalThreshold", 60); //$NON-NLS-1$
        this.registrationGlobalBase = ConfigUtil.parseFloat(cfg, "registrationGlobalBase", 1.1f); //$NON-NLS-1$

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
     * @param recpt
     * @param sourceAddress
     * @throws MailRateLimitingException
     */
    public void checkRegistrationDelay ( MailRecipient recpt, String sourceAddress ) throws MailRateLimitingException {
        int sourceDelay = 0;
        int targetDelay = 0;
        if ( this.registrationSourceLimiter != null && sourceAddress != null ) {
            RateLimitCounter sourceCounter = this.registrationSourceLimiter.get(sourceAddress);
            sourceDelay = this.registrationSourceLimiter.makeDelay(sourceAddress);
            if ( log.isTraceEnabled() ) {
                log.trace(String.format("Source registration delay %d s for %d fail %d success", //$NON-NLS-1$
                    sourceDelay,
                    sourceCounter.getFailCount(),
                    sourceCounter.getSuccessCount()));
            }
        }

        if ( this.targetLimiter != null ) {
            targetDelay = this.targetLimiter.makeDelay(recpt.getMailAddress());
            if ( log.isTraceEnabled() ) {
                log.trace(String.format("Target registration delay %d s", //$NON-NLS-1$
                    targetDelay));
            }
        }

        int globalDelay = 0;

        globalDelay = RateLimiterImpl.makeDelay(this.registrationGlobalThreshold, this.registrationGlobalBase, this.registrationGlobalCounter);
        if ( log.isTraceEnabled() ) {
            log.trace(String.format("Global login delay %d s for %d fail %d success", //$NON-NLS-1$
                globalDelay,
                this.registrationGlobalCounter.getFailCount(),
                this.registrationGlobalCounter.getSuccessCount()));
        }

        if ( sourceDelay > 0 || targetDelay > 0 ) {
            int delay = Math.max(sourceDelay, targetDelay) + globalDelay;
            throw new MailRateLimitingException(delay);
        }

        this.registrationGlobalCounter.fail();
        this.targetLimiter.fail(recpt.getMailAddress());
        this.registrationSourceLimiter.fail(sourceAddress);

    }


    /**
     * @param up
     * @param remoteAddr
     * @throws MailRateLimitingException
     */
    public void checkPasswordResetDelay ( UserPrincipal up, String remoteAddr ) throws MailRateLimitingException {
        int userDelay = 0;
        if ( this.resetLimiter != null && up != null ) {
            RateLimitCounter sourceCounter = this.resetLimiter.get(up);
            userDelay = this.resetLimiter.makeDelay(up);
            if ( log.isTraceEnabled() ) {
                log.trace(String.format("User password reset delay %d s for %d fail %d success", //$NON-NLS-1$
                    userDelay,
                    sourceCounter.getFailCount(),
                    sourceCounter.getSuccessCount()));
            }
        }

        if ( userDelay > 0 ) {
            throw new MailRateLimitingException(userDelay);
        }

        this.resetLimiter.fail(up);
    }


    /**
     * @param recpt
     * @param up
     * @param sourceAddress
     * @throws MailRateLimitingException
     */
    public void checkUserMailDelay ( MailRecipient recpt, UserPrincipal up, String sourceAddress ) throws MailRateLimitingException {

        int userDelay = 0;
        int targetDelay = 0;
        if ( this.userLimiter != null && up != null ) {
            RateLimitCounter sourceCounter = this.userLimiter.get(up);
            userDelay = this.userLimiter.makeDelay(up);
            if ( log.isTraceEnabled() ) {
                log.trace(String.format("User invitation delay %d s for %d fail %d success", //$NON-NLS-1$
                    userDelay,
                    sourceCounter.getFailCount(),
                    sourceCounter.getSuccessCount()));
            }
        }

        if ( recpt != null && this.targetLimiter != null ) {
            targetDelay = this.targetLimiter.makeDelay(recpt.getMailAddress());
            if ( log.isTraceEnabled() ) {
                log.trace(String.format("Target registration delay %d s", //$NON-NLS-1$
                    targetDelay));
            }
        }

        if ( userDelay > 0 || targetDelay > 0 ) {
            int delay = Math.max(userDelay, targetDelay);
            throw new MailRateLimitingException(delay);
        }

        if ( recpt != null ) {
            this.targetLimiter.fail(recpt.getMailAddress());
        }
        this.userLimiter.fail(up);
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run () {

        if ( this.userLimiter != null ) {
            this.userLimiter.maintenance();
        }

        if ( this.targetLimiter != null ) {
            this.targetLimiter.maintenance();
        }

        if ( this.registrationSourceLimiter != null ) {
            this.registrationSourceLimiter.maintenance();
        }

        if ( this.registrationGlobalCounter != null ) {
            this.registrationGlobalCounter.flip();
        }

    }

}
