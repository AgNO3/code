/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.06.2015 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.property.ResourceType;
import org.apache.jackrabbit.webdav.security.report.AclPrincipalReport;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.joda.time.DateTime;

import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.NativeEntityKey;
import eu.agno3.runtime.util.uuid.UUIDUtil;
import eu.agno3.runtime.webdav.server.colsync.ColSyncReport;


/**
 * @author mbechler
 *
 */
public class RootDAVNode extends AbstractVirtualDAVNode {

    private Long quotaUsed;
    private Long quotaAvailable;
    private UUID userId;


    /**
     * @param lastModified
     * @param userId
     * @param layout
     * 
     */
    public RootDAVNode ( DateTime lastModified, UUID userId, DAVLayout layout ) {
        super(null, null, lastModified, layout);
        this.userId = userId;
    }


    /**
     * @param lastModified
     * @param userId
     * @param layout
     * @param quotaAvailable
     * @param quotaUsed
     * 
     */
    public RootDAVNode ( DateTime lastModified, UUID userId, DAVLayout layout, Long quotaAvailable, Long quotaUsed ) {
        super(null, null, lastModified, layout);
        this.userId = userId;
        this.quotaAvailable = quotaAvailable;
        this.quotaUsed = quotaUsed;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getId()
     */
    @Override
    public EntityKey getId () {
        return new NativeEntityKey(this.userId);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getAbsolutePath()
     */
    @Override
    public String getAbsolutePath () {
        return "/"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getInode()
     */
    @Override
    protected byte[] getInode () {
        return UUIDUtil.toBytes(this.userId);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getResourceType()
     */
    @Override
    protected ResourceType getResourceType () {
        return new ResourceType(new int[] {
            ResourceType.COLLECTION, Constants.VIRTUAL_RESOURCE_TYPE, Constants.ROOT_RESOURCE_TYPE
        });
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getSupportedReports()
     */
    @Override
    public Set<ReportType> getSupportedReports () {
        if ( getLayout() == DAVLayout.NATIVE ) {
            return new HashSet<>(Arrays.asList(ColSyncReport.REPORT_TYPE, AclPrincipalReport.REPORT_TYPE));
        }
        return super.getSupportedReports();
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getExtraProperties(java.util.Locale)
     */
    @Override
    public Collection<DavProperty<?>> getExtraProperties ( Locale l ) {
        Collection<DavProperty<?>> properties = new LinkedList<>();
        properties.add(getResourceType());
        if ( this.quotaAvailable != null ) {
            properties.add(new DefaultDavProperty<>(Constants.QUOTA_AVAIL, this.quotaAvailable));
        }
        else {
            properties.add(new DefaultDavProperty<>(Constants.QUOTA_AVAIL, 0));
        }
        if ( this.quotaUsed != null ) {
            properties.add(new DefaultDavProperty<>(Constants.QUOTA_USED, this.quotaUsed));
        }
        return properties;
    }
}
