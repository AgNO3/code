/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.10.2015 by mbechler
 */
package eu.agno3.fileshare.model;


import java.util.Collections;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;


/**
 * @author mbechler
 *
 */
@PersistenceUnit ( unitName = "fileshare" )
@Entity
@Inheritance ( strategy = InheritanceType.JOINED )
@Table ( name = "content_mapped_container", uniqueConstraints = @UniqueConstraint ( columnNames = {
    "VFS_ID", "inode"
} ) )
public class MappedContainerEntity extends ContainerEntity implements MappedVFSEntity {

    /**
     * 
     */
    private static final long serialVersionUID = 4371666334790888347L;

    private VirtualGroup vfs;
    private VFSContainerEntity delegate;

    private EntityReferenceStorage relativePathStorage;

    private byte[] inode;


    /**
     * 
     */
    public MappedContainerEntity () {
        super();
        super.setChildrenSize(0L);
    }


    /**
     * @param e
     * @param refs
     */
    public MappedContainerEntity ( MappedContainerEntity e, boolean refs ) {
        super(e, refs);
        this.delegate = e.getDelegate();

        this.inode = e.getInode();
        this.relativePathStorage = new EntityReferenceStorage(this.relativePathStorage != null ? this.relativePathStorage.getRelativePath() : null);

        if ( e.vfs != null ) {
            this.vfs = e.vfs.cloneShallow(false);
        }

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.ContainerEntity#cloneShallow()
     */
    @Override
    public MappedContainerEntity cloneShallow () {
        return this.cloneShallow(true);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.ContainerEntity#cloneShallow(boolean)
     */
    @Override
    public MappedContainerEntity cloneShallow ( boolean refs ) {
        return new MappedContainerEntity(this, refs);
    }


    @Override
    @OneToOne ( fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = false )
    public EntityReferenceStorage getReferenceStorage () {
        return this.relativePathStorage;
    }


    /**
     * @param relativePathStorage
     *            the relativePathStorage to set
     */
    @Override
    public void setReferenceStorage ( EntityReferenceStorage relativePathStorage ) {
        this.relativePathStorage = relativePathStorage;
    }


    /**
     * @return the delegate
     */
    @Override
    @Transient
    public VFSContainerEntity getDelegate () {
        return this.delegate;
    }


    /**
     * 
     * @return the file inode
     */
    @Override
    @Column ( length = 16 )
    @Type ( type = "org.hibernate.type.BinaryType" )
    public byte[] getInode () {
        return this.inode;
    }


    /**
     * 
     * @param inode
     */
    @Override
    public void setInode ( byte[] inode ) {
        this.inode = inode;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.MappedVFSEntity#getVfs()
     */
    @Override
    @JoinColumn ( name = "VFS_ID" )
    @ManyToOne ( optional = false )
    public VirtualGroup getVfs () {
        return this.vfs;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.MappedVFSEntity#setVfs(eu.agno3.fileshare.model.VirtualGroup)
     */
    @Override
    public void setVfs ( VirtualGroup vfs ) {
        this.vfs = vfs;
    }


    /**
     * @param delegate
     *            the delegate to set
     */
    @Override
    public void setDelegate ( VFSEntity delegate ) {
        if ( ! ( delegate instanceof VFSContainerEntity ) ) {
            throw new UnsupportedOperationException();
        }
        this.delegate = (VFSContainerEntity) delegate;
    }


    @Override
    @Transient
    public boolean getAllowFileOverwrite () {
        return this.delegate != null ? this.delegate.getAllowFileOverwrite() : false;
    }


    @Override
    @Transient
    public EntityKey getEntityKey () {
        return this.delegate != null ? this.delegate.getEntityKey() : null;
    }


    @Override
    @Transient
    public String getLocalName () {
        return this.delegate != null ? this.delegate.getLocalName() : null;
    }


    @Override
    @Transient
    public boolean getSendNotifications () {
        return this.delegate != null ? this.delegate.getSendNotifications() : false;
    }


    @Override
    @Transient
    public User getCreator () {
        return this.delegate != null ? this.delegate.getCreator() : null;
    }


    @Override
    @Transient
    public Grant getCreatorGrant () {
        return this.delegate != null ? this.delegate.getCreatorGrant() : null;
    }


    @Override
    @Transient
    public Subject getOwner () {
        return this.vfs;
    }


    @Override
    @Transient
    public DateTime getLastModified () {
        return this.delegate != null ? this.delegate.getLastModified() : null;
    }


    @Override
    @Transient
    public DateTime getCreated () {
        return this.delegate != null ? this.delegate.getCreated() : null;
    }


    @Override
    @Transient
    public Long getChildrenSize () {
        Long childSize = this.delegate != null ? this.delegate.getChildrenSize() : null;
        if ( childSize != null ) {
            return childSize;
        }
        return (long) 0;
    }


    @Override
    @Transient
    public SecurityLabel getSecurityLabel () {
        return this.delegate != null ? this.delegate.getSecurityLabel() : null;
    }


    @Override
    @Transient
    public boolean isEmpty () {
        return this.delegate != null ? this.delegate.isEmpty() : true;
    }


    @Override
    @Transient
    public DateTime getExpires () {
        return this.delegate != null ? this.delegate.getExpires() : null;
    }


    @Override
    @Transient
    public User getLastModifier () {
        return this.delegate != null ? this.delegate.getLastModifier() : null;
    }


    @Override
    @Transient
    public Grant getLastModifiedGrant () {
        return this.delegate != null ? this.delegate.getLastModifiedGrant() : null;
    }


    @Override
    @Transient
    public boolean hasParent () {
        return this.delegate != null && this.delegate.hasParent();
    }


    @Override
    @Transient
    public boolean hasGrants () {
        return this.delegate != null && this.delegate.hasGrants();
    }


    @Override
    @Transient
    public boolean hasLocalValidGrants () {
        return this.delegate != null && this.delegate.hasLocalValidGrants();
    }


    @Override
    @Transient
    public boolean isStaticReadOnly () {
        return this.delegate == null || this.delegate.isStaticReadOnly();
    }


    @Override
    @Transient
    public Set<Grant> getLocalValidGrants () {
        return this.delegate != null ? this.delegate.getLocalValidGrants() : Collections.EMPTY_SET;
    }


    @Override
    @Transient
    public Set<Grant> getOriginalLocalValidGrants () {
        return super.getLocalValidGrants();
    }
}
