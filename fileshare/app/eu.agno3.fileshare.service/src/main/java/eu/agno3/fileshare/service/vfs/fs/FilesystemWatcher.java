/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.10.2015 by mbechler
 */
package eu.agno3.fileshare.service.vfs.fs;


import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.agno3.fileshare.service.vfs.FilesystemWatchListener;


/**
 * @author mbechler
 *
 */
public class FilesystemWatcher implements Runnable {

    private static final Logger log = Logger.getLogger(FilesystemWatcher.class);

    private Map<WatchKey, Path> keys = new HashMap<>();
    private WatchService watch;
    private boolean exit;
    private Thread thread;

    private FilesystemWatchListener<Path, Kind<?>> listener;


    /**
     * @param root
     * @param listener
     * @throws IOException
     * 
     */
    public FilesystemWatcher ( Path root, FilesystemWatchListener<Path, Kind<?>> listener ) throws IOException {
        this.listener = listener;
        this.exit = false;
        this.watch = root.getFileSystem().newWatchService();
        this.thread = new Thread(this, "VFS-Watch-" + root.getFileName()); //$NON-NLS-1$
        registerPathRecursive(root);
        this.thread.start();
    }


    /**
     * @param root
     * @throws IOException
     */
    void registerPath ( Path root ) throws IOException {
        if ( log.isDebugEnabled() ) {
            log.debug("Register path " + root); //$NON-NLS-1$
        }
        WatchKey key = root.register(
            this.watch,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_MODIFY,
            StandardWatchEventKinds.ENTRY_DELETE);
        this.keys.put(key, root);
    }


    private void registerPathRecursive ( Path root ) throws IOException {
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory ( Path dir, BasicFileAttributes attrs ) throws IOException {
                registerPath(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }


    /**
     * 
     */
    public void close () {
        this.exit = true;

        if ( this.thread != null ) {
            this.thread.interrupt();
            try {
                this.thread.join();
            }
            catch ( InterruptedException e ) {
                log.warn("Interrupted waiting for watch thread", e); //$NON-NLS-1$
            }
            this.thread = null;
        }

        if ( this.watch != null ) {
            try {
                this.watch.close();
            }
            catch ( IOException e ) {
                log.error("Failed to close watch service", e); //$NON-NLS-1$
            }
            this.watch = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run () {
        try {
            while ( !this.exit ) {
                WatchKey key = this.watch.take();
                Path base = this.keys.get(key);

                for ( WatchEvent<?> watchEvent : key.pollEvents() ) {
                    Kind<?> kind = watchEvent.kind();
                    if ( kind == StandardWatchEventKinds.OVERFLOW ) {
                        log.warn("Overflow in file watcher"); //$NON-NLS-1$
                        continue;
                    }

                    Path p = base.resolve((Path) watchEvent.context());

                    if ( log.isDebugEnabled() ) {
                        log.debug(String.format("%s: %s", kind.name(), p)); //$NON-NLS-1$
                    }

                    if ( kind == StandardWatchEventKinds.ENTRY_CREATE && Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS) ) {
                        try {
                            if ( log.isDebugEnabled() ) {
                                log.debug("Adding new directory " + p); //$NON-NLS-1$
                            }
                            registerPathRecursive(p);
                        }
                        catch ( IOException e ) {
                            log.warn("Failed to add recursive watch on new directory " + p, e); //$NON-NLS-1$
                        }
                    }

                    this.listener.fileChanged(p, kind);
                }

                if ( !key.reset() ) {
                    this.keys.remove(key);
                    if ( this.keys.isEmpty() ) {
                        log.warn("All keys removed"); //$NON-NLS-1$
                        return;
                    }
                }
            }
        }
        catch ( InterruptedException e ) {
            log.trace("Interrupted", e); //$NON-NLS-1$
        }
    }
}