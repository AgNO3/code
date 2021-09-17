/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.01.2015 by mbechler
 */
package eu.agno3.fileshare.model;


import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OptimisticLock;
import org.joda.time.DateTime;


/**
 * @author mbechler
 *
 */
@PersistenceUnit ( unitName = "fileshare" )
@Entity
@Inheritance ( strategy = InheritanceType.JOINED )
@Table ( name = "content_file" )
public class FileEntity extends ContentEntity implements VFSFileEntity {

    /**
     * 
     */
    private static final long serialVersionUID = -8944957022915683935L;

    private long fileSize;
    private String contentType;
    private String contentEncoding;
    private boolean canReplace;
    private DateTime contentLastModified;


    /**
     * 
     */
    public FileEntity () {}


    /**
     * 
     * @param e
     * @param refs
     */
    public FileEntity ( FileEntity e, boolean refs ) {
        super(e, refs);
        this.fileSize = e.fileSize;
        this.contentType = e.contentType;
        this.contentEncoding = e.contentEncoding;
        this.canReplace = e.canReplace();
    }


    /**
     * @return whether the file can be replaced
     */
    @Override
    @Transient
    public boolean canReplace () {

        if ( this.getParent() != null ) {
            return this.getParent().getAllowFileOverwrite();
        }
        return this.canReplace;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.ContentEntity#cloneShallow(boolean)
     */
    @Override
    public ContentEntity cloneShallow ( boolean refs ) {
        return new FileEntity(this, refs);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.ContentEntity#getEntityType()
     */
    @Override
    @Transient
    public EntityType getEntityType () {
        return EntityType.FILE;
    }


    /**
     * @return the fileSize
     */
    @Override
    public long getFileSize () {
        return this.fileSize;
    }


    /**
     * @param fileSize
     *            the fileSize to set
     */
    @Override
    public void setFileSize ( long fileSize ) {
        this.fileSize = fileSize;
    }


    /**
     * @return the contentType
     */
    @Override
    @NotNull
    public String getContentType () {
        return this.contentType;
    }


    /**
     * @param contentType
     *            the contentType to set
     */
    @Override
    public void setContentType ( String contentType ) {
        this.contentType = contentType;
    }


    /**
     * @return the contentEncoding
     */
    @Override
    public String getContentEncoding () {
        return this.contentEncoding;
    }


    /**
     * @param contentEncoding
     *            the contentEncoding to set
     */
    @Override
    public void setContentEncoding ( String contentEncoding ) {
        this.contentEncoding = contentEncoding;
    }


    /**
     * 
     * @return content last modification
     */
    @Override
    @OptimisticLock ( excluded = true )
    public DateTime getContentLastModified () {
        if ( this.contentLastModified == null ) {
            return this.getLastModified();
        }
        return this.contentLastModified;
    }


    /**
     * 
     * @param lastModified
     */
    @Override
    public void setContentLastModified ( DateTime lastModified ) {
        this.contentLastModified = lastModified;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#isStaticSharable()
     */
    @Override
    @Transient
    public boolean isStaticSharable () {
        return true;
    }

}
