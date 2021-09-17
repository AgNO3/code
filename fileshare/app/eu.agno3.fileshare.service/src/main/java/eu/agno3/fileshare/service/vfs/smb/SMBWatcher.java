/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.10.2015 by mbechler
 */
package eu.agno3.fileshare.service.vfs.smb;


import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.log4j.Logger;

import eu.agno3.fileshare.service.vfs.FilesystemWatchListener;

import jcifs.CIFSContext;
import jcifs.FileNotifyInformation;
import jcifs.SmbResource;
import jcifs.SmbWatchHandle;
import jcifs.smb.NtStatus;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;


/**
 * @author mbechler
 *
 */
public class SMBWatcher implements Runnable {

    private static final Logger log = Logger.getLogger(SMBWatcher.class);

    private static final int BASE_BACKOFF = 1000;
    private static final int MAX_BACKOFF = 1000 * 60 * 5;

    private boolean exit;
    private Thread thread;

    private int backOff = 30 * 3000;

    private FilesystemWatchListener<String, FileNotifyInformation> listener;
    private URL root;

    private SmbResource rootFile;


    /**
     * @param id
     * @param ctx
     * @param root
     * @param listener
     * @throws IOException
     * 
     */
    public SMBWatcher ( String id, CIFSContext ctx, URL root, FilesystemWatchListener<String, FileNotifyInformation> listener ) throws IOException {
        this.root = root;
        this.listener = listener;
        this.exit = false;
        this.thread = new Thread(this, "VFS-SMB-Watch-" + id); //$NON-NLS-1$
        this.thread.start();
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

    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run () {
        boolean isRetry = false;
        try ( SmbResource r = getRootFile() ) {
            while ( !this.exit ) {
                log.trace("Setting up watch"); //$NON-NLS-1$
                try ( SmbWatchHandle wh = r.watch(0xFFF, true) ) {
                    while ( true ) {
                        List<FileNotifyInformation> watch = wh.call();
                        if ( isRetry ) {
                            log.info("Watch restored"); //$NON-NLS-1$
                            isRetry = false;
                        }
                        this.backOff = BASE_BACKOFF;

                        if ( watch == null || watch.isEmpty() ) {
                            log.warn("Empty result returned"); //$NON-NLS-1$
                            continue;
                        }

                        for ( FileNotifyInformation fi : watch ) {
                            if ( log.isDebugEnabled() ) {
                                log.debug("Have change: " + fi); //$NON-NLS-1$
                            }
                            this.listener.fileChanged(fi.getFileName(), fi);
                        }
                    }
                }
                catch ( SmbAuthException e ) {
                    throw e;
                }
                catch ( IOException e ) {
                    Throwable rootCause = unwrapRootCause(e);
                    if ( rootCause instanceof InterruptedException ) {
                        throw (InterruptedException) rootCause;
                    }
                    if ( Thread.currentThread().isInterrupted() ) {
                        return;
                    }
                    log.warn(String.format("Watch failed, waiting %d seconds", +this.backOff / 1000), e); //$NON-NLS-1$
                    Thread.sleep(this.backOff);
                    if ( e instanceof SmbException ) {
                        if ( ( (SmbException) e ).getNtStatus() == NtStatus.NT_STATUS_INVALID_HANDLE ) {
                            if ( log.isDebugEnabled() ) {
                                log.debug("Reopening file " + this.root); //$NON-NLS-1$
                            }
                            this.rootFile = (SmbFile) this.root.openConnection();
                        }
                    }
                    isRetry = true;
                    this.backOff = Math.min(this.backOff * 2, MAX_BACKOFF);
                    continue;
                }

            }
        }
        catch ( InterruptedException e ) {
            log.debug("Interrupted", e); //$NON-NLS-1$
        }
        catch ( Exception e ) {
            log.warn("Persistent error, exiting", e); //$NON-NLS-1$
        }
    }


    /**
     * @param e
     * @return
     */
    private Throwable unwrapRootCause ( Throwable e ) {
        if ( e.getCause() != null ) {
            return unwrapRootCause(e.getCause());
        }
        return e;
    }


    /**
     * @return
     * @throws IOException
     */
    private SmbResource getRootFile () throws IOException {
        if ( this.rootFile == null ) {
            this.rootFile = (SmbFile) this.root.openConnection();
        }
        return this.rootFile;
    }
}