/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.04.2015 by mbechler
 */
package eu.agno3.runtime.eventlog.internal.file;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Dictionary;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;

import eu.agno3.runtime.eventlog.Event;
import eu.agno3.runtime.eventlog.EventLoggerBackend;
import eu.agno3.runtime.eventlog.impl.ChecksumFailureEvent;
import eu.agno3.runtime.eventlog.internal.EventMarshaller;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = EventLoggerBackend.class, configurationPid = "event.filelog", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class FileLoggerBackendImpl implements EventLoggerBackend, Runnable {

    private static final Logger log = Logger.getLogger(FileLoggerBackendImpl.class);

    private static final DateTimeFormatter LOGFILE_DATE_FORMAT = ( new DateTimeFormatterBuilder() ).appendHourOfDay(2).appendMinuteOfHour(2)
            .toFormatter();

    private static final int READ_BUFFER_SIZE = 4096;

    private ConcurrentLinkedQueue<byte[]> eventQueue = new ConcurrentLinkedQueue<>();
    private final AtomicLong queueSize = new AtomicLong();

    private Thread loggerThread;
    private boolean failed;
    private boolean exit;
    private Path logBase;

    private DateTime overrideLogTime;

    ShutdownHandler shutdownHandler;

    private Set<String> excludeStreams;

    private Set<String> includeStreams;

    private boolean ignorePostdated;
    private int retainDays;
    private long minFreeSpace;
    private float minFreeSpaceRatio;
    private String logName = "eventlog"; //$NON-NLS-1$

    boolean warnedAboutDiskSpace = false;

    private static class ShutdownHandler extends Thread {

        private FileLoggerBackendImpl fl;


        /**
         * @param fl
         * 
         */
        public ShutdownHandler ( FileLoggerBackendImpl fl ) {
            this.fl = fl;
        }


        /**
         * {@inheritDoc}
         *
         * @see java.lang.Thread#run()
         */
        @Override
        public void run () {
            this.fl.shutdownHandler = null;
            this.fl.stop();
        }
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.logName = ConfigUtil.parseString(
            ctx.getProperties(),
            "logName", //$NON-NLS-1$
            "eventlog").toLowerCase(Locale.ROOT); //$NON-NLS-1$

        if ( !parseConfig(ctx.getProperties()) ) {
            return;
        }

        this.shutdownHandler = new ShutdownHandler(this);
        Runtime.getRuntime().addShutdownHook(this.shutdownHandler);
        start();
    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        parseConfig(ctx.getProperties());
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        stop();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventLoggerBackend#reset()
     */
    @Override
    public void reset () {

    }


    /**
     * 
     */
    public synchronized void start () {
        this.exit = false;
        this.loggerThread = new Thread(this, "FileLoggerBackend"); //$NON-NLS-1$
        this.loggerThread.start();
    }


    /**
     * @param overrideLogTime
     *            the overrideLogTime to set
     */
    public void setOverrideLogTime ( DateTime overrideLogTime ) {
        this.overrideLogTime = overrideLogTime;
    }


    /**
     * @param c
     * @return whether the config is complete
     */
    public boolean parseConfig ( Dictionary<String, Object> c ) {
        String logPath = ConfigUtil.parseString(c, "logPath", null); //$NON-NLS-1$

        this.retainDays = ConfigUtil.parseInt(c, "retainDays", -1); //$NON-NLS-1$
        this.ignorePostdated = ConfigUtil.parseBoolean(c, "ignorePostdated", true); //$NON-NLS-1$
        this.includeStreams = ConfigUtil.parseStringSet(c, "includeStreams", null); //$NON-NLS-1$
        this.excludeStreams = ConfigUtil.parseStringSet(c, "excludeStreams", null); //$NON-NLS-1$

        this.minFreeSpace = ConfigUtil.parseInt(c, "minFreeSpaceMB", 100) * 1024 * 1024; //$NON-NLS-1$
        this.minFreeSpaceRatio = ConfigUtil.parseFloat(c, "minFreeSpaceRatio", 0.05f); //$NON-NLS-1$

        if ( StringUtils.isBlank(logPath) ) {
            log.error("logPath is required"); //$NON-NLS-1$
            this.failed = true;
            return false;
        }

        this.logBase = Paths.get(logPath);
        if ( !Files.exists(this.logBase) ) {
            try {
                Files.createDirectories(this.logBase);
            }
            catch ( IOException e ) {
                log.error("log base path does not exists and could not be created", e); //$NON-NLS-1$
            }
            this.failed = true;
            return false;
        }

        if ( !Files.isWritable(this.logBase) ) {
            log.error("log base path is not writeable"); //$NON-NLS-1$
            this.failed = true;
            return false;
        }

        return true;
    }


    /**
     * 
     */
    public void stop () {

        if ( this.shutdownHandler != null ) {
            try {
                Runtime.getRuntime().removeShutdownHook(this.shutdownHandler);
            }
            catch (
                IllegalArgumentException |
                IllegalStateException e ) {
                log.debug("Already shutting down", e); //$NON-NLS-1$
            }
            this.shutdownHandler = null;
        }

        if ( this.loggerThread != null && this.loggerThread.isAlive() ) {
            log.debug("Stopping file logger backend"); //$NON-NLS-1$
            synchronized ( this ) {
                this.exit = true;
                this.notify();
            }
            try {
                this.loggerThread.join(1000);
            }
            catch ( InterruptedException e ) {
                log.warn("Failed to join running thread"); //$NON-NLS-1$
            }

            this.loggerThread.interrupt();
            try {
                this.loggerThread.join(1000);
            }
            catch ( InterruptedException e ) {
                log.warn("Failed to join interrupted thread"); //$NON-NLS-1$
            }
            this.loggerThread = null;
        }

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventLoggerBackend#getPriority()
     */
    @Override
    public int getPriority () {
        return 1000;
    }


    @Override
    public Future<Object> log ( Event ev, byte[] bytes ) {
        if ( log.isTraceEnabled() ) {
            log.trace("Writing to queue " + ev.getType()); //$NON-NLS-1$
        }
        if ( this.failed && this.exit ) {
            return null;
        }

        if ( this.ignorePostdated && this.retainDays > 0 && ev.getTimestamp().isBefore(DateTime.now().minusDays(this.retainDays)) ) {
            log.warn("Tried to log an event that is older than retention time"); //$NON-NLS-1$
            return null;
        }

        this.eventQueue.add(bytes);
        long qs = this.queueSize.incrementAndGet();
        synchronized ( this ) {
            this.notify();
        }
        if ( log.isTraceEnabled() ) {
            log.trace("Wrote to queue, size is " + qs); //$NON-NLS-1$
        }

        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventLoggerBackend#bulkLog(java.util.List, java.util.Map)
     */
    @Override
    public Future<?> bulkLog ( List<Event> evs, Map<Event, byte[]> data ) {
        if ( log.isTraceEnabled() ) {
            log.trace("Writing bulk to queue"); //$NON-NLS-1$
        }
        if ( this.failed && this.exit ) {
            return null;

        }
        this.eventQueue.addAll(data.values());
        long qs = this.queueSize.addAndGet(data.size());
        synchronized ( this ) {
            this.notify();
        }
        if ( log.isTraceEnabled() ) {
            log.trace("Wrote to queue, size is " + qs); //$NON-NLS-1$
        }
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run () {
        MDC.put("TTL", 0); //$NON-NLS-1$
        try {
            Path lockFile = this.logBase.resolve(String.format(".%s.lock", this.logName)); //$NON-NLS-1$
            while ( !this.exit || !this.eventQueue.isEmpty() ) {
                MessageDigest digest = MessageDigest.getInstance("SHA-256"); //$NON-NLS-1$
                try ( FileChannel lockChannel = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                      FileLock l = lockChannel.lock() ) {
                    while ( true ) {
                        if ( this.eventQueue.isEmpty() && this.exit ) {
                            log.debug("Event queue is empty, exiting"); //$NON-NLS-1$
                            return;
                        }
                        DateTime logTime = getCurrentLogTime();
                        DateTime logEndTime = logTime.plusHours(1);
                        Path logDir = ensureLogDir(logTime);
                        Path logFile = logDir.resolve(makeLogFileName(logTime));
                        Path hashFilePath = logDir.resolve(makeHashFileName(logTime));

                        if ( log.isDebugEnabled() ) {
                            log.debug("Delay open log file " + logFile); //$NON-NLS-1$
                        }

                        doWait();
                        if ( this.eventQueue.isEmpty() && this.exit ) {
                            log.debug("Event queue is empty, exiting"); //$NON-NLS-1$
                            return;
                        }

                        if ( !checkFreeSpace(logDir) ) {
                            this.eventQueue.clear();
                            continue;
                        }
                        else if ( this.warnedAboutDiskSpace ) {
                            log.info("Recovered from insufficient disk space, continuing"); //$NON-NLS-1$
                        }

                        if ( log.isDebugEnabled() ) {
                            log.debug("Opening log file " + logFile); //$NON-NLS-1$
                        }

                        try ( FileChannel channel = FileChannel
                                .open(logFile, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
                              FileLock logLock = channel.lock() ) {

                            digest.reset();
                            if ( channel.size() > 0 ) {
                                log.debug("Continuing log file " + logFile); //$NON-NLS-1$
                                if ( !verifyChecksum(digest, hashFilePath, channel) ) {
                                    log.warn("Checksum validation failed for " + this.logName); //$NON-NLS-1$
                                    channel.position(channel.size());
                                    writeEvent(digest, channel, EventMarshaller.marshallEvent(new ChecksumFailureEvent()));
                                }
                            }
                            writeEvents(digest, logEndTime, channel);
                        }
                        catch ( IOException e ) {
                            log.warn("IO error writing log file", e); //$NON-NLS-1$
                            Thread.sleep(1000);
                        }
                        finally {
                            boolean wasInterrupted = Thread.currentThread().isInterrupted();
                            byte[] newDigest = digest.digest();
                            Files.write(hashFilePath, newDigest, StandardOpenOption.CREATE);
                            if ( log.isDebugEnabled() ) {
                                log.debug(String.format("Wrote log file %s", logFile)); //$NON-NLS-1$
                                log.debug(String.format("Wrote hash file %s : %s", hashFilePath, Hex.encodeHexString(newDigest))); //$NON-NLS-1$
                            }
                            if ( wasInterrupted ) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                }
                catch ( IOException e ) {
                    log.error("Failed to lock event log directory", e); //$NON-NLS-1$
                    Thread.sleep(10000);
                }
            }

            log.debug("Exiting outer loop"); //$NON-NLS-1$
        }
        catch (
            InterruptedException |
            NoSuchAlgorithmException e ) {
            log.trace("Interrupted FileLoggerBackendImpl thread", e); //$NON-NLS-1$
        }

        if ( this.eventQueue.size() > 0 ) {
            log.warn("Events remained in queue " + this.eventQueue.size()); //$NON-NLS-1$
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Exited FileLoggerBackendImpl thread " + this.eventQueue.size()); //$NON-NLS-1$
        }
    }


    /**
     * @param logDir
     * @return
     */
    private boolean checkFreeSpace ( Path logDir ) {

        boolean diskFull = false;
        try {
            FileStore fileStore = Files.getFileStore(logDir);
            long totalSpace = fileStore.getTotalSpace();
            long usableSpace = fileStore.getUsableSpace();

            if ( usableSpace < this.minFreeSpace ) {
                diskFull = true;
            }
            else {
                double spratio = ( (double) ( usableSpace ) ) / ( (double) totalSpace );
                float ratio = Math.max(0f, (float) spratio);
                if ( ratio < this.minFreeSpaceRatio ) {
                    diskFull = true;
                }
            }

            if ( diskFull && !this.warnedAboutDiskSpace ) {
                this.warnedAboutDiskSpace = true;
                log.error(String.format("Not enough disk space on %s, free %d of %d", logDir, usableSpace, totalSpace)); //$NON-NLS-1$
            }
            return !diskFull;
        }
        catch ( IOException e ) {
            log.debug("Failed to get storage usage information", e); //$NON-NLS-1$
            return true;
        }

    }


    private DateTime getCurrentLogTime () {
        if ( this.overrideLogTime != null ) {
            return this.overrideLogTime;
        }

        return DateTime.now().withSecondOfMinute(0).withMinuteOfHour(0);
    }


    /**
     * @param digest
     * @param logEndTime
     * @param channel
     * @throws IOException
     */
    private void writeEvents ( MessageDigest digest, DateTime logEndTime, FileChannel channel ) throws IOException {
        do {
            byte[] ev;

            long qs = 0;
            int cnt = 0;
            while ( ( ev = this.eventQueue.poll() ) != null ) {
                qs = this.queueSize.decrementAndGet();
                writeEvent(digest, channel, ev);
                cnt++;
            }

            if ( log.isTraceEnabled() ) {
                log.trace(String.format("Queue size after write of %d is %d", cnt, qs)); //$NON-NLS-1$
                if ( qs == 0 ) {
                    log.trace("Actual queue size " + this.eventQueue.size()); //$NON-NLS-1$
                }
            }

            if ( this.exit && this.eventQueue.isEmpty() ) {
                break;
            }

            doWait();

            if ( getCurrentLogTime().isAfter(logEndTime) ) {
                log.debug("Rotating log file"); //$NON-NLS-1$
                break;
            }
        }
        while ( !this.exit || !this.eventQueue.isEmpty() );
    }


    /**
     * @param digest
     * @param channel
     * @param ev
     * @throws IOException
     */
    private static void writeEvent ( MessageDigest digest, FileChannel channel, byte[] ev ) throws IOException {
        if ( ev.length == 0 ) {
            log.debug("Ignoring empty message"); //$NON-NLS-1$
            return;
        }
        digest.update(ev);
        digest.update((byte) '\n');
        channel.write(ByteBuffer.wrap(ev));
        channel.write(ByteBuffer.wrap(new byte[] {
            '\n'
        }));
        channel.force(false);
    }


    /**
     * 
     */
    private void doWait () {
        synchronized ( this ) {
            try {
                while ( this.eventQueue.isEmpty() ) {
                    this.wait();
                }
            }
            catch ( InterruptedException e ) {
                log.trace("Interrupted wait", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventLoggerBackend#runMaintenance()
     */
    @Override
    public void runMaintenance () {
        if ( this.retainDays < 0 ) {
            // keep forever
            return;
        }

        int baseComps = this.logBase.getNameCount();
        try ( Stream<Path> walk = Files.walk(this.logBase, 3) ) {
            walk.forEach(f -> {
                if ( Files.isDirectory(f) && f.getNameCount() == baseComps + 3 ) {
                    try {
                        if ( log.isTraceEnabled() ) {
                            log.trace("Found log dir " + f); //$NON-NLS-1$
                        }

                        DateTime dirDate = getDirectoryDate(f);
                        if ( dirDate.isBefore(DateTime.now().withMillisOfDay(0).minusDays(this.retainDays)) ) {
                            log.debug("Removing log from expired log directory " + f); //$NON-NLS-1$
                            doRemoveLogDir(f);
                        }
                    }
                    catch ( IllegalArgumentException e ) {
                        log.warn("Failed to parse directory date", e); //$NON-NLS-1$
                    }
                }
            });
        }
        catch ( IOException e ) {
            log.error("Failed to enumerate log directories", e); //$NON-NLS-1$
        }
    }


    /**
     * @param logDir
     */
    private boolean doRemoveLogDir ( Path logDir ) {
        String prefix = this.logName + '-';
        try ( Stream<Path> list = Files.list(logDir) ) {
            list.filter(p -> {
                return p.getFileName().startsWith(prefix);
            }).forEach(f -> {
                try {
                    if ( Files.deleteIfExists(f) ) {
                        if ( log.isDebugEnabled() ) {
                            log.debug("Removed " + f); //$NON-NLS-1$
                        }
                    }
                }
                catch ( IOException e ) {
                    log.warn("Failed to remove log file " + f, e); //$NON-NLS-1$
                }

            });

            return removeIfEmpty(logDir) && removeIfEmpty(logDir.getParent()) && removeIfEmpty(logDir.getParent().getParent());
        }
        catch ( IOException e ) {
            log.error("Failed to remove expired logs in " + logDir, e); //$NON-NLS-1$
            return false;
        }
    }


    /**
     * @param dir
     * @throws IOException
     */
    private static boolean removeIfEmpty ( Path dir ) throws IOException {
        if ( !Files.isDirectory(dir) ) {
            return false;
        }
        try ( Stream<Path> list = Files.list(dir) ) {
            Optional<Path> findAny = list.findAny();
            if ( findAny.isPresent() ) {
                return false;
            }
            if ( log.isDebugEnabled() ) {
                log.debug("Removing empty " + dir); //$NON-NLS-1$
            }
            Files.deleteIfExists(dir);
            return true;
        }
    }


    /**
     * @param p
     * @return
     */
    private static DateTime getDirectoryDate ( Path p ) {
        String day = p.getName(p.getNameCount() - 1).toString();
        String month = p.getName(p.getNameCount() - 2).toString();
        String year = p.getName(p.getNameCount() - 3).toString();
        return new DateTime(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day), 0, 0);
    }


    /**
     * @param digest
     * @param hashFilePath
     * @param channel
     * @return
     */
    static boolean verifyChecksum ( MessageDigest digest, Path hashFilePath, FileChannel channel ) {
        if ( !Files.exists(hashFilePath) ) {
            log.debug(String.format("Hash file %s does not exist", hashFilePath)); //$NON-NLS-1$
            return false;
        }
        try {
            byte[] oldStoredHash = Files.readAllBytes(hashFilePath);

            if ( log.isDebugEnabled() ) {
                log.debug("Existing logfile hash " + Hex.encodeHexString(oldStoredHash)); //$NON-NLS-1$
            }

            channel.position(0);

            ByteBuffer readBuffer = ByteBuffer.allocate(READ_BUFFER_SIZE);
            while ( channel.read(readBuffer) >= 0 ) {
                readBuffer.flip();
                digest.update(readBuffer);
                readBuffer.compact();
            }

            MessageDigest clonedDigest = (MessageDigest) digest.clone();
            byte[] oldHash = clonedDigest.digest();
            if ( !MessageDigest.isEqual(oldStoredHash, oldHash) ) {
                log.warn("Existing log file corrupted, actual hash " + Hex.encodeHexString(oldHash)); //$NON-NLS-1$
                return false;
            }

            return true;
        }
        catch (
            IOException |
            CloneNotSupportedException e ) {
            log.error("Error verifiying existing checksum", e); //$NON-NLS-1$
            return false;
        }
    }


    private Path ensureLogDir ( DateTime time ) throws IOException {
        Path logDir = this.logBase.resolve(String.format(
            "%04d/%02d/%02d/", //$NON-NLS-1$
            time.year().get(),
            time.monthOfYear().get(),
            time.dayOfMonth().get()));
        Files.createDirectories(logDir);
        return logDir;
    }


    /**
     * @return
     */
    private String makeLogFileName ( DateTime time ) {
        return String.format(
            "%s-%s.log", //$NON-NLS-1$
            this.logName,
            time.toString(LOGFILE_DATE_FORMAT));
    }


    /**
     * @return
     */
    private String makeHashFileName ( DateTime time ) {
        return String.format(
            "%s-%s.sha256", //$NON-NLS-1$
            this.logName,
            time.toString(LOGFILE_DATE_FORMAT));
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventLoggerBackend#getExcludeStreams()
     */
    @Override
    public Set<String> getExcludeStreams () {
        return this.excludeStreams;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventLoggerBackend#getIncludeStreams()
     */
    @Override
    public Set<String> getIncludeStreams () {
        return this.includeStreams;
    }
}
