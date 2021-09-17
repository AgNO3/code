/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.10.2015 by mbechler
 */
package eu.agno3.fileshare.model;


import java.io.IOException;

import org.joda.time.DateTime;


/**
 * @author mbechler
 *
 */
public class VFSFileEntityImpl extends VFSEntityImpl implements VFSFileEntity {

    /**
     * 
     */
    private static final long serialVersionUID = 6702365238461975354L;
    private long size;
    private String contentType = "application/octet-stream"; //$NON-NLS-1$
    private String contentEncoding;


    /**
     * @param relativePath
     * @param group
     * @param readOnly
     * @throws IOException
     */
    public VFSFileEntityImpl ( String relativePath, VirtualGroup group, boolean readOnly ) throws IOException {
        super(relativePath, group, readOnly);
    }


    /**
     * @param e
     * 
     */
    public VFSFileEntityImpl ( VFSFileEntityImpl e ) {
        super(e);
        this.size = e.getFileSize();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#getEntityType()
     */
    @Override
    public EntityType getEntityType () {
        return EntityType.FILE;
    }


    /**
     * {@inheritDoc}
     * 
     *
     * @see eu.agno3.fileshare.model.VFSEntity#cloneShallow(boolean)
     */
    @Override
    public VFSFileEntity cloneShallow ( boolean b ) {
        // probably no need for clone
        return this;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#cloneShallow()
     */
    @Override
    public VFSFileEntity cloneShallow () {
        return this.cloneShallow(true);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSFileEntity#getFileSize()
     */
    @Override
    public long getFileSize () {
        return this.size;

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSFileEntity#setFileSize(long)
     */
    @Override
    public void setFileSize ( long fileSize ) {
        this.size = fileSize;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSFileEntity#getContentType()
     */
    @Override
    public String getContentType () {
        return this.contentType;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSFileEntity#setContentType(java.lang.String)
     */
    @Override
    public void setContentType ( String string ) {
        this.contentType = string;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSFileEntity#getContentEncoding()
     */
    @Override
    public String getContentEncoding () {
        return this.contentEncoding;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSFileEntity#setContentEncoding(java.lang.String)
     */
    @Override
    public void setContentEncoding ( String string ) {
        this.contentEncoding = string;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSFileEntity#canReplace()
     */
    @Override
    public boolean canReplace () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSFileEntity#getContentLastModified()
     */
    @Override
    public DateTime getContentLastModified () {
        return getLastModified();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSFileEntity#setContentLastModified(org.joda.time.DateTime)
     */
    @Override
    public void setContentLastModified ( DateTime lastMod ) {
        // ignore
    }

}
