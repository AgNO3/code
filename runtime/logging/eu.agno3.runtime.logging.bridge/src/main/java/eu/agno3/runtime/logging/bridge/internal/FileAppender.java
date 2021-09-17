/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.10.2014 by mbechler
 */
package eu.agno3.runtime.logging.bridge.internal;


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.EnumSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.ops4j.pax.logging.spi.PaxLayout;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import eu.agno3.runtime.logging.Appender;


/**
 * @author mbechler
 *
 */
@Component ( service = Appender.class, configurationPid = FileAppender.PID, configurationPolicy = ConfigurationPolicy.REQUIRE )
public class FileAppender implements Appender {

    /**
     * 
     */
    private static final String USER_ONLY_PERM = "rw-------"; //$NON-NLS-1$

    /**
     * 
     */
    private static final String GROUP_PERM = "rw--r----"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(FileAppender.class);

    /**
     * 
     */
    public static final String PID = "log.file"; //$NON-NLS-1$

    private PaxLayout layout;
    private Path file;


    @Activate
    protected synchronized void activate ( ComponentContext context ) {

        String pathSpec = (String) context.getProperties().get("path"); //$NON-NLS-1$
        if ( StringUtils.isBlank(pathSpec) ) {
            log.warn("File log configured but no path provided"); //$NON-NLS-1$
            return;
        }

        this.file = Paths.get(pathSpec.trim());

        String groupSpec = (String) context.getProperties().get("group"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(groupSpec) ) {
            try {
                GroupPrincipal group = FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByGroupName(groupSpec.trim());

                try ( FileChannel ch = FileChannel.open(
                    this.file,
                    EnumSet.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.APPEND),
                    PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString(USER_ONLY_PERM))) ) {
                    PosixFileAttributeView attrs = Files.getFileAttributeView(this.file, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
                    attrs.setGroup(group);
                    attrs.setPermissions(PosixFilePermissions.fromString(GROUP_PERM));
                }
            }
            catch ( IOException e ) {
                log.warn("Failed to set group permissions", e); //$NON-NLS-1$
            }
        }

    }


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL, target = "(appender=file)" )
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
    public synchronized void doAppend ( PaxLoggingEvent ev ) {

        if ( FileAppender.class.getName().equals(ev.getLoggerName()) ) {
            return;
        }

        // there does not seem a working way to flush buffers so opening and
        // closing the file on every message is the only reliable solution.
        try ( FileChannel ch = FileChannel.open(
                  this.file,
                  EnumSet.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.APPEND),
                  PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString(USER_ONLY_PERM)));
              OutputStream fos = Channels.newOutputStream(ch);
              Writer writer = new BufferedWriter(new OutputStreamWriter(fos, Charset.forName("UTF-8"))) ) { //$NON-NLS-1$

            if ( this.layout != null ) {
                writer.write(this.layout.doLayout(ev));
            }
            else {
                writer.write(formatMessage(ev));
                writer.write(System.lineSeparator());
                if ( ev.getThrowableStrRep() != null ) {
                    for ( String line : ev.getThrowableStrRep() ) {
                        writer.write(line);
                        writer.write(System.lineSeparator());
                    }
                }
            }

        }
        catch ( ClosedByInterruptException e ) {
            log.trace("Interrupted write", e); //$NON-NLS-1$
        }
        catch ( IOException e ) {
            log.warn("Failed to write log file", e); //$NON-NLS-1$
        }
    }


    private static String formatMessage ( PaxLoggingEvent ev ) {
        return String.format("[%s] %s", ev.getLevel().toString().substring(0, 4), ev.getRenderedMessage()); //$NON-NLS-1$
    }
}
