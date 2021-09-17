/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.04.2016 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.ResourceType;
import org.joda.time.DateTime;

import eu.agno3.fileshare.model.TrustLevel;


/**
 * @author mbechler
 *
 */
public abstract class SubjectDAVNode extends AbstractVirtualDAVNode {

    private TrustLevel trustLevel;


    /**
     * @param pathName
     * @param tl
     * @param lastModified
     * @param layout
     */
    public SubjectDAVNode ( String pathName, TrustLevel tl, DateTime lastModified, DAVLayout layout ) {
        super(pathName, null, null, layout);
        this.trustLevel = tl;
    }


    /**
     * @return the trustLevel
     */
    public TrustLevel getTrustLevel () {
        return this.trustLevel;
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
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getContentLength()
     */
    @Override
    public Long getContentLength () {
        return 0L;
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
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getInode()
     */
    @Override
    protected byte[] getInode () {
        return null;
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
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getExtraProperties(java.util.Locale)
     */
    @Override
    public Collection<DavProperty<?>> getExtraProperties ( Locale l ) {
        List<DavProperty<?>> properties = new LinkedList<>();
        properties.add(getResourceType());

        if ( this.trustLevel != null ) {
            properties.add(new TrustLevelProperty(this.trustLevel, l));
        }

        return properties;
    }


    /**
     * @return
     */
    @Override
    protected ResourceType getResourceType () {
        return new ResourceType(new int[] {
            Constants.PRINCIPAL_RESOURCE_TYPE, Constants.VIRTUAL_RESOURCE_TYPE
        });
    }
}
