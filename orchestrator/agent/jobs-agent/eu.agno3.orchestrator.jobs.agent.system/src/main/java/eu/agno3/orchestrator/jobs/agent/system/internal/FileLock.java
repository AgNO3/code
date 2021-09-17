/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.02.2016 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.system.internal;


import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.jobs.agent.system.ConfigRepositoryException;


/**
 * @author mbechler
 *
 */
public class FileLock {

    private static final Logger log = Logger.getLogger(FileLock.class);

    private Path p;
    private ReadWriteLock rwl = new ReentrantReadWriteLock();
    private FileChannel fileLock;


    /**
     * @param p
     * 
     */
    public FileLock ( Path p ) {
        this.p = p;
    }


    /**
     * @throws IOException
     */
    public void close () throws IOException {
        try {
            if ( log.isDebugEnabled() ) {
                log.debug("Closing file lock " + this.p); //$NON-NLS-1$
            }
            FileChannel fl = this.fileLock;
            if ( fl != null ) {
                fl.close();
            }
        }
        finally {
            this.fileLock = null;
        }
    }


    /**
     * @throws IOException
     * 
     */
    private void ensureFileLock () throws IOException {
        if ( this.fileLock == null || !this.fileLock.isOpen() ) {
            Path lockFile = this.p;
            if ( !Files.exists(lockFile, LinkOption.NOFOLLOW_LINKS) ) {
                Files.write(lockFile, new byte[] {}, StandardOpenOption.CREATE);
            }
            this.fileLock = FileChannel.open(this.p, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            if ( log.isDebugEnabled() ) {
                log.debug("Opening file lock " + this.p); //$NON-NLS-1$
            }

            this.fileLock.lock(0L, Long.MAX_VALUE, false);
        }
    }


    /**
     * @return a lock context for writing
     * @throws ConfigRepositoryException
     */
    public LockContext obtainWriteLock () throws ConfigRepositoryException {
        try {
            Lock l = this.rwl.writeLock();
            if ( log.isDebugEnabled() ) {
                log.debug("Locking for write " + l); //$NON-NLS-1$
            }
            l.lock();
            ensureFileLock();
            return new LockContext(this, l);
        }
        catch ( IOException e ) {
            throw new ConfigRepositoryException("Failed to obtain write lock", e); //$NON-NLS-1$
        }
    }


    /**
     * @return a lock context for reading
     * @throws ConfigRepositoryException
     */
    public LockContext obtainReadLock () throws ConfigRepositoryException {
        try {
            Lock l = this.rwl.readLock();
            if ( log.isDebugEnabled() ) {
                log.debug("Locking for read " + l); //$NON-NLS-1$
            }
            l.lock();
            ensureFileLock();
            return new LockContext(this, l);
        }
        catch ( IOException e ) {
            throw new ConfigRepositoryException("Failed to obtain read lock", e); //$NON-NLS-1$
        }
    }


    /**
     * @param lockContext
     */
    void release ( LockContext lockContext ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Unlocking " + lockContext.getLock()); //$NON-NLS-1$
        }
        lockContext.getLock().unlock();
    }

}
