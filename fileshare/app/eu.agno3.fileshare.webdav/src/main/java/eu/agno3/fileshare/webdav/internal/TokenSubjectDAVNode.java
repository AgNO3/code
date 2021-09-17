/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.04.2016 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


import java.util.Collection;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.property.ResourceType;

import eu.agno3.fileshare.model.TokenGrant;
import eu.agno3.fileshare.model.TrustLevel;


/**
 * @author mbechler
 *
 */
public class TokenSubjectDAVNode extends SubjectDAVNode {

    private TokenGrant grant;


    /**
     * @param tg
     * @param tl
     * @param layout
     */
    public TokenSubjectDAVNode ( TokenGrant tg, TrustLevel tl, DAVLayout layout ) {
        super(tg.getId().toString(), tl, null, layout);
        this.grant = tg;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.SubjectDAVNode#getAbsolutePath()
     */
    @Override
    public String getAbsolutePath () {
        return TokenSubjectsDAVNode.SUBJECTS_TOKENS + '/' + this.grant.getId();
    }


    /**
     * @return the grant
     */
    public TokenGrant getGrant () {
        return this.grant;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getDisplayName()
     */
    @Override
    public String getDisplayName () {
        return getGrant().getIdentifier();
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

        props.add(new GrantDetailProperty(this.grant));

        props.add(new DefaultDavProperty<>(Constants.TOK_ID_PROP, this.grant.getIdentifier(), true));
        if ( !StringUtils.isBlank(this.grant.getComment()) ) {
            props.add(new DefaultDavProperty<>(Constants.TOK_COMMENT_PROP, this.grant.getComment(), true));
        }

        if ( this.grant.getPasswordProtected() && !StringUtils.isBlank(this.grant.getPassword()) ) {
            props.add(new DefaultDavProperty<>(Constants.TOK_PASSWORD_PROP, this.grant.getPassword(), true));
        }
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
            Constants.PRINCIPAL_RESOURCE_TYPE, Constants.TOKEN_RESOURCE_TYPE, Constants.VIRTUAL_RESOURCE_TYPE
        });
    }

}
