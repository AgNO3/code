/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 6, 2016 by mbechler
 */
package eu.agno3.fileshare.service.archive.internal;


import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.service.ArchiveType;
import eu.agno3.fileshare.vfs.VFSContentHandle;
import eu.agno3.fileshare.vfs.VFSContext;


/**
 * @author mbechler
 *
 */
public class TarArchiveContext implements ArchiveContext {

    private final TarArchiveOutputStream archiveOut;
    private final CompressorOutputStream compressOut;
    private final int bufferSize;
    private ArchiveType type;


    /**
     * @param out
     * @param bufferSize
     * @param type
     * @throws IOException
     * 
     */
    public TarArchiveContext ( OutputStream out, int bufferSize, ArchiveType type ) throws IOException {
        this.bufferSize = bufferSize;
        this.type = type;
        if ( type != ArchiveType.TAR ) {
            try {
                String ctype;
                switch ( type ) {
                case TBZ2:
                    ctype = CompressorStreamFactory.BZIP2;
                    break;
                case TGZ:
                    ctype = CompressorStreamFactory.GZIP;
                    break;
                default:
                    throw new IOException("Unknown compressor type " + type); //$NON-NLS-1$
                }
                this.compressOut = ( new CompressorStreamFactory() ).createCompressorOutputStream(ctype, out);
            }
            catch ( CompressorException e ) {
                throw new IOException("Failed to create compressor", e); //$NON-NLS-1$
            }
        }
        else {
            this.compressOut = null;
        }
        TarArchiveOutputStream archive = new TarArchiveOutputStream(this.compressOut != null ? this.compressOut : out);
        archive.setAddPaxHeadersForNonAsciiNames(true);
        archive.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
        archive.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        this.archiveOut = archive;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.archive.internal.ArchiveContext#getArchiveType()
     */
    @Override
    public ArchiveType getArchiveType () {
        return this.type;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.archive.internal.ArchiveContext#close()
     */
    @Override
    public void close () throws IOException {
        if ( this.archiveOut != null ) {
            this.archiveOut.close();
        }

        if ( this.compressOut != null ) {
            this.compressOut.close();
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.archive.internal.ArchiveContext#addEntry(java.lang.String,
     *      eu.agno3.fileshare.vfs.VFSContext, eu.agno3.fileshare.model.VFSFileEntity, boolean)
     */
    @Override
    public void addEntry ( String path, VFSContext v, VFSFileEntity e, boolean compress ) throws IOException, FileshareException {
        TarArchiveEntry te = new TarArchiveEntry(path);
        te.setModTime(e.getLastModified().getMillis());
        te.setSize(e.getFileSize());

        try ( VFSContentHandle ch = v.getContents(e, null) ) {
            this.archiveOut.putArchiveEntry(te);
            ch.transferTo(this.archiveOut, new byte[this.bufferSize]);
            this.archiveOut.closeArchiveEntry();
        }
    }

}
