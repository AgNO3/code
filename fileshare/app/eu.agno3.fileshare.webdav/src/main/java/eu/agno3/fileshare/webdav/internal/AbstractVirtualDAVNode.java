/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.06.2015 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.property.HrefProperty;
import org.apache.jackrabbit.webdav.property.ResourceType;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.joda.time.DateTime;

import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.runtime.webdav.server.DAVTreeNode;


/**
 * @author mbechler
 *
 */
public abstract class AbstractVirtualDAVNode implements DAVTreeNode<EntityKey> {

    private String name;
    private DateTime lastModified;
    private DAVLayout layout;
    private EntityKey parentId;


    /**
     * @param name
     * @param parentId
     * @param lastModified
     * @param layout
     * 
     */
    public AbstractVirtualDAVNode ( String name, EntityKey parentId, DateTime lastModified, DAVLayout layout ) {
        this.name = name;
        this.parentId = parentId;
        this.lastModified = lastModified;
        this.layout = layout;
    }


    /**
     * @return the layout
     */
    public DAVLayout getLayout () {
        return this.layout;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getId()
     */
    @Override
    public EntityKey getId () {
        return null;
    }


    protected abstract byte[] getInode ();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#isCollection()
     */
    @Override
    public boolean isCollection () {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getCreationTime()
     */
    @Override
    public DateTime getCreationTime () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getModificationTime()
     */
    @Override
    public DateTime getModificationTime () {
        return this.lastModified;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getDisplayName()
     */
    @Override
    public String getDisplayName () {
        return this.name;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getPathName()
     */
    @Override
    public String getPathName () {
        return getDisplayName();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getContentType()
     */
    @Override
    public String getContentType () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getContentLength()
     */
    @Override
    public Long getContentLength () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getETag()
     */
    @Override
    public String getETag () {
        if ( this.getId() == null && this.getModificationTime() == null ) {
            return null;
        }
        else if ( this.getId() == null ) {
            return String.valueOf(this.getModificationTime() != null ? this.getModificationTime().getMillis() : null);
        }
        else if ( this.getModificationTime() == null ) {
            return String.valueOf(this.getId());
        }
        return String.format("\"%s-%d\"", this.getId(), this.getModificationTime().getMillis()); //$NON-NLS-1$ ;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getCustomHeaders()
     */
    @Override
    public Map<String, String> getCustomHeaders () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#isOverrideResourceType()
     */
    @Override
    public boolean isOverrideResourceType () {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getSupportedReports()
     */
    @Override
    public Set<ReportType> getSupportedReports () {
        return Collections.EMPTY_SET;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getSupportedMethods()
     */
    @Override
    public Collection<String> getSupportedMethods () {
        return Arrays.asList(Constants.BASE_METHODS);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getExtraProperties(java.util.Locale)
     */
    @Override
    public Collection<DavProperty<?>> getExtraProperties ( Locale l ) {
        List<DavProperty<?>> properties = new LinkedList<>();

        properties.add(getResourceType());

        if ( this.parentId != null ) {
            properties.add(new HrefProperty(Constants.PARENT_ID, "urn:id:" + this.parentId, false)); //$NON-NLS-1$
        }

        if ( this.layout == DAVLayout.OWNCLOUD ) {
            byte[] inode = this.getInode();
            if ( inode != null ) {
                properties.add(new DefaultDavProperty<>(Constants.OC_ID, Constants.makeOcId(inode)));
            }

            properties.add(new DefaultDavProperty<>(Constants.OC_PERMISSIONS, "S")); //$NON-NLS-1$
        }
        properties.add(new DefaultDavProperty<>(Constants.PERMISSIONS, GrantPermission.BROWSE.getStableId(), false));
        return properties;
    }


    /**
     * @return
     */
    protected ResourceType getResourceType () {
        if ( isCollection() ) {
            return new ResourceType(new int[] {
                ResourceType.COLLECTION, Constants.VIRTUAL_RESOURCE_TYPE
            });
        }

        return new ResourceType(new int[] {
            Constants.VIRTUAL_RESOURCE_TYPE
        });
    }
}
