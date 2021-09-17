/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2016 by mbechler
 */
package eu.agno3.orchestrator.system.logsink.impl;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.system.logging.LogFields;
import eu.agno3.orchestrator.system.logsink.LogAction;
import eu.agno3.orchestrator.system.logsink.LogProcessorPlugin;
import eu.agno3.orchestrator.system.logsink.LogTimerExpiredRunnable;
import eu.agno3.orchestrator.system.logsink.ProcessorContext;


/**
 * @author mbechler
 *
 */
public abstract class AbstractRecombiningLogProcessPlugin implements LogProcessorPlugin, LogTimerExpiredRunnable {

    private static final Logger log = Logger.getLogger(AbstractRecombiningLogProcessPlugin.class);

    private Queue<Map<String, Object>> backlog = new LinkedList<>();
    private Long lastTimestamp;

    private int recombinationTimeout = 500;
    private int maxBacklog = 50;

    private ScheduledFuture<?> timer;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.logsink.LogProcessorPlugin#matches(java.util.Map)
     */
    @Override
    public boolean matches ( Map<String, Object> ev ) {
        return !ev.containsKey(LogFields.RECOMBINED);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.logsink.LogProcessorPlugin#process(eu.agno3.orchestrator.system.logsink.ProcessorContext,
     *      java.util.Map)
     */
    @Override
    public LogAction process ( ProcessorContext ctx, Map<String, Object> ev ) {
        if ( ev.containsKey(LogFields.RECOMBINED) ) {
            return LogAction.IGNORE;
        }
        ev.put(LogFields.RECOMBINED, false);

        Long newTs = ev.containsKey(LogFields.TIMESTAMP) ? Long.parseLong((String) ev.get(LogFields.TIMESTAMP)) : null;
        Long lastTs = this.lastTimestamp;

        if ( newTs != null && !this.backlog.isEmpty() ) {
            this.lastTimestamp = newTs;
            if ( lastTs != null && newTs - lastTs > this.recombinationTimeout * 1000 ) {
                log.debug("Flushing because of timeout"); //$NON-NLS-1$
                flushAll(ctx);
            }
        }

        if ( this.backlog.size() >= this.maxBacklog ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Flushing because of limit " + this.maxBacklog); //$NON-NLS-1$
            }
            flushAll(ctx);
        }

        if ( isRecombineable(ev) ) {
            return pushMessage(ctx, ev);
        }

        return LogAction.IGNORE;
    }


    protected void flushAll ( ProcessorContext context ) {
        flush(context, this.backlog.size());
    }


    /**
     * @param ctx
     */
    protected final void flush ( ProcessorContext ctx, int num ) {
        log.trace("Flushing log event buffer"); //$NON-NLS-1$
        synchronized ( this.backlog ) {
            int remaining = num;
            for ( Iterator<Map<String, Object>> iterator = this.backlog.iterator(); iterator.hasNext(); ) {
                if ( remaining <= 0 ) {
                    return;
                }
                Map<String, Object> ev = iterator.next();
                ctx.inject(ev);
                iterator.remove();
            }

            if ( this.backlog.isEmpty() && this.timer != null ) {
                log.debug("Canceling timer"); //$NON-NLS-1$
                this.timer.cancel(false);
                this.timer = null;
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.logsink.LogTimerExpiredRunnable#expired(eu.agno3.orchestrator.system.logsink.ProcessorContext)
     */
    @Override
    public void expired ( ProcessorContext context ) {
        this.timer = null;
        log.debug("Timer called"); //$NON-NLS-1$
        flushAll(context);
    }


    /**
     * @param ctx
     * @param ev
     * @return
     */
    private LogAction pushMessage ( ProcessorContext ctx, Map<String, Object> ev ) {
        if ( log.isTraceEnabled() ) {
            log.trace("Have message " + ev); //$NON-NLS-1$
        }
        LogAction act = haveMessage(ctx, ev);
        if ( act == LogAction.EMIT ) {
            synchronized ( this.backlog ) {
                if ( log.isTraceEnabled() ) {
                    log.trace("Adding to backlog " + ev); //$NON-NLS-1$
                }
                this.backlog.add(ev);
                if ( this.timer == null ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug("Setting up timer in " + this.recombinationTimeout); //$NON-NLS-1$
                    }
                    this.timer = ctx.addTimer(this.recombinationTimeout, this);
                }
            }
            act = LogAction.DROP;
        }
        return act;
    }


    /**
     * @return the backlog
     */
    public Queue<Map<String, Object>> getBacklog () {
        return this.backlog;
    }


    /**
     * @param ctx
     * @param ev
     * @return whether this message has been recombined (otherwise it will be pushed to the backlog)
     */
    protected LogAction haveMessage ( ProcessorContext ctx, Map<String, Object> ev ) {
        return LogAction.IGNORE;
    }


    /**
     * @param ev
     * @return
     */
    protected boolean isRecombineable ( Map<String, Object> ev ) {
        return true;
    }

}
