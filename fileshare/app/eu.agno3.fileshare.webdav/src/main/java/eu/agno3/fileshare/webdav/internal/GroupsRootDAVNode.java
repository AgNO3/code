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
public class GroupsRootDAVNode extends AbstractVirtualDAVNode {

    /**
     * 
     */
    public static final String GROUPS_NAME = "groups"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String GROUPS_PATH = "/groups"; //$NON-NLS-1$

    /**
     * 
     */
    private static final String GROUPS_DISPLAY_NAME = "My groups"; //$NON-NLS-1$

    /**
     * 
     */
    public static final UUID GROUP_ID = UUID.fromString("3a4816c0-d114-43f6-9004-e6264560e788"); //$NON-NLS-1$


    /**
     * @param parentKey
     * @param lastModified
     * @param layout
     * 
     */
    public GroupsRootDAVNode ( EntityKey parentKey, DateTime lastModified, DAVLayout layout ) {
        super(GROUPS_NAME, parentKey, lastModified, layout);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getAbsolutePath()
     */
    @Override
    public String getAbsolutePath () {
        return GROUPS_PATH;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getId()
     */
    @Override
    public EntityKey getId () {
        return new NativeEntityKey(GROUP_ID);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getInode()
     */
    @Override
    protected byte[] getInode () {
        return UUIDUtil.toBytes(GROUP_ID);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getDisplayName()
     */
    @Override
    public String getDisplayName () {
        return GROUPS_DISPLAY_NAME;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getPathName()
     */
    @Override
    public String getPathName () {
        return GROUPS_NAME;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getResourceType()
     */
    @Override
    protected ResourceType getResourceType () {
        return new ResourceType(new int[] {
            ResourceType.COLLECTION, Constants.VIRTUAL_RESOURCE_TYPE, Constants.GROUPS_ROOT_RESOURCE_TYPE
        });
    }
}
