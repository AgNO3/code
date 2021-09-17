/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.02.2016 by mbechler
 */
package eu.agno3.orchestrator.system.logsink.internal;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ProcessBuilder.Redirect;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.system.logging.LogFields;
import eu.agno3.orchestrator.system.logsink.InhibitableLogSource;
import eu.agno3.orchestrator.system.logsink.LogProcessor;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = JournalSink.class, configurationPid = "journalsink", immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE )
public class JournalSink implements InhibitableLogSource, Runnable {

    private static final Logger log = Logger.getLogger(JournalSink.class);

    private static final String CURSOR_FIELD = "__CURSOR"; //$NON-NLS-1$

    private static final int BUFFER_CAPACITY = 4096;
    private static final byte[] BUFFER = new byte[BUFFER_CAPACITY];
    private static final byte[] SMALL_BUFFER = new byte[8];

    private static final String[] REMOVE_PROPERTIES = new String[] {
        // machine id is useless for us
        "_MACHINE_ID", //$NON-NLS-1$
        "_SYSTEMD_SESSION", //$NON-NLS-1$
        "_SYSTEMD_SLICE", //$NON-NLS-1$
        "_HOSTNAME" //$NON-NLS-1$
    };

    private static final Map<String, String> DEFAULT_PROPERTY_MAP = new HashMap<>();


    static {
        DEFAULT_PROPERTY_MAP.put(CURSOR_FIELD, LogFields.CURSOR);
        DEFAULT_PROPERTY_MAP.put("SYSLOG_IDENTIFIER", LogFields.TAG); //$NON-NLS-1$
        DEFAULT_PROPERTY_MAP.put("PRIORITY", LogFields.LEVEL); //$NON-NLS-1$
        DEFAULT_PROPERTY_MAP.put("__REALTIME_TIMESTAMP", LogFields.TIMESTAMP); //$NON-NLS-1$
    }

    private String lastKnownCursor;
    private boolean exit;
    private Thread thread;

    private int binarySizeLimit = 4 * Short.MAX_VALUE;

    private Set<String> removeProperties = new HashSet<>(Arrays.asList(REMOVE_PROPERTIES));
    private Map<String, String> mapProperties = new HashMap<>();

    private long handled = 0;

    private final StringBuilder stringBuilder;
    private final CharsetDecoder decoder;
    private final ByteBuffer buf;
    private final CharBuffer cbuf;

    private long lastMonotonic;

    private String checkpointFileName;

    private volatile boolean inhibited = false;
    private final Object inhibitedLock = new Object();

    private LogProcessor logProcessor;

    private Encoder encoder;

    private String journalCtlPath;

    private boolean restoredCheckpoint;

    private int restoreLimit;


    /**
     * 
     */
    public JournalSink () {
        this.decoder = StandardCharsets.UTF_8.newDecoder(); // $NON-NLS-1$
        this.decoder.onMalformedInput(CodingErrorAction.REPORT);
        this.decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        this.encoder = Base64.getEncoder();

        this.buf = ByteBuffer.wrap(BUFFER);
        this.cbuf = CharBuffer.allocate(BUFFER_CAPACITY);
        this.stringBuilder = new StringBuilder();
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        if ( configure(ctx.getProperties()) ) {
            start();
        }
        else {
            log.error("Invalid journal listener configuration"); //$NON-NLS-1$
        }
    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        configure(ctx.getProperties());
    }


    @Reference
    protected synchronized void setLogProcessor ( LogProcessor lp ) {
        this.logProcessor = lp;
    }


    protected synchronized void unsetLogProcessor ( LogProcessor lp ) {
        if ( this.logProcessor == lp ) {
            this.logProcessor = null;
        }
    }


    /**
     * @param properties
     * @return
     */
    private boolean configure ( Dictionary<String, Object> cfg ) {
        this.restoreLimit = ConfigUtil.parseInt(cfg, "restoreLimit", -1); //$NON-NLS-1$
        this.binarySizeLimit = ConfigUtil.parseInt(cfg, "binarySizeLimit", 4 * Short.MAX_VALUE); //$NON-NLS-1$
        this.removeProperties = ConfigUtil.parseStringSet(cfg, "removeProperties", new HashSet<>(Arrays.asList(REMOVE_PROPERTIES))); //$NON-NLS-1$
        this.mapProperties = ConfigUtil.parseStringMap(cfg, "mapProperties", DEFAULT_PROPERTY_MAP); //$NON-NLS-1$
        String def = "/bin/journalctl"; //$NON-NLS-1$
        if ( !Files.exists(Paths.get(def)) ) {
            def = "/usr/bin/journalctl"; //$NON-NLS-1$
        }
        this.journalCtlPath = ConfigUtil.parseString(
            cfg,
            "journalctlPath", //$NON-NLS-1$
            def);
        this.checkpointFileName = ConfigUtil.parseString(
            cfg,
            "checkpointFile", //$NON-NLS-1$
            "/var/lib/orchagent/journal.checkpoint"); //$NON-NLS-1$

        return Files.isExecutable(Paths.get(this.journalCtlPath));
    }


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }


    /**
     * 
     */
    private synchronized void start () {
        this.thread = new Thread(this, "Journal-Listener"); //$NON-NLS-1$
        this.thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

            @Override
            public void uncaughtException ( Thread t, Throwable e ) {
                getLog().error("Uncaught exception in journal listener thread", e); //$NON-NLS-1$
            }
        });
        this.exit = false;
        this.thread.start();
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        close();
    }


    /**
     * 
     */
    private synchronized void close () {
        this.exit = true;
        if ( this.thread != null ) {
            this.thread.interrupt();
            try {
                this.thread.join(1000);
            }
            catch ( InterruptedException e ) {
                log.warn("Failed to join journal listener within timeout", e); //$NON-NLS-1$
            }
            this.thread = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run () {
        Integer oldTTL = (Integer) MDC.get(LogFields.TTL);
        if ( oldTTL != null ) {
            MDC.remove(LogFields.TTL);
        }
        MDC.put(LogFields.TTL, 0); // $NON-NLS-1$
        try {
            while ( !this.exit ) {
                try {
                    synchronized ( this.inhibitedLock ) {
                        if ( this.inhibited ) {
                            this.inhibitedLock.wait();
                        }
                    }
                    doRun();
                }
                catch ( IOException e ) {
                    log.error("Journal listener failed", e); //$NON-NLS-1$
                }
            }
        }
        catch ( InterruptedException e ) {
            log.debug("Journal listener was interrupted"); //$NON-NLS-1$
        }
        finally {
            if ( oldTTL != null ) {
                MDC.put(LogFields.TTL, oldTTL);
            }
        }
    }


    /**
     * @throws IOException
     * @throws InterruptedException
     */
    protected void doRun () throws IOException, InterruptedException {
        Process listener = launchListener();
        try ( InputStream is = listener.getInputStream();
              BufferedInputStream br = new BufferedInputStream(is) ) { // $NON-NLS-1$
            while ( listener.isAlive() ) {
                Map<String, Object> readEntry = readEntry(br);
                if ( readEntry != null ) {
                    if ( haveEntry(readEntry) ) {
                        this.inhibited = true;
                        break;
                    }
                }
            }
        }
        finally {
            if ( listener.isAlive() ) {
                log.trace("Stopping listener process"); //$NON-NLS-1$
                listener.destroy();
                listener.waitFor(1, TimeUnit.SECONDS);
                listener.destroyForcibly();
                if ( !listener.waitFor(10, TimeUnit.SECONDS) ) {
                    log.error("Journal listener failed to exit within timeout"); //$NON-NLS-1$
                }
            }
        }
    }


    /**
     * @param readEntry
     * @return whether to inhibit further executions
     */
    private boolean haveEntry ( Map<String, Object> readEntry ) {
        Object ttl = readEntry.get("TTL"); //$NON-NLS-1$
        if ( ttl instanceof String ) {
            try {
                int t = Integer.parseInt((String) ttl);
                if ( t <= 0 ) {
                    return false;
                }
            }
            catch ( NumberFormatException e ) {
                log.trace("Failed to parse ttl", e); //$NON-NLS-1$
            }
        }
        else if ( ttl != null ) {
            log.debug("Invalid ttl"); //$NON-NLS-1$
            return false;
        }

        this.handled++;
        if ( log.isDebugEnabled() && this.handled % 10000 == 0 ) {
            log.debug("At " + this.handled); //$NON-NLS-1$
        }

        if ( this.logProcessor != null ) {
            canonicalize(readEntry);
            return this.logProcessor.process(readEntry, this);
        }

        return true;
    }


    /**
     * 
     */
    @Override
    public void reenable () {
        synchronized ( this.inhibitedLock ) {
            if ( this.inhibited ) {
                this.inhibited = false;
                this.inhibitedLock.notify();
            }
        }
    }


    /**
     * @param readEntry
     */
    private void canonicalize ( Map<String, Object> readEntry ) {
        for ( String key : this.removeProperties ) {
            readEntry.remove(key);
        }

        for ( Entry<String, String> entry : this.mapProperties.entrySet() ) {
            if ( readEntry.containsKey(entry.getKey()) ) {
                readEntry.put(entry.getValue(), readEntry.remove(entry.getKey()));
            }
        }
    }


    /**
     * @return the handled
     */
    public long getHandled () {
        return this.handled;
    }


    /**
     * @param cursor
     * 
     */
    @Override
    public void checkpoint ( String cursor ) {
        if ( StringUtils.isBlank(cursor) ) {
            return;
        }
        this.lastKnownCursor = cursor;
        if ( log.isDebugEnabled() ) {
            log.debug("Writing checkpoint " + cursor); //$NON-NLS-1$
        }

        if ( this.checkpointFileName != null ) {
            try {
                Path chkpointPath = Paths.get(this.checkpointFileName);
                Files.write(chkpointPath, this.lastKnownCursor.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
            }
            catch ( IOException e ) {
                log.warn("Failed to write log checkpoint", e); //$NON-NLS-1$
            }
        }

    }


    /**
     * @param dis
     * @param r
     * @return
     * @throws IOException
     */
    private Map<String, Object> readEntry ( InputStream is ) throws IOException {
        Map<String, Object> fields = new HashMap<>();
        try {

            log.trace("Reading entry..."); //$NON-NLS-1$
            while ( readField(is, fields) ) {}
            if ( log.isTraceEnabled() ) {
                log.trace("Entry complete " + fields); //$NON-NLS-1$
            }

            if ( fields.isEmpty() ) {
                return null;
            }

            String monotonic = (String) fields.get("__MONOTONIC_TIMESTAMP"); //$NON-NLS-1$
            if ( !StringUtils.isBlank(monotonic) ) {
                long monoTs = Long.parseLong(monotonic);

                if ( monoTs < this.lastMonotonic ) {
                    log.trace("Last monotonic time is later " + fields); //$NON-NLS-1$
                }
                this.lastMonotonic = monoTs;
            }

            return fields;
        }
        catch ( JournalParseException e ) {
            log.error("Failed to read journal entry", e); //$NON-NLS-1$
            resync(is);
        }

        return null;
    }


    private static void resync ( InputStream is ) throws IOException {
        log.debug("Resyncing stream"); //$NON-NLS-1$
        int read = -1;
        while ( ( read = is.read() ) != '\n' || is.read() != '\n' ) {
            if ( read < 0 ) {
                throw new IOException("EOF while resyncing"); //$NON-NLS-1$
            }
            continue;
        }
    }


    /**
     * @param is
     * @param r
     * @param fields
     * @throws IOException
     * @throws JournalParseException
     */
    private boolean readField ( InputStream is, Map<String, Object> fields ) throws IOException, JournalParseException {
        StringBuilder sb = getStringBuilder();
        int c = readUTF8String(is, '=', sb);

        if ( c == -1 ) {
            return false;
        }

        boolean singleLine = c == '=';
        if ( !singleLine && c != '\n' ) {
            throw new JournalParseException("Expected newline for multi-line field"); //$NON-NLS-1$
        }

        String fieldName = sb.toString();
        if ( fieldName.isEmpty() ) {
            log.trace("Found end of entry"); //$NON-NLS-1$
            return false;
        }

        if ( log.isTraceEnabled() ) {
            log.trace("Found field " + fieldName); //$NON-NLS-1$
        }

        if ( "-- Reboot --".equals(fieldName) ) { //$NON-NLS-1$
            // these markers should not be in the export format, but it seems they are
            return false;
        }

        c = -1;
        String val = readValue(is, singleLine, fieldName);

        if ( log.isTraceEnabled() ) {
            log.trace("Value is " + val); //$NON-NLS-1$
        }

        if ( fields.put(fieldName, val) != null ) {
            log.debug("Duplicate field in entry, replacing with last " + fieldName); //$NON-NLS-1$
            log.debug(fields);
        }
        return true;
    }


    /**
     * @param is
     * @return
     * @throws IOException
     */
    private int readUTF8String ( InputStream is, Character extraStopChar, StringBuilder sb ) throws IOException {
        int c;
        this.decoder.reset();
        this.buf.clear();
        this.cbuf.clear();
        while ( ( c = is.read() ) >= 0 && ! ( ( extraStopChar != null && extraStopChar == c ) || ( c < 32 && c != 9 ) ) ) {
            long len = getCodepointLength(c);
            if ( this.buf.remaining() < len ) {
                flushBuffer(sb, false);
            }
            SMALL_BUFFER[ 0 ] = (byte) c;
            if ( len > 1 ) {
                readFully(is, SMALL_BUFFER, 1, (int) len);
            }
            this.buf.put(SMALL_BUFFER, 0, (int) len);
        }

        flushBuffer(sb, true);
        return c;
    }


    /**
     * @param c
     * @return
     */
    protected static long getCodepointLength ( int c ) {
        if ( c < 128 ) {
            return 1;
        }
        else if ( c >= 128 && c < 192 ) {
            return 2;
        }
        else if ( c >= 192 && c < 224 ) {
            return 3;
        }
        else if ( c >= 224 && c < 240 ) {
            return 4;
        }
        else if ( c >= 240 && c < 248 ) {
            return 5;
        }
        return 6;
    }


    /**
     * @param buf
     * @param cbuf
     * @param sb
     */
    private void flushBuffer ( StringBuilder sb, boolean eod ) {

        this.buf.flip();
        this.decoder.decode(this.buf, this.cbuf, eod);
        this.buf.flip();

        if ( eod || this.cbuf.remaining() < 10 ) {
            this.cbuf.flip();
            if ( this.cbuf.hasRemaining() ) {
                sb.append(this.cbuf.array(), this.cbuf.position(), this.cbuf.limit());
                this.cbuf.clear();
            }
            else {
                this.cbuf.flip();
            }
        }

    }


    /**
     * @param is
     * @param r
     * @param singleLine
     * @param fieldName
     * @return
     * @throws IOException
     * @throws JournalParseException
     */
    protected String readValue ( InputStream is, boolean singleLine, String fieldName ) throws IOException, JournalParseException {
        if ( singleLine ) {
            return readSimpleValue(is);
        }

        return readBinaryValue(is, fieldName);

    }


    /**
     * @param is
     * @param r
     * @param fieldName
     * @param val
     * @return
     * @throws IOException
     * @throws JournalParseException
     */
    protected String readBinaryValue ( InputStream is, String fieldName ) throws IOException, JournalParseException {
        byte[] val = null;
        int c;
        long size = readLongLE(is);
        if ( log.isTraceEnabled() ) {
            log.trace("Field size is " + size); //$NON-NLS-1$
        }

        if ( size < 0 || size > Integer.MAX_VALUE ) {
            throw new JournalParseException("Invalid size " + size); //$NON-NLS-1$
        }

        if ( size > this.binarySizeLimit ) {
            log.warn(String.format("Field size %d exceeds limit %d, truncating %s", size, this.binarySizeLimit, fieldName)); //$NON-NLS-1$
            val = new byte[this.binarySizeLimit];
            readFully(is, val, 0, this.binarySizeLimit);

            long toSkip = size - this.binarySizeLimit;
            while ( toSkip > 0 ) {
                // skip seems to be problematic on the process input stream
                if ( toSkip > BUFFER_CAPACITY ) {
                    toSkip -= is.read(BUFFER, 0, BUFFER_CAPACITY);
                }
                else {
                    toSkip -= is.read(BUFFER, 0, (int) toSkip);
                }
            }
        }
        else {
            val = new byte[(int) size];
            readFully(is, val, 0, (int) size);
        }

        c = is.read();
        if ( c != '\n' ) {
            throw new JournalParseException("Missing newline after binary field " + c); //$NON-NLS-1$
        }

        try {
            return this.decoder.decode(ByteBuffer.wrap(val)).toString();
        }
        catch ( CharacterCodingException e ) {
            log.debug("Failure decoding field, actual binary data? Falling back to base64.", e); //$NON-NLS-1$
            return this.encoder.encodeToString(val);
        }
    }


    /**
     * @param r
     * @param c
     * @return
     * @throws IOException
     * @throws JournalParseException
     */
    protected String readSimpleValue ( InputStream is ) throws IOException, JournalParseException {
        StringBuilder sb = getStringBuilder();
        int c = readUTF8String(is, null, sb);
        if ( c != '\n' ) {
            throw new JournalParseException("Invalid field value"); //$NON-NLS-1$
        }
        return sb.toString();
    }


    private static long readLongLE ( InputStream in ) throws IOException {
        byte[] sizeBytes = new byte[8];
        readFully(in, sizeBytes, 0, 8);
        long val = ( sizeBytes[ 0 ] & 0xFF );
        val |= ( sizeBytes[ 1 ] & 0xFF ) << 8;
        val |= ( sizeBytes[ 2 ] & 0xFF ) << 16;
        val |= ( sizeBytes[ 3 ] & 0xFF ) << 24;
        val |= ( sizeBytes[ 4 ] & 0xFF ) << 32;
        val |= ( sizeBytes[ 5 ] & 0xFF ) << 40;
        val |= ( sizeBytes[ 6 ] & 0xFF ) << 48;
        val |= ( sizeBytes[ 7 ] & 0xFF ) << 56;
        return val;
    }


    /**
     * @param in
     * @param sizeBytes
     * @throws IOException
     */
    protected static void readFully ( InputStream in, byte[] sizeBytes, int start, int end ) throws IOException {
        int pos = start;
        while ( pos < end ) {
            int read = in.read(sizeBytes, pos, end - pos);
            if ( read < 0 ) {
                throw new IOException("Incomplete data for long"); //$NON-NLS-1$
            }
            pos += read;
        }
    }


    /**
     * @return
     */
    protected StringBuilder getStringBuilder () {
        int length = this.stringBuilder.length();
        if ( length > 0 ) {
            this.stringBuilder.delete(0, length);
        }
        return this.stringBuilder;
    }


    /**
     * @return
     * @throws IOException
     */
    protected Process launchListener () throws IOException {
        List<String> args = new ArrayList<>(Arrays.asList(
            this.journalCtlPath,
            "-f", //$NON-NLS-1$
            "-o", //$NON-NLS-1$
            "export" //$NON-NLS-1$
        ));

        if ( this.restoreLimit >= 0 ) {
            args.add("-n"); //$NON-NLS-1$
            args.add(String.valueOf(this.restoreLimit));
        }
        else {
            args.add("--no-tail"); //$NON-NLS-1$
        }

        String cursor = getLastKnownCursor();
        if ( !StringUtils.isBlank(cursor) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Resuming from cursor " + cursor); //$NON-NLS-1$
            }
            args.add("-c"); //$NON-NLS-1$
            args.add(cursor);
        }
        else {
            log.debug("Recover journal from beginning"); //$NON-NLS-1$
        }

        ProcessBuilder processBuilder = new ProcessBuilder(); // $NON-NLS-1$
        processBuilder.command(args);
        processBuilder.directory(new File("/")); //$NON-NLS-1$
        processBuilder.environment().clear();

        processBuilder.redirectInput(Redirect.PIPE);
        processBuilder.redirectOutput(Redirect.PIPE);
        processBuilder.redirectError(Redirect.INHERIT);

        Process p = processBuilder.start();
        p.getOutputStream().close();
        return p;
    }


    /**
     * @return
     */
    private String getLastKnownCursor () {
        if ( !this.restoredCheckpoint && this.lastKnownCursor == null && this.checkpointFileName != null ) {
            try {
                this.restoredCheckpoint = true;
                Path chkpointPath = Paths.get(this.checkpointFileName);
                if ( Files.exists(chkpointPath) ) {
                    byte[] checkpointData = Files.readAllBytes(chkpointPath);
                    this.lastKnownCursor = new String(checkpointData, StandardCharsets.UTF_8);
                }
            }
            catch ( IOException e ) {
                log.warn("Failed to restore log checkpoint", e); //$NON-NLS-1$
            }
        }
        return this.lastKnownCursor;
    }

}
