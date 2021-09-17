/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.12.2014 by mbechler
 */
package eu.agno3.orchestrator.system.file.hashtracking.internal;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import eu.agno3.orchestrator.system.base.SystemService;
import eu.agno3.orchestrator.system.base.SystemServiceType;
import eu.agno3.orchestrator.system.file.hashtracking.FileHashTracker;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    FileHashTracker.class, SystemService.class
}, configurationPid = FileHashTrackerImpl.PID, configurationPolicy = ConfigurationPolicy.REQUIRE )
@SystemServiceType ( FileHashTracker.class )
public class FileHashTrackerImpl implements FileHashTracker {

    private static final Logger log = Logger.getLogger(FileHashTrackerImpl.class);

    /**
     * 
     */
    public static final String PID = "config.hashes"; //$NON-NLS-1$
    private static final String DEFAULT_BASE_PATH = "/var/lib/orchagent/hashes/"; //$NON-NLS-1$

    private static final String UTF8 = "UTF-8"; //$NON-NLS-1$

    private static final Set<String> IGNORE_FILES = new HashSet<>(Arrays.asList(".gitignore" //$NON-NLS-1$
    ));

    private Path baseHashPath;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) throws IOException {

        String baseSpec = (String) ctx.getProperties().get("base"); //$NON-NLS-1$

        if ( StringUtils.isBlank(baseSpec) ) {
            baseSpec = DEFAULT_BASE_PATH;
        }

        this.baseHashPath = Paths.get(baseSpec.trim());

        if ( !Files.exists(this.baseHashPath) ) {
            Files.createDirectories(this.baseHashPath, PosixFilePermissions.asFileAttribute(FileSecurityUtils.getOwnerOnlyDirPermissions()));
        }

        if ( !Files.isDirectory(this.baseHashPath) || !Files.isWritable(this.baseHashPath) ) {
            throw new IOException("Cannot access hash directory"); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     *
     * @see eu.agno3.orchestrator.system.file.hashtracking.FileHashTracker#updateHash(java.nio.file.Path, byte[])
     */
    @Override
    public void updateHash ( Path p, byte[] hash ) throws IOException {
        if ( hash == null ) {
            Files.deleteIfExists(this.makeHashPath(p));
            return;
        }
        int written = 0;
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Storing hash value %s for path %s", printableHash(hash), p)); //$NON-NLS-1$
        }
        try ( FileChannel fc = FileChannel.open(
            this.makeHashPath(p),
            EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE),
            PosixFilePermissions.asFileAttribute(FileSecurityUtils.getOwnerOnlyFilePermissions()));
              FileLock lock = fc.lock() ) {

            ByteBuffer buf = ByteBuffer.wrap(hash);

            while ( written < hash.length ) {
                written += fc.write(buf);
            }
        }
    }


    @Override
    public Map<Path, byte[]> listHashes () throws IOException {
        Map<Path, byte[]> hashes = new HashMap<>();
        Files.list(this.baseHashPath).forEach( ( f ) -> {
            String fname = f.getFileName().toString();
            if ( IGNORE_FILES.contains(fname) ) {
                return;
            }
            try {
                Path path = keyToPath(fname);
                byte[] hashVal = Files.readAllBytes(f);
                hashes.put(path, hashVal);
            }
            catch ( Exception e ) {
                log.warn("Failed to get hash value for " + fname, e); //$NON-NLS-1$
            }
        });
        return hashes;
    }


    /**
     * 
     * @param p
     * @throws IOException
     */
    @Override
    public void removeHash ( Path p ) throws IOException {
        Files.deleteIfExists(this.makeHashPath(p));
    }


    /**
     * @param p
     * @return
     * @throws IOException
     */
    private static String pathToKey ( Path p ) throws IOException {
        String stringPath = p.toString();

        if ( stringPath.indexOf('_') >= 0 || stringPath.indexOf('%') >= 0 ) {
            stringPath = URLEncoder.encode(stringPath, UTF8);
            stringPath = StringUtils.replace(
                stringPath,
                "_", //$NON-NLS-1$
                "%5F"); //$NON-NLS-1$
        }
        return StringUtils.replaceChars(stringPath, '/', '_');
    }


    /**
     * @param fname
     * @return
     * @throws UnsupportedEncodingException
     */
    private static Path keyToPath ( String fname ) throws UnsupportedEncodingException {
        String string = StringUtils.replaceChars(fname, '_', '/');
        if ( string.indexOf('%') >= 0 ) {
            string = URLDecoder.decode(string, UTF8);
        }
        return Paths.get(string);
    }


    /**
     * @param p
     * @return
     * @throws IOException
     */
    private Path makeHashPath ( Path p ) throws IOException {
        return this.baseHashPath.resolve(pathToKey(p));
    }


    /**
     * {@inheritDoc}
     * 
     *
     * @see eu.agno3.orchestrator.system.file.hashtracking.FileHashTracker#checkHash(java.nio.file.Path, byte[])
     */
    @Override
    public boolean checkHash ( Path p, byte[] hash ) throws IOException {
        Path hashPath = this.makeHashPath(p);
        if ( !Files.exists(hashPath) ) {
            return true;
        }
        byte[] savedHash = Files.readAllBytes(hashPath);

        if ( log.isDebugEnabled() ) {
            log.debug(String.format(
                "Checking hash value %s against stored %s for path %s", //$NON-NLS-1$
                printableHash(hash),
                printableHash(savedHash),
                p));
        }

        return Arrays.equals(savedHash, hash);
    }


    /**
     * @param hash
     * @return
     */
    private static String printableHash ( byte[] hash ) {
        return Hex.encodeHexString(hash);
    }

}
