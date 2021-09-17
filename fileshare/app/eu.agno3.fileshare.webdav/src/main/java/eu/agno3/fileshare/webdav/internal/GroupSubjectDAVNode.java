/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.04.2016 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


import java.util.Collection;
import java.util.Locale;

import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.property.ResourceType;

import eu.agno3.fileshare.model.GroupInfo;
import eu.agno3.fileshare.model.TrustLevel;


/**
 * @author mbechler
 *
 */
public class GroupSubjectDAVNode extends SubjectDAVNode {

    private GroupInfo groupInfo;


    /**
     * @param gi
     * @param tl
     * @param layout
     */
    public GroupSubjectDAVNode ( GroupInfo gi, TrustLevel tl, DAVLayout layout ) {
        super(gi.getId().toString(), tl, null, layout);
        this.groupInfo = gi;
    }


    /**
     * @return the groupInfo
     */
    public GroupInfo getGroupInfo () {
        return this.groupInfo;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getDisplayName()
     */
    @Override
    public String getDisplayName () {
        if ( this.groupInfo.getRealm() != null ) {
            return this.groupInfo.getName() + "@" + this.groupInfo.getRealm(); //$NON-NLS-1$
        }
        return this.groupInfo.getName();
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.SubjectDAVNode#getExtraProperties(java.util.Locale)
     */
    @Override
    public Collection<DavProperty<?>> getExtraProperties ( Locale l ) {
        Collection<DavProperty<?>> props = super.getExtraProperties(l);
        props.add(new DefaultDavProperty<>(Constants.SUBJECT_NAME, this.groupInfo.getName(), true));
        props.add(new DefaultDavProperty<>(Constants.SUBJECT_ID, this.groupInfo.getId(), true));
        props.add(new DefaultDavProperty<>(Constants.SUBJECT_REALM, this.groupInfo.getRealm(), true));
        props.add(new DefaultDavProperty<>(Constants.SUBJECT_NAME_SOURCE, this.groupInfo.getNameSource().name(), true));
        return props;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.SubjectDAVNode#getAbsolutePath()
     */
    @Override
    public String getAbsolutePath () {
        return GroupSubjectsDAVNode.SUBJECTS_GROUPS + '/' + this.groupInfo.getId();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.SubjectDAVNode#getResourceType()
     */
    @Override
    protected ResourceType getResourceType () {
        return new ResourceType(new int[] {
            Constants.PRINCIPAL_RESOURCE_TYPE, Constants.GROUP_RESOURCE_TYPE, Constants.VIRTUAL_RESOURCE_TYPE
        });
    }

}
