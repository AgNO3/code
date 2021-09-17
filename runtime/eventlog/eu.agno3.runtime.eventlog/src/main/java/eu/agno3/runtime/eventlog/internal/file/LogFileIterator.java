/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.05.2015 by mbechler
 */
package eu.agno3.runtime.eventlog.internal.file;


import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.log4j.Logger;

import eu.agno3.runtime.eventlog.EventWithProperties;
import eu.agno3.runtime.eventlog.internal.EmptyEventIterator;


/**
 * @author mbechler
 *
 */
public class LogFileIterator implements Iterator<Iterator<EventWithProperties>> {

    private static final Logger log = Logger.getLogger(LogFileIterator.class);

    private Iterator<File> dirIterator;
    private Iterator<File> cur;
    private String logFileName;

    private boolean ignoreChecksum;


    /**
     * @param logBase
     * @param logFileName
     * @param ignoreChecksum
     */
    public LogFileIterator ( File logBase, String logFileName, boolean ignoreChecksum ) {
        this.ignoreChecksum = ignoreChecksum;
        List<File> logDirs = new ArrayList<>(FileUtils.listFilesAndDirs(logBase, FalseFileFilter.INSTANCE, TrueFileFilter.INSTANCE));
        Collections.sort(logDirs);
        this.dirIterator = logDirs.iterator();
        this.logFileName = logFileName;

        while ( this.dirIterator.hasNext() && ( this.cur == null || !this.cur.hasNext() ) ) {
            this.cur = nextDirectory();
        }
    }


    /**
     * @return
     */
    private Iterator<File> nextDirectory () {
        File next = this.dirIterator.next();
        if ( log.isDebugEnabled() ) {
            log.debug("Reading directory " + next); //$NON-NLS-1$
        }
        return FileUtils.iterateFiles(next, new LogFileFilter(this.logFileName), FalseFileFilter.INSTANCE);
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext () {
        return this.cur != null && this.cur.hasNext();
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.Iterator#next()
     */
    @Override
    public Iterator<EventWithProperties> next () {
        File curFile = this.cur.next();
        if ( !this.cur.hasNext() ) {
            while ( this.dirIterator.hasNext() && !this.cur.hasNext() ) {
                this.cur = nextDirectory();
            }

            if ( !this.cur.hasNext() ) {
                this.cur = null;
            }

        }
        try {
            if ( log.isDebugEnabled() ) {
                log.debug("Reading log file " + curFile); //$NON-NLS-1$
            }
            return FileLogReaderImpl.readFile(curFile, this.ignoreChecksum);
        }
        catch (
            NoSuchAlgorithmException |
            IOException e ) {
            log.error("Failed to read log file " + curFile, e); //$NON-NLS-1$
            return new EmptyEventIterator();
        }
    }
}
