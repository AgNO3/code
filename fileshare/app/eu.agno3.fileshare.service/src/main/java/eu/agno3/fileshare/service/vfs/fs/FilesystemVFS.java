/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.10.2015 by mbechler
 */
package eu.agno3.fileshare.service.vfs.fs;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent.Kind;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.exceptions.EntityNotFoundException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.service.api.internal.RecursiveModificationTimeTracker;
import eu.agno3.fileshare.service.vfs.AbstractVFS;
import eu.agno3.fileshare.service.vfs.FilesystemWatchListener;
import eu.agno3.fileshare.vfs.VFS;
import eu.agno3.fileshare.vfs.VFSContext;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.db.orm.EntityTransactionException;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = VFS.class, configurationPid = "vfs.fs", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class FilesystemVFS extends AbstractVFS implements VFS, FilesystemWatchListener<Path, Kind<?>> {

    private static final Logger log = Logger.getLogger(FilesystemVFS.class);

    private Path root;

    private FilesystemWatcher watcher;

    private RecursiveModificationTimeTracker modTracker;


    @Reference
    protected synchronized void setRecursiveModTracker ( RecursiveModificationTimeTracker rmt ) {
        this.modTracker = rmt;
    }


    protected synchronized void unsetRecursiveModTracker ( RecursiveModificationTimeTracker rmt ) {
        if ( this.modTracker == rmt ) {
            this.modTracker = null;
        }
    }


    @Override
    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        String path = ConfigUtil.parseString(ctx.getProperties(), "path", null); //$NON-NLS-1$

        Path p = Paths.get(path);

        if ( !Files.exists(p) || !Files.isReadable(p) ) {
            log.error("Cannot read VFS backend path " + p); //$NON-NLS-1$
            return;
        }
        this.root = p;

        super.activate(ctx);

        if ( this.isInitialized() ) {
            try {

                this.watcher = new FilesystemWatcher(this.root, this);
            }
            catch ( IOException e ) {
                log.error("Failed to create filesystem watch service", e); //$NON-NLS-1$
            }
        }

    }


    @Override
    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        if ( this.watcher != null ) {
            this.watcher.close();
            this.watcher = null;
        }

        super.deactivate(ctx);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.vfs.FilesystemWatchListener#fileChanged(java.lang.Object, java.lang.Object)
     */
    @Override
    public void fileChanged ( Path p, Kind<?> kind ) {
        if ( !p.startsWith(this.root) ) {
            return;
        }

        Path rel = this.root.relativize(p);

        try ( VFSContext v = begin(true) ) {
            DateTime modTime;
            if ( kind == StandardWatchEventKinds.ENTRY_DELETE ) {
                rel = rel.getParent();
                modTime = new DateTime(Files.getLastModifiedTime(p.getParent()).toMillis());
            }
            else {
                modTime = new DateTime(Files.getLastModifiedTime(p, LinkOption.NOFOLLOW_LINKS).toMillis());
            }

            log.debug("File changed " + rel); //$NON-NLS-1$

            VFSContainerEntity e = v.getRoot();
            String relPath = rel.toString();
            VFSEntity entity = v.resolveRelative(e, StringUtils.split(relPath, '/'));

            entity.setLastModified(modTime);
            this.modTracker.trackUpdate(v, entity);
        }
        catch (
            FileshareException |
            IOException e ) {
            log.warn("Failed to get modified entity", e); //$NON-NLS-1$
        }

    }


    /**
     * {@inheritDoc}
     * 
     * @throws FileshareException
     * 
     * @see eu.agno3.fileshare.vfs.VFS#begin(boolean)
     */
    @Override
    public VFSContext begin ( boolean readOnly ) throws FileshareException {
        if ( !this.isInitialized() ) {
            throw new EntityNotFoundException();
        }
        try {
            return new FilesystemVFSContext(this.getServiceContext().getFileshareEntityTS(), readOnly, this.root, this);
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to start entity transaction", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFS#begin(eu.agno3.runtime.db.orm.EntityTransactionContext)
     */
    @Override
    public VFSContext begin ( EntityTransactionContext ctx ) throws FileshareException {
        if ( !this.isInitialized() ) {
            throw new EntityNotFoundException();
        }

        return new FilesystemVFSContext(this, ctx, this.root);

    }
}
