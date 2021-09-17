/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.05.2015 by mbechler
 */
package eu.agno3.runtime.eventlog.internal.file;


import java.io.File;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParser;

import eu.agno3.runtime.eventlog.EventIterator;
import eu.agno3.runtime.eventlog.internal.EventMarshaller;


/**
 * @author mbechler
 *
 */
public class FileLogReaderImpl {

    private static final Logger log = Logger.getLogger(FileLogReaderImpl.class);

    private File logBase;
    private String logFileName;
    private boolean ignoreChecksum;


    /**
     * @param logBase
     * @param logFileName
     * @param ignoreChecksum
     */
    public FileLogReaderImpl ( File logBase, String logFileName, boolean ignoreChecksum ) {
        this.logBase = logBase;
        this.logFileName = logFileName;
        this.ignoreChecksum = ignoreChecksum;
    }


    /**
     * @return an iterator of event objects in all
     */
    public EventIterator readAll () {
        return new RecursiveEventIterator(new LogFileIterator(this.logBase, this.logFileName, this.ignoreChecksum));
    }


    /**
     * @param f
     * @param ignoreChecksumFailure
     * @return an iterator of event objects in the given file
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    @SuppressWarnings ( "resource" )
    public static EventIterator readFile ( File f, boolean ignoreChecksumFailure ) throws IOException, NoSuchAlgorithmException {

        File hashFile = new File(f.getParentFile(), f.getName().replace(".log", //$NON-NLS-1$
            ".sha256")); //$NON-NLS-1$

        MessageDigest digest = MessageDigest.getInstance("SHA-256"); //$NON-NLS-1$

        try ( FileChannel channel = FileChannel.open(f.toPath(), StandardOpenOption.READ) ) {
            if ( !FileLoggerBackendImpl.verifyChecksum(digest, hashFile.toPath(), channel) && !ignoreChecksumFailure ) {
                throw new IOException("Failed to verify log file checksum " + f); //$NON-NLS-1$
            }
        }

        log.debug("Opening log file " + f); //$NON-NLS-1$
        FileChannel channel = FileChannel.open(f.toPath(), StandardOpenOption.READ);
        JsonParser parser = EventMarshaller.getJsonFactory().createParser(Channels.newInputStream(channel));
        return new EventIteratorImpl(channel, parser);
    }
}
