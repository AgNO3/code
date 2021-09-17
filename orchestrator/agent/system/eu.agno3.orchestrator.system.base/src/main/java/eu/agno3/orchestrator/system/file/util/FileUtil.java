/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.file.util;


import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;


/**
 * @author mbechler
 * 
 */
public final class FileUtil {

    /**
     * 
     */
    private FileUtil () {}


    /**
     * 
     * @param from
     * @param to
     * @param replace
     * @throws IOException
     */
    public static void safeMove ( Path from, Path to, boolean replace ) throws IOException {
        // ensure the file was created in a safe location,
        // the file itself may be group/other writable by now
        FileSecurityUtils.assertSecureLocation(from.getParent());

        Set<CopyOption> options = new HashSet<>();
        options.add(StandardCopyOption.ATOMIC_MOVE);
        options.add(LinkOption.NOFOLLOW_LINKS);

        if ( replace ) {
            options.add(StandardCopyOption.REPLACE_EXISTING);
        }

        Files.move(from, to, options.toArray(new CopyOption[] {}));
    }


    /**
     * Copies a file's contents to another file
     * 
     * @param from
     * @param to
     * @param cb
     * @param bufSize
     * @throws ExecutionException
     */
    public static void copyFileContents ( Path from, Path to, ProgressCallback cb, int bufSize ) throws ExecutionException {
        try ( FileChannel sourceChannel = FileChannel.open(from, StandardOpenOption.READ);
              FileChannel targetChannel = FileChannel.open(to, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING) ) {
            targetChannel.lock();

            long sourceSize = sourceChannel.size();
            cb.start(sourceSize);
            long sourcePos = 0;

            while ( sourcePos < sourceSize ) {
                sourcePos += sourceChannel.transferTo(sourcePos, bufSize, targetChannel);
                cb.progress(sourcePos);
            }

            targetChannel.force(true);
            cb.finished();
        }
        catch ( IOException e ) {
            throw new ExecutionException("Failed to open source file:", e); //$NON-NLS-1$
        }
    }


    /**
     * @param path
     * @throws IOException
     */
    public static void deleteRecursive ( Path path ) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile ( Path file, BasicFileAttributes attrs ) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }


            @Override
            public FileVisitResult visitFileFailed ( Path file, IOException exc ) throws IOException {
                // try to delete the file anyway, even if its attributes
                // could not be read, since delete-only access is
                // theoretically possible
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }


            @Override
            public FileVisitResult postVisitDirectory ( Path dir, IOException exc ) throws IOException {

                if ( exc != null ) {
                    throw exc;
                }

                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

}
