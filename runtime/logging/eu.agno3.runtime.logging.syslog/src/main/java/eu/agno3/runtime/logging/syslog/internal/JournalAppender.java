/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.08.2014 by mbechler
 */
package eu.agno3.runtime.logging.syslog.internal;


import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.graylog2.syslog4j.SyslogConstants;
import org.graylog2.syslog4j.SyslogRuntimeException;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.ops4j.pax.logging.spi.PaxLayout;
import org.ops4j.pax.logging.spi.PaxLocationInfo;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import eu.agno3.runtime.logging.Appender;
import eu.agno3.runtime.logging.layouts.WithExceptionLayout;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 * 
 */
@Component ( service = Appender.class, configurationPid = SyslogConfiguration.JORNAL_PID, configurationPolicy = ConfigurationPolicy.REQUIRE )
public class JournalAppender implements Appender {

    private static final Logger log = Logger.getLogger(JournalAppender.class);

    private static final String JOURNAL_SOCKET_PATH = "/run/systemd/journal/socket"; //$NON-NLS-1$

    private static final Charset UTF8 = Charset.forName("UTF-8"); //$NON-NLS-1$

    private static final int MAX_SENDMSG_SIZE = 16384;

    /**
     * 
     */
    private PaxLayout layout = new WithExceptionLayout();

    private AFUNIXSocket sock;

    private String loggerName;

    private String loggerFacility;

    private String loggerSocket;


    @Activate
    protected void activate ( ComponentContext ctx ) {
        this.loggerName = ConfigUtil.parseString(ctx.getProperties(), SyslogConfiguration.NAME, "runtime"); //$NON-NLS-1$
        this.loggerFacility = String.valueOf(ConfigUtil.parseInt(ctx.getProperties(), SyslogConfiguration.FACILITY, SyslogConstants.FACILITY_DAEMON));
        this.loggerSocket = ConfigUtil.parseString(ctx.getProperties(), SyslogConfiguration.SOCKET, JOURNAL_SOCKET_PATH);

        if ( !AFUNIXSocket.isSupported() ) {
            throw new SyslogRuntimeException("UNIX Socket support is not avaialble"); //$NON-NLS-1$
        }

        connect();
    }


    /**
     * 
     */
    private void connect () {
        try {
            AFUNIXSocketAddress addr = new AFUNIXSocketAddress(new File(this.loggerSocket), 0, false, true);
            this.sock = AFUNIXSocket.connectTo(addr);
            this.sock.setPassCred(true);
        }
        catch ( IOException e ) {
            log.warn("Failed to open journal socket", e); //$NON-NLS-1$
        }
    }


    @Deactivate
    protected void deactivate ( ComponentContext ctx ) {

        try {
            this.sock.close();
        }
        catch ( IOException e ) {
            log.warn("Failed to close journal socket", e); //$NON-NLS-1$
        }
    }


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL, target = "(appender=journal)" )
    protected synchronized void setLayout ( PaxLayout l ) {
        this.layout = l;
    }


    protected synchronized void unsetLayout ( PaxLayout l ) {
        if ( this.layout == l ) {
            this.layout = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.ops4j.pax.logging.spi.PaxAppender#doAppend(org.ops4j.pax.logging.spi.PaxLoggingEvent)
     */
    @Override
    public void doAppend ( PaxLoggingEvent ev ) {

        if ( log.getName().equals(ev.getLoggerName()) ) {
            return;
        }

        if ( this.sock == null || !this.sock.isConnected() ) {
            connect();
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            long len = makeMsg(ev, bos, MAX_SENDMSG_SIZE);
            if ( len >= 0 ) {
                this.sock.getOutputStream().sendmsg(bos.toByteArray(), true);
                return;
            }

            Path tmpFile = Files.createTempFile(
                Paths.get("/dev/shm/"), //$NON-NLS-1$
                "journal", //$NON-NLS-1$
                ".tmp"); //$NON-NLS-1$

            try ( FileOutputStream fos = new FileOutputStream(tmpFile.toFile());
                  FileInputStream fis = new FileInputStream(tmpFile.toFile()) ) {
                makeMsg(ev, fos, -1);
                fos.flush();
                this.sock.getOutputStream().sendfd(fis.getFD());
            }
            finally {
                Files.deleteIfExists(tmpFile);
            }
        }
        catch ( IOException e ) {
            log.warn("Failed to write to journal", e); //$NON-NLS-1$
        }

    }


    /**
     * @param ev
     * @param b
     * @throws IOException
     */
    private long makeMsg ( PaxLoggingEvent ev, OutputStream b, long sizeLimit ) throws IOException {
        long len = 0;
        String msg = ev.getMessage();
        if ( this.layout != null ) {
            msg = this.layout.doLayout(ev);
        }

        len += writeVariable(b, "PRIORITY", String.valueOf(ev.getLevel().getSyslogEquivalent())); //$NON-NLS-1$
        len += writeVariable(b, "MESSAGE", msg); //$NON-NLS-1$
        len += writeVariable(b, "SYSLOG_FACILITY", this.loggerFacility); //$NON-NLS-1$
        len += writeVariable(b, "SYSLOG_IDENTIFIER", this.loggerName); //$NON-NLS-1$

        if ( sizeLimit > 0 && len > sizeLimit ) {
            return -1;
        }

        if ( !StringUtils.isBlank(ev.getThreadName()) ) {
            len += writeVariable(b, "THREAD_NAME", ev.getThreadName()); //$NON-NLS-1$
        }
        len += writeVariable(b, "LOG4J_LOGGER", ev.getLoggerName()); //$NON-NLS-1$
        len += writeLocationInformation(ev, b);

        if ( sizeLimit > 0 && len > sizeLimit ) {
            return -1;
        }

        if ( ev.getThrowableStrRep() != null && ev.getThrowableStrRep().length > 0 ) {
            len += writeVariable(b, "STACKTRACE", StringUtils.join(ev.getThrowableStrRep(), '\n')); //$NON-NLS-1$
        }

        if ( sizeLimit > 0 && len > sizeLimit ) {
            return -1;
        }

        if ( ev.getProperties() != null && !ev.getProperties().isEmpty() ) {
            for ( Entry<Object, Object> e : ( (Map<Object, Object>) ev.getProperties() ).entrySet() ) {
                len += writeVariable(b, normalizeKey(String.valueOf(e.getKey())), String.valueOf(e.getValue()));
            }
        }

        if ( sizeLimit > 0 && len > sizeLimit ) {
            return -1;
        }

        return len;
    }


    /**
     * @param ev
     * @param b
     * @throws IOException
     */
    private static long writeLocationInformation ( PaxLoggingEvent ev, OutputStream b ) throws IOException {
        long len = 0;
        if ( ev.getLocationInformation() != null ) {
            PaxLocationInfo locationInformation = ev.getLocationInformation();

            if ( !StringUtils.isBlank(locationInformation.getFileName()) ) {
                len += writeVariable(b, "CODE_FILE", locationInformation.getFileName()); //$NON-NLS-1$
            }

            if ( !StringUtils.isBlank(locationInformation.getLineNumber()) ) {
                len += writeVariable(b, "CODE_LINE", locationInformation.getLineNumber()); //$NON-NLS-1$
            }

            if ( !StringUtils.isBlank(locationInformation.getMethodName()) ) {
                StringBuilder sb = new StringBuilder();
                if ( !StringUtils.isBlank(locationInformation.getClassName()) ) {
                    sb.append(locationInformation.getClassName());
                    sb.append('.');
                }
                sb.append(locationInformation.getMethodName());
                len += writeVariable(b, "CODE_FUNC", sb.toString()); //$NON-NLS-1$
            }
        }

        return len;
    }


    /**
     * @param b
     * @param property
     * @param value
     * @throws IOException
     */
    private static long writeVariable ( OutputStream b, String property, String value ) throws IOException {

        if ( !isValidProperty(property) ) {
            return 0;
        }

        DataOutputStream dos = new DataOutputStream(b);
        byte[] propBytes = property.getBytes(UTF8);
        byte[] valueBytes = value.getBytes(UTF8);
        if ( value.indexOf('\n') >= 0 ) {
            dos.write(propBytes);
            dos.write('\n');
            writeLongLE(dos, value.length());
            dos.write(valueBytes);
            dos.write('\n');
            return propBytes.length + valueBytes.length + 2 + 8;
        }

        dos.write(propBytes);
        dos.write('=');
        dos.write(valueBytes);
        dos.write('\n');
        return propBytes.length + valueBytes.length + 2;
    }


    private static String normalizeKey ( String key ) {
        return key.toUpperCase().replaceAll(
            "[^_A-Z0-9]", //$NON-NLS-1$
            "_"); //$NON-NLS-1$
    }


    /**
     * @param property
     * @return
     */
    private static boolean isValidProperty ( String property ) {
        if ( property.isEmpty() ) {
            return false;
        }

        if ( property.charAt(0) == '_' ) {
            return false;
        }

        for ( char c : property.toCharArray() ) {
            if ( ! ( 'A' <= c && c <= 'Z' || '0' <= c && c <= '9' || c == '_' ) ) {
                return false;
            }
        }
        return true;
    }


    private static void writeLongLE ( DataOutputStream out, long value ) throws IOException {
        out.writeByte((int) ( value & 0xFF ));
        out.writeByte((int) ( ( value >> 8 ) & 0xFF ));
        out.writeByte((int) ( ( value >> 16 ) & 0xFF ));
        out.writeByte((int) ( ( value >> 24 ) & 0xFF ));
        out.writeByte((int) ( ( value >> 32 ) & 0xFF ));
        out.writeByte((int) ( ( value >> 40 ) & 0xFF ));
        out.writeByte((int) ( ( value >> 48 ) & 0xFF ));
        out.writeByte((int) ( ( value >> 56 ) & 0xFF ));
    }
}
