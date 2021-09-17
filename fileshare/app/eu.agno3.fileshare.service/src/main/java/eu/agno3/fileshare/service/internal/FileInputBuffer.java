/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.06.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashSet;
import java.util.Set;

import eu.agno3.fileshare.service.InputBuffer;


/**
 * @author mbechler
 *
 */
public class FileInputBuffer implements InputBuffer {

    private Path tempFile;
    private Path revertPath;
    private long fileSize;


    /**
     * @param tempFile
     * @param fileSize
     */
    public FileInputBuffer ( Path tempFile, long fileSize ) {
        if ( tempFile == null ) {
            throw new NullPointerException("tempFile is NULL"); //$NON-NLS-1$
        }
        this.tempFile = tempFile;
        this.revertPath = tempFile;
        this.fileSize = fileSize;
    }


    /**
     * @return the tempFile
     */
    public Path getPath () {
        return this.revertPath;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.InputBuffer#getSize()
     */
    @Override
    public long getSize () {
        return this.fileSize;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws FileNotFoundException
     *
     * @see eu.agno3.fileshare.service.InputBuffer#getStream()
     */
    @Override
    public SeekableByteChannel getStream () throws IOException {
        if ( this.tempFile == null || !Files.exists(this.tempFile) ) {
            throw new IOException("Use after closed"); //$NON-NLS-1$
        }

        return FileChannel.open(this.tempFile, StandardOpenOption.READ);
    }


    /**
     * Move file to target
     * 
     * @param target
     * @param create
     * @param storageGroup
     * @throws IOException
     */
    public void move ( Path target, boolean create, GroupPrincipal storageGroup ) throws IOException {
        if ( this.tempFile == null ) {
            throw new IOException("Use after closed"); //$NON-NLS-1$
        }
        Set<CopyOption> opts = new HashSet<>();

        opts.add(StandardCopyOption.ATOMIC_MOVE);
        if ( !create ) {
            opts.add(StandardCopyOption.REPLACE_EXISTING);
        }

        if ( storageGroup != null ) {
            PosixFileAttributeView attrs = Files.getFileAttributeView(this.tempFile, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
            attrs.setGroup(storageGroup);
            attrs.setPermissions(PosixFilePermissions.fromString("rw-rw----")); //$NON-NLS-1$
        }

        Files.move(this.tempFile, target, opts.toArray(new CopyOption[] {}));
        this.tempFile = null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.InputBuffer#close()
     */
    @Override
    public void close () throws IOException {
        this.tempFile = null;
    }

}
