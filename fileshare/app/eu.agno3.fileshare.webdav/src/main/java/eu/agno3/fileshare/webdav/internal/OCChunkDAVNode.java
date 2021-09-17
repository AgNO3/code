/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.06.2015 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.apache.jackrabbit.webdav.property.DavProperty;
import org.joda.time.DateTime;

import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.runtime.webdav.server.ExistanceDAVNode;


/**
 * @author mbechler
 *
 */
public class OCChunkDAVNode extends AbstractVirtualDAVNode implements ExistanceDAVNode<EntityKey> {

    private VFSEntity parent;
    private String localName;
    private long ts;
    private int chunkIdx;
    private long chunkNum;
    private long chunkSize;
    private Long totalSize;
    private boolean exists;
    private Set<GrantPermission> permissions;
    private boolean shared;
    private UUID grantId;


    /**
     * @param parent
     * @param grantId
     * @param localName
     * @param ts
     * @param chunkIdx
     * @param chunkNum
     * @param totalSize
     * @param chunkSize
     * @param exists
     */
    public OCChunkDAVNode ( VFSEntity parent, UUID grantId, String localName, long ts, int chunkIdx, long chunkNum, long chunkSize, Long totalSize,
            boolean exists ) {
        super(null, parent.getEntityKey(), null, DAVLayout.OWNCLOUD);
        this.parent = parent;
        this.grantId = grantId;
        this.localName = localName;
        this.ts = ts;
        this.chunkIdx = chunkIdx;
        this.chunkNum = chunkNum;
        this.chunkSize = chunkSize;
        this.totalSize = totalSize;
        this.exists = exists;
    }


    /**
     * @return the parent
     */
    public VFSEntity getParent () {
        return this.parent;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getAbsolutePath()
     */
    @Override
    public String getAbsolutePath () {
        return null;
    }


    /**
     * @return the grantId
     */
    public UUID getGrantId () {
        return this.grantId;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.ExistanceDAVNode#exists()
     */
    @Override
    public boolean exists () {
        return this.exists;
    }


    /**
     * @return the localName
     */
    public String getLocalName () {
        return this.localName;
    }


    /**
     * @return the parent
     */
    public VFSEntity getTarget () {
        return this.parent;
    }


    /**
     * @return the ts
     */
    public long getTs () {
        return this.ts;
    }


    /**
     * @return the chunkIdx
     */
    public int getChunkIdx () {
        return this.chunkIdx;
    }


    /**
     * @return the chunkNum
     */
    public long getChunkNum () {
        return this.chunkNum;
    }


    /**
     * @return the chunkSize
     */
    public long getChunkSize () {
        return this.chunkSize;
    }


    /**
     * @return the totalSize
     */
    public Long getTotalSize () {
        return this.totalSize;
    }


    /**
     * @return permissions
     */
    public Set<GrantPermission> getPermissions () {
        return this.permissions;
    }


    /**
     * @param permissions
     *            the permissions to set
     */
    public void setPermissions ( Set<GrantPermission> permissions ) {
        this.permissions = permissions;
    }


    /**
     * @return shared
     */
    public boolean isShared () {
        return this.shared;
    }


    /**
     * @param shared
     *            the shared to set
     */
    public void setShared ( boolean shared ) {
        this.shared = shared;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getId()
     */
    @Override
    public EntityKey getId () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getInode()
     */
    @Override
    protected byte[] getInode () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#isCollection()
     */
    @Override
    public boolean isCollection () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getCreationTime()
     */
    @Override
    public DateTime getCreationTime () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getModificationTime()
     */
    @Override
    public DateTime getModificationTime () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getDisplayName()
     */
    @Override
    public String getDisplayName () {
        return getPathName();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getPathName()
     */
    @Override
    public String getPathName () {
        return String.format("%s-chunked-%d-%d-%d", this.localName, this.ts, this.chunkNum, this.chunkIdx); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getContentType()
     */
    @Override
    public String getContentType () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getContentLength()
     */
    @Override
    public Long getContentLength () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getETag()
     */
    @Override
    public String getETag () {
        return null;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getExtraProperties(java.util.Locale)
     */
    @Override
    public Collection<DavProperty<?>> getExtraProperties ( Locale l ) {
        return Collections.EMPTY_LIST;
    }

}
