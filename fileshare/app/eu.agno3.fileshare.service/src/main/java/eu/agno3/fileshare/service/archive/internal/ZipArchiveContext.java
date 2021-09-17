/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 6, 2016 by mbechler
 */
package eu.agno3.fileshare.service.archive.internal;


import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.service.ArchiveType;
import eu.agno3.fileshare.vfs.VFSContentHandle;
import eu.agno3.fileshare.vfs.VFSContext;


/**
 * @author mbechler
 *
 */
public class ZipArchiveContext implements ArchiveContext {

    private final ZipOutputStream outputStream;
    private final int bufferSize;
    private final ArchiveType type;


    /**
     * @param bs
     * @param bos
     * @param t
     * 
     */
    public ZipArchiveContext ( BufferedOutputStream bos, int bs, ArchiveType t ) {
        this.type = t;
        this.bufferSize = bs;
        this.outputStream = new ZipOutputStream(bos);
        if ( t == ArchiveType.ZIPU ) {
            this.outputStream.setLevel(0);
        }
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
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close () throws IOException {
        this.outputStream.close();
    }


    @Override
    public void addEntry ( String path, VFSContext v, VFSFileEntity e, boolean compress ) throws IOException, FileshareException {
        if ( !compress ) {
            this.outputStream.setLevel(0);
        }
        try {
            ZipEntry ze = new ZipEntry(path);
            ze.setTime(e.getLastModified().getMillis());
            try ( VFSContentHandle ch = v.getContents(e, null) ) {
                this.outputStream.putNextEntry(ze);
                ch.transferTo(this.outputStream, new byte[this.bufferSize]);
                this.outputStream.closeEntry();
            }
        }
        finally {
            if ( this.type != ArchiveType.ZIPU ) {
                this.outputStream.setLevel(-1);
            }
        }
    }

}
