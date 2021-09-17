/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.04.2016 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.property.ResourceType;

import eu.agno3.fileshare.model.query.ChunkInfo;
import eu.agno3.fileshare.model.query.ChunkedUploadInfo;


/**
 * @author mbechler
 *
 */
public class TransferDAVNode extends AbstractVirtualDAVNode {

    /**
     * 
     */
    public static final String TRANSFERS_PATH = "/.transfers"; //$NON-NLS-1$

    private String hint;
    private long chunkSize;
    private long completeSize;
    private String localName;
    private String contentType;
    private List<ChunkInfo> missingChunks;


    /**
     * @param ci
     * @param layout
     */
    public TransferDAVNode ( ChunkedUploadInfo ci, DAVLayout layout ) {
        super(".transfers", null, null, layout); //$NON-NLS-1$
        this.hint = ci.getReference();
        this.chunkSize = ci.getChunkSize();
        this.completeSize = ci.getTotalSize();
        this.localName = ci.getLocalName();
        this.contentType = ci.getContentType();
        this.missingChunks = ci.getMissingChunks();
    }


    /**
     * @return the hint
     */
    public String getHint () {
        return this.hint;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getContentLength()
     */
    @Override
    public Long getContentLength () {
        return this.completeSize;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getContentType()
     */
    @Override
    public String getContentType () {
        return this.contentType;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getDisplayName()
     */
    @Override
    public String getDisplayName () {
        return this.localName;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#isCollection()
     */
    @Override
    public boolean isCollection () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#isOverrideResourceType()
     */
    @Override
    public boolean isOverrideResourceType () {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getExtraProperties(java.util.Locale)
     */
    @Override
    public Collection<DavProperty<?>> getExtraProperties ( Locale l ) {
        Collection<DavProperty<?>> props = super.getExtraProperties(l);
        props.add(new ResourceType(new int[] {
            Constants.TRANSFER_RESOURCE_TYPE, Constants.VIRTUAL_RESOURCE_TYPE
        }));
        props.add(new DefaultDavProperty<>(Constants.CHUNK_SIZE, this.chunkSize));
        props.add(new MissingChunks(this.missingChunks));
        return props;
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
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getAbsolutePath()
     */
    @Override
    public String getAbsolutePath () {
        return TRANSFERS_PATH + "/" + this.hint; //$NON-NLS-1$
    }

}
