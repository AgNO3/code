/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.11.2014 by mbechler
 */
package eu.agno3.orchestrator.system.file.util;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
public final class FileHashUtil {

    private static final Logger log = Logger.getLogger(FileHashUtil.class);
    private static final int BUFSIZE = 4096;


    /**
     * 
     */
    private FileHashUtil () {}


    /**
     * 
     * @param p
     * @return the SHA-256 of the given file's contents
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public static byte[] sha256 ( Path p ) throws NoSuchAlgorithmException, IOException {
        return digest("SHA-256", p); //$NON-NLS-1$
    }


    /**
     * 
     * @param p
     * @return the SHA-512 of the given file's contents
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public static byte[] sha512 ( Path p ) throws NoSuchAlgorithmException, IOException {
        return digest("SHA-512", p); //$NON-NLS-1$
    }


    /**
     * @param digestAlg
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    protected static byte[] digest ( String digestAlg, Path p ) throws NoSuchAlgorithmException, IOException {
        MessageDigest dgst = MessageDigest.getInstance(digestAlg);
        ByteBuffer buf = ByteBuffer.allocate(BUFSIZE);
        if ( log.isDebugEnabled() ) {
            log.debug("Hashing " + p); //$NON-NLS-1$
        }
        try ( SeekableByteChannel is = Files.newByteChannel(p, StandardOpenOption.READ) ) {
            int read = 0;
            while ( ( read = is.read(buf) ) >= 0 ) {
                if ( log.isTraceEnabled() ) {
                    log.trace("Read " + read); //$NON-NLS-1$
                }
                buf.flip();
                dgst.update(buf);
                buf.clear();
            }
        }
        log.trace("DONE"); //$NON-NLS-1$
        return dgst.digest();
    }
}
