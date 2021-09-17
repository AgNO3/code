/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.06.2015 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


import java.util.UUID;

import org.apache.jackrabbit.webdav.property.ResourceType;
import org.joda.time.DateTime;

import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.NativeEntityKey;
import eu.agno3.runtime.util.uuid.UUIDUtil;


/**
 * @author mbechler
 *
 */
public class OthersRootDAVNode extends AbstractVirtualDAVNode {

    /**
     * 
     */
    public static final String OTHERS_NAME = "others"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String OTHERS_PATH = "/others"; //$NON-NLS-1$

    /**
     * 
     */
    private static final String OTHERS_DISPLAY_NAME = "Files shared by others"; //$NON-NLS-1$

    /**
     * 
     */
    public static final UUID OTHERS_ID = UUID.fromString("86491455-b360-44a3-bb5c-63a215644d4b"); //$NON-NLS-1$


    /**
     * @param rootId
     * @param lastModified
     * @param layout
     * 
     */
    public OthersRootDAVNode ( EntityKey rootId, DateTime lastModified, DAVLayout layout ) {
        super(OTHERS_NAME, rootId, lastModified, layout);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getId()
     */
    @Override
    public EntityKey getId () {
        return new NativeEntityKey(OTHERS_ID);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getAbsolutePath()
     */
    @Override
    public String getAbsolutePath () {
        return OTHERS_PATH;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getInode()
     */
    @Override
    protected byte[] getInode () {
        return UUIDUtil.toBytes(OTHERS_ID);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getDisplayName()
     */
    @Override
    public String getDisplayName () {
        return OTHERS_DISPLAY_NAME;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getPathName()
     */
    @Override
    public String getPathName () {
        return OTHERS_NAME;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getResourceType()
     */
    @Override
    protected ResourceType getResourceType () {
        return new ResourceType(new int[] {
            ResourceType.COLLECTION, Constants.VIRTUAL_RESOURCE_TYPE, Constants.SHARED_ROOT_RESOURCE_TYPE
        });
    }
}
