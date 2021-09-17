/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.04.2016 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Locale;

import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.property.ResourceType;

import eu.agno3.fileshare.model.MailGrant;
import eu.agno3.fileshare.model.TrustLevel;


/**
 * @author mbechler
 *
 */
public class MailSubjectDAVNode extends SubjectDAVNode {

    private String mailAddress;
    private MailGrant grant;


    /**
     * @param mailAddress
     * @param tl
     * @param layout
     */
    public MailSubjectDAVNode ( String mailAddress, TrustLevel tl, DAVLayout layout ) {
        super(mailAddress, tl, null, layout);
        this.mailAddress = mailAddress;
    }


    /**
     * @param g
     * @param tl
     * @param layout
     */
    public MailSubjectDAVNode ( MailGrant g, TrustLevel tl, DAVLayout layout ) {
        this(g.getMailAddress(), tl, layout);
        this.grant = g;

    }


    /**
     * @return the mailAddress
     */
    public String getMailAddress () {
        return this.mailAddress;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getDisplayName()
     */
    @Override
    public String getDisplayName () {
        return getMailAddress();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.SubjectDAVNode#getAbsolutePath()
     */
    @Override
    public String getAbsolutePath () {
        return MailSubjectsDAVNode.SUBJECTS_MAIL + '/' + this.getPathName();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getPathName()
     */
    @Override
    public String getPathName () {
        try {
            return URLEncoder.encode(getMailAddress(), "UTF-8"); //$NON-NLS-1$
        }
        catch ( UnsupportedEncodingException e ) {
            throw new AssertionError(e);
        }
    }


    @Override
    public Collection<DavProperty<?>> getExtraProperties ( Locale l ) {
        Collection<DavProperty<?>> props = super.getExtraProperties(l);

        if ( this.grant != null ) {
            props.add(new GrantDetailProperty(this.grant));
        }

        props.add(new DefaultDavProperty<>(Constants.MAIL, this.mailAddress, true));
        return props;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.SubjectDAVNode#getResourceType()
     */
    @Override
    protected ResourceType getResourceType () {
        return new ResourceType(new int[] {
            Constants.PRINCIPAL_RESOURCE_TYPE, Constants.MAIL_RESOURCE_TYPE, Constants.VIRTUAL_RESOURCE_TYPE
        });
    }

}
