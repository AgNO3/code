/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.06.2015 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


import org.apache.jackrabbit.webdav.property.ResourceType;
import org.joda.time.DateTime;

import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.GroupInfo;
import eu.agno3.fileshare.model.NativeEntityKey;
import eu.agno3.fileshare.model.SubjectInfo;
import eu.agno3.fileshare.model.UserInfo;
import eu.agno3.fileshare.model.query.GroupQueryResult;
import eu.agno3.fileshare.model.query.UserQueryResult;
import eu.agno3.runtime.util.uuid.UUIDUtil;


/**
 * @author mbechler
 *
 */
public class SharingSubjectDAVNode extends AbstractVirtualDAVNode {

    private SubjectInfo subj;


    /**
     * @param subj
     * @param lastModified
     * @param layout
     */
    public SharingSubjectDAVNode ( SubjectInfo subj, DateTime lastModified, DAVLayout layout ) {
        super(null, new NativeEntityKey(OthersRootDAVNode.OTHERS_ID), lastModified, layout); // $NON-NLS-1$
        this.subj = subj;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getId()
     */
    @Override
    public EntityKey getId () {
        return new NativeEntityKey(this.subj.getId());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getInode()
     */
    @Override
    protected byte[] getInode () {
        return UUIDUtil.toBytes(this.subj.getId());
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
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getDisplayName()
     */
    @Override
    public String getPathName () {
        return getSubjectName(this.subj);
    }


    /**
     * @param sq
     * @return the share name
     * 
     */
    public static String getSubjectName ( SubjectInfo sq ) {
        if ( sq instanceof UserInfo ) {
            UserInfo u = (UserInfo) sq;
            return u.getPrincipal().toString();
        }
        else if ( sq instanceof GroupInfo ) {
            return ( (GroupQueryResult) sq ).getName();
        }
        return null;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getPathName()
     */
    @Override
    public String getDisplayName () {
        if ( this.subj instanceof UserQueryResult ) {
            return ( (UserQueryResult) this.subj ).getUserDisplayName();
        }
        else if ( this.subj instanceof GroupQueryResult ) {
            return ( (GroupQueryResult) this.subj ).getName();
        }
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getResourceType()
     */
    @Override
    protected ResourceType getResourceType () {
        return new ResourceType(new int[] {
            ResourceType.COLLECTION, Constants.VIRTUAL_RESOURCE_TYPE, Constants.SUBJECT_SHARES_ROOT_RESOURCE_TYPE
        });
    }
}
