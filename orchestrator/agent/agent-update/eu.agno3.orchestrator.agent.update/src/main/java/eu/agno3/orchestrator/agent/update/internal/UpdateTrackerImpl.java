/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.12.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.update.internal;


import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.connector.QueueingEventProducer;
import eu.agno3.orchestrator.agent.update.UpdateInstallation;
import eu.agno3.orchestrator.agent.update.UpdateTracker;
import eu.agno3.orchestrator.system.base.SystemService;
import eu.agno3.orchestrator.system.base.SystemServiceType;
import eu.agno3.orchestrator.system.base.execution.ExecutionConfigProperties;
import eu.agno3.orchestrator.system.base.units.file.PrefixUtil;
import eu.agno3.orchestrator.system.img.util.SystemImageUtil;
import eu.agno3.orchestrator.system.update.msg.SystemRevertedEvent;
import eu.agno3.orchestrator.system.update.msg.SystemUpdatedEvent;


/**
 * @author mbechler
 *
 */
@Component ( immediate = true, service = {
    UpdateTracker.class, SystemService.class
} )
@SystemServiceType ( UpdateTracker.class )
public class UpdateTrackerImpl implements UpdateTracker {

    /**
     * 
     */

    private static final Logger log = Logger.getLogger(UpdateTrackerImpl.class);

    private QueueingEventProducer eventProducer;

    private static final String RELEASE_STREAM = "RELEASE"; //$NON-NLS-1$
    private static final String UPDATE_FILE = "/var/lib/orchagent/update/update.state"; //$NON-NLS-1$
    private static final String ROLLBACK_FILE = "/var/lib/orchagent/update/rollback.state"; //$NON-NLS-1$
    private static final String REBOOT_FILE = "/run/orchagent/rebootRequired"; //$NON-NLS-1$

    private UpdateInstallation current;
    private UpdateInstallation revert;
    private ExecutionConfigProperties execConfig;


    @Reference
    protected synchronized void setEventProducer ( QueueingEventProducer evp ) {
        this.eventProducer = evp;
    }


    protected synchronized void unsetEventProducer ( QueueingEventProducer evp ) {
        if ( this.eventProducer == evp ) {
            this.eventProducer = null;
        }
    }


    @Reference
    protected synchronized void setExecutionConfig ( ExecutionConfigProperties ec ) {
        this.execConfig = ec;
    }


    protected synchronized void unsetExecutionConfig ( ExecutionConfigProperties ec ) {
        if ( this.execConfig == ec ) {
            this.execConfig = null;
        }
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        UpdateInstallation r = null;
        UpdateInstallation c = null;
        c = fromFile(PrefixUtil.resolvePrefix(this.execConfig, Paths.get(UPDATE_FILE)));
        r = fromFile(PrefixUtil.resolvePrefix(this.execConfig, Paths.get(ROLLBACK_FILE)));
        String applianceBuild = SystemImageUtil.getApplianceBuild();
        if ( c == null && !StringUtils.isBlank(applianceBuild) ) {
            log.debug("Have appliance build from release file"); //$NON-NLS-1$
            c = new UpdateInstallation();
            c.setSequence(Long.parseLong(applianceBuild));
            c.setStream(RELEASE_STREAM); // $NON-NLS-1$
        }

        if ( c == null ) {
            log.debug("No release file"); //$NON-NLS-1$
            c = new UpdateInstallation();
            c.setSequence(0);
            c.setStream(RELEASE_STREAM);
        }

        this.current = c;
        this.revert = r;

        String cmdLine = null;
        try {
            cmdLine = StringUtils.join(Files.readAllLines(Paths.get("/proc/cmdline")), ' '); //$NON-NLS-1$
        }
        catch ( IOException e ) {
            log.error("Failed to read kernel command line", e); //$NON-NLS-1$
        }

        if ( cmdLine != null ) {
            if ( cmdLine.matches("(^| )recovery($| )") ) { //$NON-NLS-1$
                reverted(c);
            }
        }

    }


    /**
     * @param path
     * @return
     */
    private static UpdateInstallation fromFile ( Path path ) {
        if ( !Files.exists(path, LinkOption.NOFOLLOW_LINKS) ) {
            log.debug("File does not exist " + path); //$NON-NLS-1$
            return null;
        }
        try {
            List<String> allLines = Files.readAllLines(path, StandardCharsets.UTF_8);

            if ( allLines.isEmpty() ) {
                log.warn("Release file empty " + path); //$NON-NLS-1$
                return null;
            }

            String firstLine = allLines.get(0);
            int sep = firstLine.indexOf(' ');
            if ( sep < 0 ) {
                log.warn("Invalid file format " + path); //$NON-NLS-1$
                return null;
            }

            int nextSep = firstLine.indexOf(' ', sep + 1);

            String sequence = firstLine.substring(0, sep);
            String stream;
            String backupRef = null;
            if ( nextSep < 0 ) {
                stream = firstLine.substring(sep + 1);
            }
            else {
                stream = firstLine.substring(sep + 1, nextSep);
                backupRef = firstLine.substring(nextSep + 1);
            }

            UpdateInstallation r = new UpdateInstallation();
            r.setSequence(Long.parseLong(sequence));
            r.setStream(stream); // $NON-NLS-1$
            if ( backupRef != null ) {
                r.setReferenceBackupId(UUID.fromString(backupRef));
            }
            r.setInstallDate(new DateTime(Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS).toMillis()));
            return r;
        }
        catch (
            IOException |
            IllegalArgumentException e ) {
            log.warn("Failed to read file " + path, e); //$NON-NLS-1$
            return null;
        }
    }


    private void writeTo ( Path path, UpdateInstallation inst ) {
        Path p = PrefixUtil.resolvePrefix(this.execConfig, path);
        if ( this.execConfig.isAlwaysCreateTargets() ) {
            try {
                Files.createDirectories(p.getParent());
            }
            catch ( IOException e ) {
                log.error("Failed to create target dir", e); //$NON-NLS-1$
            }
        }

        try ( FileChannel ch = FileChannel.open(p, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
              OutputStream os = Channels.newOutputStream(ch);
              FileLock l = ch.lock() ) {
            os.write(String.format("%d %s", inst.getSequence(), inst.getStream()).getBytes(StandardCharsets.UTF_8)); //$NON-NLS-1$
            if ( inst.getReferenceBackupId() != null ) {
                os.write(String.format(" %s", inst.getReferenceBackupId()).getBytes(StandardCharsets.UTF_8)); //$NON-NLS-1$
            }
        }
        catch ( IOException e ) {
            log.error("Failed to write file " + path, e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.update.UpdateTracker#getCurrent()
     */
    @Override
    public UpdateInstallation getCurrent () {
        return this.current;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.update.UpdateTracker#getRevert()
     */
    @Override
    public UpdateInstallation getRevert () {
        return this.revert;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.update.UpdateTracker#isRebootIndicated()
     */
    @Override
    public boolean isRebootIndicated () {
        return Files.exists(Paths.get(REBOOT_FILE));
    }


    @Override
    public void updated ( UpdateInstallation inst, boolean suggestReboot ) {
        log.info("Updated system to " + inst); //$NON-NLS-1$
        if ( this.current != null && this.current.getSequence() > 0 ) {
            this.revert = this.current;
            writeTo(Paths.get(ROLLBACK_FILE), this.current);
        }
        this.current = inst;
        writeTo(Paths.get(UPDATE_FILE), inst);

        if ( suggestReboot ) {
            try {
                Files.write(Paths.get(REBOOT_FILE), new byte[0]);
            }
            catch ( IOException e ) {
                log.warn("Failed to write reboot indicator file", e); //$NON-NLS-1$
            }
        }

        SystemUpdatedEvent systemUpdatedEvent = new SystemUpdatedEvent(this.eventProducer.getMessageSource());
        systemUpdatedEvent.setRebootIndicated(suggestReboot);
        systemUpdatedEvent.setUpdatedSequence(inst.getSequence());
        systemUpdatedEvent.setUpdatedStream(inst.getStream());
        this.eventProducer.publish(systemUpdatedEvent);
    }


    private void reverted ( UpdateInstallation inst ) {
        log.info("Reverted system to " + inst); //$NON-NLS-1$
        this.current = inst;
        try {
            Files.deleteIfExists(Paths.get(ROLLBACK_FILE));
        }
        catch ( IOException e ) {
            log.error("Failed to delete rollback state file", e); //$NON-NLS-1$
        }

        SystemRevertedEvent systemRevertedEvent = new SystemRevertedEvent(this.eventProducer.getMessageSource());
        systemRevertedEvent.setRevertedToSequence(inst.getSequence());
        systemRevertedEvent.setRevertedToStream(inst.getStream());
        this.eventProducer.publish(systemRevertedEvent);
    }

}
