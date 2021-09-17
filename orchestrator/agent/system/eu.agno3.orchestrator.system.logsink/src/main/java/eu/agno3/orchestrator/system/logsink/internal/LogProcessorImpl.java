/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.02.2016 by mbechler
 */
package eu.agno3.orchestrator.system.logsink.internal;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import eu.agno3.orchestrator.agent.connector.AgentServerConnector;
import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageSource;
import eu.agno3.orchestrator.system.logging.LogEvent;
import eu.agno3.orchestrator.system.logging.LogFields;
import eu.agno3.orchestrator.system.logsink.InhibitableLogSource;
import eu.agno3.orchestrator.system.logsink.LogAction;
import eu.agno3.orchestrator.system.logsink.LogProcessor;
import eu.agno3.orchestrator.system.logsink.LogProcessorPlugin;
import eu.agno3.orchestrator.system.logsink.LogTimerExpiredRunnable;
import eu.agno3.orchestrator.system.logsink.ProcessorContext;
import eu.agno3.runtime.messaging.InvalidSessionException;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.client.MessagingClient;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = LogProcessor.class )
public class LogProcessorImpl implements LogProcessor, ProcessorContext {

    private static final Logger log = Logger.getLogger(LogProcessorImpl.class);

    private static final ObjectMapper OM = new ObjectMapper();
    private static final JsonFactory JF;


    static {
        OM.registerModule(new JodaModule());
        JF = new JsonFactory(OM);
    }

    private static final int EVENT_SIZE_LIMIT = 512 * 1024;
    private static final int QUEUE_CAPACITY = 256;
    final BlockingQueue<Map<String, Object>> inputQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY, true);
    final BlockingQueue<LogEvent> outputQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY, true);
    final BlockingQueue<LogEvent> fallbackQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY, true);
    private final Set<WeakReference<InhibitableLogSource>> inhibited = new HashSet<>();
    private final Map<InhibitableLogSource, Object> known = Collections.synchronizedMap(new WeakHashMap<>());

    volatile boolean exit;

    private MessagingClient<AgentMessageSource> messageClient;
    private Object messageClientLock = new Object();

    private AgentServerConnector connector;

    private Thread inputThread;
    private Thread outputThread;

    private int sinceLastCheckpoint;
    private long lastCheckpointTime;

    private int checkpointTimeLimit = 10000;
    private int checkpointLimit = 50;

    private ScheduledExecutorService timer; // $NON-NLS-1$

    private SortedSet<LogProcessorPlugin> plugins = new TreeSet<>(new LogProcessorPluginComparator());

    int bulkSize = 500;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        configure(ctx.getProperties());
        this.exit = false;
        this.inputThread = new Thread(new InputRunnable(), "Logger-Input"); //$NON-NLS-1$
        this.outputThread = new Thread(new OutputRunnable(), "Logger-Output"); //$NON-NLS-1$
        this.timer = Executors.newScheduledThreadPool(1, new ThreadFactory() {

            @Override
            public Thread newThread ( Runnable r ) {
                Thread thread = new Thread("LogProcessor-Timer"); //$NON-NLS-1$
                thread.setDaemon(true);
                return thread;
            }
        });
        this.inputThread.start();
        this.outputThread.start();

    }


    /**
     * @param properties
     */
    private void configure ( Dictionary<String, Object> cfg ) {
        this.checkpointTimeLimit = ConfigUtil.parseInt(cfg, "checkpointTimeLimit", 10000); //$NON-NLS-1$
        this.checkpointLimit = ConfigUtil.parseInt(cfg, "checkpointTimeLimit", 50); //$NON-NLS-1$
    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        configure(ctx.getProperties());
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        this.exit = true;
        if ( this.inputThread != null ) {
            this.inputThread.interrupt();
            try {
                this.inputThread.join(1000);
            }
            catch ( InterruptedException e ) {
                log.warn("Failed to join input thread within timeout", e); //$NON-NLS-1$
            }
            this.inputThread = null;
        }

        if ( this.outputThread != null ) {
            this.outputThread.interrupt();
            try {
                this.outputThread.join(1000);
            }
            catch ( InterruptedException e ) {
                log.warn("Failed to join output thread within timeout", e); //$NON-NLS-1$
            }
            this.outputThread = null;
        }
        if ( this.timer != null ) {
            this.timer.shutdownNow();
            try {
                this.timer.awaitTermination(1, TimeUnit.SECONDS);
            }
            catch ( InterruptedException e ) {
                log.warn("Failed to join timer thread within timeout", e); //$NON-NLS-1$
            }
            this.timer = null;
        }
    }


    /**
     * @param mc
     *            the messageClient to set
     */
    @Reference ( cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC )
    protected void setMessageClient ( MessagingClient<AgentMessageSource> mc ) {
        synchronized ( this.messageClientLock ) {
            this.messageClient = mc;
            this.messageClientLock.notify();
        }
    }


    protected void unsetMessageClient ( MessagingClient<AgentMessageSource> mc ) {
        if ( this.messageClient == mc ) {
            synchronized ( this.messageClientLock ) {
                if ( this.messageClient == mc ) {
                    this.messageClient = null;
                    this.messageClientLock.notify();
                }
            }
        }
    }


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected void bindPlugin ( LogProcessorPlugin lpp ) {
        synchronized ( this.plugins ) {
            this.plugins.add(lpp);
        }
    }


    protected void unbindPlugin ( LogProcessorPlugin lpp ) {
        synchronized ( this.plugins ) {
            this.plugins.remove(lpp);
        }
    }


    @Reference
    protected synchronized void setAgentConnector ( AgentServerConnector asc ) {
        this.connector = asc;
    }


    protected synchronized void unsetAgentConnector ( AgentServerConnector asc ) {
        if ( this.connector == asc ) {
            this.connector = null;
        }
    }


    /**
     * @return the messageClient
     * @throws InterruptedException
     */
    MessagingClient<AgentMessageSource> getMessageClient () throws InterruptedException {
        log.trace("Acquire client"); //$NON-NLS-1$
        synchronized ( this.messageClientLock ) {
            if ( this.messageClient == null ) {
                log.debug("Waiting for message client"); //$NON-NLS-1$
                this.messageClientLock.wait();
                log.debug("Have message client"); //$NON-NLS-1$
            }
            log.trace("Got client"); //$NON-NLS-1$
            return this.messageClient;
        }
    }


    @NonNull
    AgentMessageSource getMessageSource () {
        UUID componentId = this.connector.getComponentId();
        if ( componentId == null ) {
            throw new IllegalStateException("No agentId"); //$NON-NLS-1$
        }
        return new AgentMessageSource(componentId);
    }


    /**
     * @return the messageClientLock
     */
    public Object getMessageClientLock () {
        return this.messageClientLock;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.logsink.LogProcessor#process(java.util.Map,
     *      eu.agno3.orchestrator.system.logsink.InhibitableLogSource)
     */
    @Override
    public boolean process ( Map<String, Object> logEntry, InhibitableLogSource sink ) {
        boolean ignored = false;
        if ( !ignored ) {
            if ( this.inputQueue.remainingCapacity() == 0 ) {
                doInhibit(sink);
                return true;
            }

            try {
                if ( sink != null && !this.known.containsKey(sink) ) {
                    this.known.put(sink, null);
                }
                this.inputQueue.add(logEntry);
            }
            catch ( IllegalStateException e ) {
                log.debug("Failed to add to queue", e); //$NON-NLS-1$
                doInhibit(sink);
                return true;
            }
        }
        return false;
    }


    @Override
    public void inject ( Map<String, Object> ev ) {
        try {
            if ( ev == null ) {
                return;
            }
            log.debug(String.format(
                "Injecting message in queue in:%d out:%d fallback:%d", //$NON-NLS-1$
                this.inputQueue.size(),
                this.outputQueue.size(),
                this.fallbackQueue.size()));

            LogEvent pev = doProcess(ev);
            this.outputQueue.put(pev);
            log.debug("Injected message"); //$NON-NLS-1$
        }
        catch (
            InterruptedException |
            IOException e ) {
            log.warn("Interrupted while adding back to queue, event dropped " + ev, e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.logsink.ProcessorContext#addTimer(int,
     *      eu.agno3.orchestrator.system.logsink.LogTimerExpiredRunnable)
     */
    @Override
    public ScheduledFuture<?> addTimer ( int delay, LogTimerExpiredRunnable t ) {
        return this.timer.schedule(new LogTimerRunnable(this, t), delay, TimeUnit.MILLISECONDS);
    }


    /**
     * @param poll
     * @param encoder
     * @param decoder
     * @return
     * @throws IOException
     */
    LogEvent doProcess ( Map<String, Object> poll ) throws IOException {
        LogAction combined = LogAction.EMIT;

        synchronized ( this.plugins ) {
            for ( LogProcessorPlugin lpp : this.plugins ) {
                if ( lpp.matches(poll) ) {
                    combined = combined.and(lpp.process(this, poll));
                }
            }
        }

        if ( combined == LogAction.EMIT ) {
            return doMarshall(poll);
        }
        else if ( combined == LogAction.UNKNOWN ) {
            log.warn("Processor plugins did not provide a valid action"); //$NON-NLS-1$
        }

        return null;
    }


    /**
     * @param poll
     * @return
     * @throws IOException
     */
    protected LogEvent doMarshall ( Map<String, Object> poll ) throws IOException {
        LogEvent ev = new LogEvent(getMessageSource());
        ev.setTtl(1);
        Object ttl = poll.get(LogFields.TTL);
        if ( ttl != null ) {
            ev.setTtl(Integer.parseInt((String) ttl));
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try ( JsonGenerator g = JF.createGenerator(bos) ) {
            g.writeObject(poll);
        }

        if ( bos.size() >= EVENT_SIZE_LIMIT ) {
            log.error(String.format("Event size %s exceeds limit %s", bos.size(), EVENT_SIZE_LIMIT)); //$NON-NLS-1$
            return null;
        }

        ev.getProperties().put(LogFields.CURSOR, poll.get(LogFields.CURSOR));
        ev.setBytes(bos.toByteArray());

        if ( log.isTraceEnabled() ) {
            log.trace(new String(ev.getBytes(), StandardCharsets.UTF_8));
        }
        return ev;
    }


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }


    /**
     * @param sink
     */
    private void doInhibit ( InhibitableLogSource sink ) {
        if ( sink == null ) {
            return;
        }
        if ( log.isDebugEnabled() ) {
            log.debug(String.format(
                "Inhibiting source in:%d out:%d fallback:%d", //$NON-NLS-1$
                this.inputQueue.size(),
                this.outputQueue.size(),
                this.fallbackQueue.size()));
        }
        synchronized ( this.inhibited ) {
            this.inhibited.add(new WeakReference<>(sink));
        }
    }


    void reenableAll () {
        if ( this.inhibited.isEmpty() || !this.outputQueue.isEmpty() || !this.inputQueue.isEmpty() ) {
            return;
        }
        log.debug("Reenabling sources"); //$NON-NLS-1$
        synchronized ( this.inhibited ) {
            for ( WeakReference<InhibitableLogSource> ref : this.inhibited ) {
                InhibitableLogSource source = ref.get();
                if ( source != null ) {
                    source.reenable();
                }
            }
            this.inhibited.clear();
        }
    }


    /**
     * @param last
     */
    void sent ( LogEvent last ) {

        this.sinceLastCheckpoint++;

        reenableAll();

        if ( this.sinceLastCheckpoint < this.checkpointLimit
                && this.lastCheckpointTime >= ( System.currentTimeMillis() - this.checkpointTimeLimit ) ) {
            return;
        }

        this.sinceLastCheckpoint = 0;
        this.lastCheckpointTime = System.currentTimeMillis();

        String cursor = (String) last.getProperties().get(LogFields.CURSOR);
        if ( !StringUtils.isBlank(cursor) ) {
            for ( InhibitableLogSource s : new HashSet<>(this.known.keySet()) ) {
                s.checkpoint(cursor);
            }
        }
    }


    void publish ( @NonNull LogEvent ev ) throws InterruptedException {
        MessagingClient<AgentMessageSource> mc = this.messageClient;
        if ( mc != null ) {
            try {
                mc.publishEvent(ev);
            }
            catch (
                MessagingException |
                InterruptedException e ) {
                log.warn("Failed to publish event", e); //$NON-NLS-1$
                this.outputQueue.put(ev);
            }
        }
        else {
            this.outputQueue.put(ev);
        }
    }

    private class InputRunnable implements Runnable {

        /**
         * 
         */
        public InputRunnable () {}


        /**
         * {@inheritDoc}
         *
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run () {
            MDC.put("ttl", 0); //$NON-NLS-1$
            getLog().debug("Starting input thread"); //$NON-NLS-1$
            try {
                while ( !LogProcessorImpl.this.exit ) {
                    Map<String, Object> poll = LogProcessorImpl.this.inputQueue.take();
                    LogEvent ev;
                    try {
                        ev = doProcess(poll);
                    }
                    catch ( Exception e ) {
                        getLog().error("Exception in log processing, dropping message " + poll.get(LogFields.MESSAGE), e); //$NON-NLS-1$
                        continue;
                    }
                    if ( ev != null ) {
                        LogProcessorImpl.this.outputQueue.put(ev);
                    }
                }
            }
            catch ( InterruptedException e ) {
                getLog().debug("Interrupted input thread", e); //$NON-NLS-1$
            }

            getLog().debug("Exiting input thread"); //$NON-NLS-1$
        }

    }

    private class OutputRunnable implements Runnable {

        /**
         * 
         */
        public OutputRunnable () {}


        /**
         * {@inheritDoc}
         *
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run () {
            MDC.put("ttl", 0); //$NON-NLS-1$
            getLog().debug("Starting output thread"); //$NON-NLS-1$
            Session session = null;
            MessagingClient<AgentMessageSource> sessionFor = null;

            Queue<LogEvent> toSend = new LinkedList<>();
            while ( !LogProcessorImpl.this.exit ) {
                try {
                    LogProcessorImpl.this.fallbackQueue.drainTo(toSend, LogProcessorImpl.this.bulkSize);
                    // we need to make sure that everything can be put in the fallback queue
                    // and we do not exceed bulk size
                    int remaining = Math
                            .min(QUEUE_CAPACITY - LogProcessorImpl.this.fallbackQueue.size(), LogProcessorImpl.this.bulkSize - toSend.size());
                    if ( remaining > 0 && LogProcessorImpl.this.outputQueue.drainTo(toSend, remaining) == 0 ) {
                        toSend.add(LogProcessorImpl.this.outputQueue.take());
                    }

                    MessagingClient<AgentMessageSource> mc = getMessageClient();
                    if ( sessionFor != mc ) {
                        if ( session != null ) {
                            try {
                                session.close();
                            }
                            catch ( JMSException e ) {
                                getLog().warn("Failed to close session", e); //$NON-NLS-1$
                            }
                        }
                        session = mc.getSession(false, Session.DUPS_OK_ACKNOWLEDGE);
                        sessionFor = mc;
                    }
                    if ( toSend.size() > 1 ) {
                        sendCompound(session, toSend, mc);
                    }
                    else {
                        sendSingle(session, toSend, mc);
                    }
                }
                catch ( InvalidSessionException e ) {
                    getLog().debug("Session is invalid, repoen", e); //$NON-NLS-1$
                    sessionFor = null;
                    try {
                        if ( session != null ) {
                            session.close();
                        }
                    }
                    catch ( JMSException e1 ) {
                        getLog().debug("Failed to close session", e1); //$NON-NLS-1$
                    }
                    session = null;
                }
                catch (
                    IOException |
                    MessagingException e ) {
                    getLog().warn("Failed to send log messages", e); //$NON-NLS-1$
                }
                catch ( InterruptedException e ) {
                    getLog().debug("Interrupted output thread", e); //$NON-NLS-1$
                }
                catch ( Exception e ) {
                    getLog().error("Uncaught exception while sending log messages", e); //$NON-NLS-1$
                }
                finally {
                    try {
                        LogProcessorImpl.this.fallbackQueue.addAll(toSend);
                    }
                    catch ( IllegalStateException e ) {
                        getLog().error("Failed to recover unsent log events, dropping", e); //$NON-NLS-1$
                        getLog().debug("Session may be invalid, repoen", e); //$NON-NLS-1$
                        sessionFor = null;
                        try {
                            if ( session != null ) {
                                session.close();
                            }
                        }
                        catch ( JMSException e1 ) {
                            getLog().debug("Failed to close session", e1); //$NON-NLS-1$
                        }
                        session = null;
                    }
                    toSend.clear();
                }
            }

            if ( session != null ) {
                try {
                    getLog().debug("Closing session"); //$NON-NLS-1$
                    session.close();
                }
                catch ( JMSException e ) {
                    getLog().warn("Failed to close session", e); //$NON-NLS-1$
                }
            }

            getLog().debug("Exiting output thread"); //$NON-NLS-1$
        }


        /**
         * @param session
         * @param toSend
         * @param mc
         * @throws MessagingException
         * @throws InterruptedException
         */
        protected void sendSingle ( Session session, Queue<LogEvent> toSend, MessagingClient<AgentMessageSource> mc )
                throws MessagingException, InterruptedException {
            getLog().trace("Sending single event"); //$NON-NLS-1$
            LogEvent event = toSend.poll();
            if ( event == null ) {
                return;
            }

            if ( event.getBytes().length >= EVENT_SIZE_LIMIT ) {
                getLog().error("Log message exceeds size limit, skip"); //$NON-NLS-1$
                return;
            }

            mc.publishEvent(session, event);
            sent(event);
            toSend.clear();
        }


        /**
         * @param session
         * @param toSend
         * @param mc
         * @throws IOException
         * @throws InterruptedException
         * @throws InvalidSessionException
         */
        protected void sendCompound ( Session session, Queue<LogEvent> toSend, MessagingClient<AgentMessageSource> mc )
                throws IOException, InterruptedException, InvalidSessionException {
            Set<LogEvent> children = new HashSet<>();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            while ( ( children.isEmpty() || toSend.size() > LogProcessorImpl.this.bulkSize / 4 ) && bos.size() < EVENT_SIZE_LIMIT ) {
                LogEvent compound = new LogEvent(getMessageSource());
                compound.setTtl(1);
                LogEvent last = null;
                bos.reset();
                children.clear();

                for ( Iterator<LogEvent> iterator = toSend.iterator(); iterator.hasNext(); ) {
                    LogEvent logEvent = iterator.next();

                    if ( logEvent == null ) {
                        continue;
                    }

                    if ( ( bos.size() + logEvent.getBytes().length ) >= ( EVENT_SIZE_LIMIT - 256 ) ) {
                        if ( children.isEmpty() ) {
                            iterator.remove();
                            getLog().error("Skipping log event exceeding maximum size " + logEvent.getBytes().length); //$NON-NLS-1$
                        }
                        break;
                    }

                    bos.write(logEvent.getBytes());
                    bos.write('\n');
                    last = logEvent;
                    children.add(logEvent);
                }

                if ( children.isEmpty() ) {
                    continue;
                }

                compound.setBytes(bos.toByteArray());
                compound.setCount(children.size());
                compound.getProperties().put(LogFields.CURSOR, last != null ? last.getProperties().get(LogFields.CURSOR) : null);
                toSend.removeAll(children);

                try {
                    if ( getLog().isTraceEnabled() ) {
                        getLog().trace("Sending compound event " + children.size()); //$NON-NLS-1$
                    }
                    mc.publishEvent(session, compound);
                    sent(last);
                }
                catch ( InvalidSessionException e ) {
                    LogProcessorImpl.this.fallbackQueue.add(compound);
                    throw e;
                }
                catch ( MessagingException e ) {
                    getLog().debug("Compound send failed", e); //$NON-NLS-1$
                    LogProcessorImpl.this.fallbackQueue.add(compound);
                    break;
                }
            }
        }

    }
}
