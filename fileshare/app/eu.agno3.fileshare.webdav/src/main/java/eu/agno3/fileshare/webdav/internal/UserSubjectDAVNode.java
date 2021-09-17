/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.04.2016 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


import java.util.Collection;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.property.ResourceType;

import eu.agno3.fileshare.model.TrustLevel;
import eu.agno3.fileshare.model.UserDetails;
import eu.agno3.fileshare.model.UserInfo;


/**
 * @author mbechler
 *
 */
public class UserSubjectDAVNode extends SubjectDAVNode {

    private UserInfo userInfo;
    private UserDetails userDetails;


    /**
     * @param ui
     * @param userDetails
     * @param tl
     * @param layout
     */
    public UserSubjectDAVNode ( UserInfo ui, UserDetails userDetails, TrustLevel tl, DAVLayout layout ) {
        super(ui.getId().toString(), tl, null, layout);
        this.userInfo = ui;
        this.userDetails = userDetails;
    }


    /**
     * @return the userInfo
     */
    public UserInfo getUserInfo () {
        return this.userInfo;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getDisplayName()
     */
    @Override
    public String getDisplayName () {
        return this.userInfo.getUserDisplayName();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.SubjectDAVNode#getAbsolutePath()
     */
    @Override
    public String getAbsolutePath () {
        return UserSubjectsDAVNode.SUBJECTS_USERS + '/' + this.userInfo.getId();
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
        props.add(new DefaultDavProperty<>(Constants.SUBJECT_NAME, this.userInfo.getPrincipal().getUserName(), true));
        props.add(new DefaultDavProperty<>(Constants.SUBJECT_ID, this.userInfo.getPrincipal().getUserId(), true));
        props.add(new DefaultDavProperty<>(Constants.SUBJECT_REALM, this.userInfo.getPrincipal().getRealmName(), true));
        props.add(new DefaultDavProperty<>(Constants.SUBJECT_NAME_SOURCE, this.userInfo.getNameSource().name(), true));

        if ( this.userDetails != null ) {
            addDetails(props);
        }

        return props;
    }


    /**
     * @param props
     */
    private void addDetails ( Collection<DavProperty<?>> props ) {
        if ( !StringUtils.isBlank(this.userDetails.getPreferredName()) ) {
            props.add(
                new DefaultDavProperty<>(
                    Constants.USER_FULLNAME,
                    new VerifiedString(this.userDetails.getPreferredName(), this.userDetails.getPreferredNameVerified()),
                    true));
        }
        if ( !StringUtils.isBlank(this.userDetails.getMailAddress()) ) {
            props.add(
                new DefaultDavProperty<>(
                    Constants.USER_MAIL,
                    new VerifiedString(this.userDetails.getMailAddress(), this.userDetails.getMailAddressVerified()),
                    true));
        }
        if ( !StringUtils.isBlank(this.userDetails.getJobTitle()) ) {
            props.add(new DefaultDavProperty<>(Constants.USER_JOB_TITLE, new VerifiedString(this.userDetails.getJobTitle(), true), true));
        }

        if ( !StringUtils.isBlank(this.userDetails.getOrganization()) ) {
            props.add(new DefaultDavProperty<>(Constants.USER_ORGANIZATION, new VerifiedString(this.userDetails.getOrganization(), true), true));
        }

        if ( !StringUtils.isBlank(this.userDetails.getOrganizationUnit()) ) {
            props.add(
                new DefaultDavProperty<>(Constants.USER_ORGANIZATION_UNIT, new VerifiedString(this.userDetails.getOrganizationUnit(), true), true));
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.SubjectDAVNode#getResourceType()
     */
    @Override
    protected ResourceType getResourceType () {
        return new ResourceType(new int[] {
            Constants.PRINCIPAL_RESOURCE_TYPE, Constants.USER_RESOURCE_TYPE, Constants.VIRTUAL_RESOURCE_TYPE
        });
    }
}
