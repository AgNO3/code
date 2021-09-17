/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.06.2015 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


import java.util.Collection;
import java.util.Locale;
import java.util.UUID;

import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.ResourceType;

import eu.agno3.fileshare.model.GroupInfo;
import eu.agno3.fileshare.model.NativeEntityKey;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.VFSContainerEntity;


/**
 * @author mbechler
 *
 */
public class GroupRootDAVNode extends EntityDAVNode {

    private UUID groupId;


    /**
     * @param entity
     * @param layout
     */
    public GroupRootDAVNode ( VFSContainerEntity entity, DAVLayout layout ) {
        super(entity, new NativeEntityKey(GroupsRootDAVNode.GROUP_ID), layout); // $NON-NLS-1$
        this.groupId = entity.getOwner().getId();
    }


    /**
     * 
     * @param groupId
     * @param layout
     */
    public GroupRootDAVNode ( UUID groupId, DAVLayout layout ) {
        super(null, new NativeEntityKey(groupId), layout);
        this.groupId = groupId;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.EntityDAVNode#isOverrideResourceType()
     */
    @Override
    public boolean isOverrideResourceType () {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.EntityDAVNode#getExtraProperties(java.util.Locale)
     */
    @Override
    public Collection<DavProperty<?>> getExtraProperties ( Locale l ) {
        Collection<DavProperty<?>> res = super.getExtraProperties(l);
        res.add(new ResourceType(new int[] {
            ResourceType.COLLECTION, Constants.GROUP_ROOT_RESOURCE_TYPE, Constants.NOT_SHAREABLE_RESOURCE_TYPE
        }));
        return res;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.EntityDAVNode#isCollection()
     */
    @Override
    public boolean isCollection () {
        return true;
    }


    /**
     * @return the groupId
     */
    public UUID getGroupId () {
        return this.groupId;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.EntityDAVNode#getDisplayName()
     */
    @Override
    public String getDisplayName () {
        return getGroupName(this.getEntity());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.EntityDAVNode#getEntity()
     */
    @Override
    public VFSContainerEntity getEntity () {
        return (VFSContainerEntity) super.getEntity();
    }


    /**
     * @param e
     * @return the group name
     */
    public static String getGroupName ( VFSContainerEntity e ) {
        Subject owner = e.getOwner();
        if ( owner instanceof GroupInfo ) {
            GroupInfo gi = (GroupInfo) owner;
            if ( gi.getRealm() != null ) {
                return gi.getName() + "@" + gi.getRealm(); //$NON-NLS-1$
            }
            return gi.getName();
        }
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.EntityDAVNode#getPathName()
     */
    @Override
    public String getPathName () {
        return getDisplayName();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.EntityDAVNode#hashCode()
     */
    @Override
    public int hashCode () {
        return this.groupId.hashCode();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.EntityDAVNode#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {
        if ( obj instanceof GroupRootDAVNode ) {
            return ( (GroupRootDAVNode) obj ).getGroupId().equals(this.groupId);
        }
        return super.equals(obj);
    }
}
