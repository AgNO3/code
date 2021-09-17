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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.property.HrefProperty;
import org.apache.jackrabbit.webdav.util.HttpDateFormat;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.agno3.fileshare.model.ContentEntity;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.runtime.webdav.server.DAVTreeNode;
import eu.agno3.runtime.webdav.server.ResolvedHrefProperty;


/**
 * @author mbechler
 *
 */
public class EntityDAVNode implements DAVTreeNode<EntityKey> {

    private static final Logger log = Logger.getLogger(EntityDAVNode.class);

    private VFSEntity entity;
    private String overridePathName;
    private Long quotaAvailable;
    private Long quotaUsed;
    private Set<GrantPermission> permissions;
    private boolean shared;
    private DateTime overrideModificationTime;
    private DAVLayout layout;
    private UUID grantId;
    private EntityKey parentKey;


    /**
     * @param entity
     * @param parentKey
     * @param layout
     */
    public EntityDAVNode ( VFSEntity entity, EntityKey parentKey, DAVLayout layout ) {
        this.entity = entity;
        this.parentKey = parentKey;
        this.layout = layout;
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
     * @return the grantId
     */
    public UUID getGrantId () {
        return this.grantId;
    }


    /**
     * @param grantId
     *            the grantId to set
     */
    public void setGrantId ( UUID grantId ) {
        this.grantId = grantId;
    }


    /**
     * @param quotaAvailable
     *            the quotaAvailable to set
     */
    public void setQuotaAvailable ( Long quotaAvailable ) {
        this.quotaAvailable = quotaAvailable;
    }


    /**
     * @param quotaUsed
     *            the quotaUsed to set
     */
    public void setQuotaUsed ( Long quotaUsed ) {
        this.quotaUsed = quotaUsed;
    }


    /**
     * @return the entity
     */
    public VFSEntity getEntity () {
        return this.entity;
    }


    /**
     * @return the parentKey
     */
    public EntityKey getParentId () {
        return this.parentKey;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getId()
     */
    @Override
    public EntityKey getId () {
        if ( this.entity == null ) {
            return null;
        }
        return this.entity.getEntityKey();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#isCollection()
     */
    @Override
    public boolean isCollection () {
        return this.entity instanceof VFSContainerEntity;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getCreationTime()
     */
    @Override
    public DateTime getCreationTime () {
        return this.entity.getCreated();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getModificationTime()
     */
    @Override
    public DateTime getModificationTime () {
        if ( this.overrideModificationTime != null ) {
            return this.overrideModificationTime;
        }
        if ( this.entity instanceof VFSFileEntity ) {
            return ( (VFSFileEntity) this.entity ).getContentLastModified();
        }
        return this.entity.getLastModified();
    }


    /**
     * @param lastMod
     */
    public void setModificationTime ( DateTime lastMod ) {
        this.overrideModificationTime = lastMod;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getDisplayName()
     */
    @Override
    public String getDisplayName () {
        return this.entity.getLocalName();
    }


    /**
     * @param pathName
     */
    public void setOverridePath ( String pathName ) {
        this.overridePathName = pathName;
    }


    /**
     * @param permissions
     */
    public void setPermissions ( Set<GrantPermission> permissions ) {
        this.permissions = permissions;
    }


    /**
     * @return the permissions
     */
    public Set<GrantPermission> getPermissions () {
        return this.permissions;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getPathName()
     */
    @Override
    public String getPathName () {
        if ( this.overridePathName != null ) {
            return this.overridePathName;
        }
        return this.entity.getLocalName();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getContentType()
     */
    @Override
    public String getContentType () {
        if ( ! ( this.entity instanceof VFSFileEntity ) ) {
            return null;
        }

        VFSFileEntity f = (VFSFileEntity) this.entity;
        return f.getContentType();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getContentLength()
     */
    @Override
    public Long getContentLength () {
        if ( ! ( this.entity instanceof VFSFileEntity ) ) {
            return null;
        }

        VFSFileEntity f = (VFSFileEntity) this.entity;
        return f.getFileSize();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getETag()
     */
    @Override
    public String getETag () {
        if ( this.entity != null && this.entity.getLastModified() != null ) {
            return String.format("\"%s-%d\"", this.entity.getEntityKey(), this.getModificationTime().getMillis()); //$NON-NLS-1$
        }
        return null;
    }


    /**
     * @param shared
     */
    public void setShared ( boolean shared ) {
        this.shared = shared;
    }


    /**
     * @return the shared
     */
    public boolean isShared () {
        return this.shared;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getCustomHeaders()
     */
    @Override
    public Map<String, String> getCustomHeaders () {
        Map<String, String> hdrs = new HashMap<>();

        hdrs.put(
            "X-Parent-Id", //$NON-NLS-1$
            "urn:id:" + this.getParentId().toString()); //$NON-NLS-1$

        hdrs.put(
            "X-Object-Id", //$NON-NLS-1$
            "urn:id:" + this.getId().toString()); //$NON-NLS-1$

        hdrs.put("X-Display-Name", this.getDisplayName()); //$NON-NLS-1$
        if ( this.entity.getSecurityLabel() != null ) {
            hdrs.put("X-Security-Label", this.entity.getSecurityLabel().getLabel()); //$NON-NLS-1$
        }
        if ( this.entity.getExpires() != null ) {
            hdrs.put("X-Expires", HttpDateFormat.creationDateFormat().format(this.entity.getExpires().toDate())); //$NON-NLS-1$
        }

        if ( this.isCollection() ) {
            hdrs.put("X-Allow-File-Overwrite", String.valueOf( ( (VFSContainerEntity) this.entity ).getAllowFileOverwrite())); //$NON-NLS-1$
            hdrs.put("X-Send-Notifications", String.valueOf( ( (VFSContainerEntity) this.entity ).getSendNotifications())); //$NON-NLS-1$
        }

        return hdrs;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#isOverrideResourceType()
     */
    @Override
    public boolean isOverrideResourceType () {
        return false;
    }


    @Override
    public Collection<DavProperty<?>> getExtraProperties ( Locale l ) {
        List<DavProperty<?>> properties = new LinkedList<>();

        if ( this.entity instanceof VFSContainerEntity ) {
            addQuotaProperties(properties);
        }

        if ( this.entity != null ) {
            addCustomProperties(properties);
        }

        if ( this.layout == DAVLayout.OWNCLOUD ) {
            addOwncloudProperties(properties);
        }
        return properties;
    }


    /**
     * @param properties
     */
    private void addOwncloudProperties ( List<DavProperty<?>> properties ) {
        if ( this.entity instanceof VFSContainerEntity ) {
            Long s = ( (VFSContainerEntity) this.entity ).getChildrenSize();
            if ( s != null ) {
                properties.add(new DefaultDavProperty<>(Constants.OC_SIZE, s));
            }
        }
        else if ( this.entity instanceof VFSFileEntity ) {
            properties.add(new DefaultDavProperty<>(Constants.OC_SIZE, String.valueOf( ( (VFSFileEntity) this.entity ).getFileSize())));
        }

        String ocId = Constants.makeOcId(this.entity.getInode());
        if ( log.isTraceEnabled() ) {
            log.trace(String.format("OC ID for %s is %s", this.getId(), ocId)); //$NON-NLS-1$
        }
        properties.add(new DefaultDavProperty<>(Constants.OC_ID, ocId));
        String makeOCPErmissions = makeOCPErmissions(this.getPermissions(), this.entity, this.isShared());
        if ( !StringUtils.isBlank(makeOCPErmissions) ) {
            properties.add(new DefaultDavProperty<>(Constants.OC_PERMISSIONS, makeOCPErmissions));
        }
    }


    /**
     * @param properties
     */
    private void addCustomProperties ( List<DavProperty<?>> properties ) {
        if ( this.parentKey != null ) {
            properties.add(new HrefProperty(Constants.PARENT_ID, "urn:id:" + this.parentKey.toString(), false)); //$NON-NLS-1$
        }

        if ( this.entity.getSecurityLabel() != null ) {
            properties.add(new DefaultDavProperty<>(Constants.SECURITY_LABEL, this.entity.getSecurityLabel().getLabel(), false));
        }

        if ( this.entity.getLastModified() != null ) {
            properties.add(
                new DefaultDavProperty<>(
                    Constants.LAST_MODIFIED,
                    HttpDateFormat.creationDateFormat().format(this.entity.getLastModified().toDate()),
                    false));
        }

        if ( this.entity.getExpires() != null ) {
            properties.add(
                new DefaultDavProperty<>(
                    Constants.EXPIRES_PROP,
                    HttpDateFormat.creationDateFormat().format(this.entity.getExpires().toDate()),
                    false));
        }

        if ( this.entity.getCreator() != null ) {
            properties
                    .add(new ResolvedHrefProperty(Constants.CREATOR, SubjectsSubtreeProvider.getSubjectUrl(this.entity.getCreator()), false, false));
        }
        else if ( this.entity.getCreatorGrant() != null ) {
            properties.add(
                new ResolvedHrefProperty(Constants.CREATOR, SubjectsSubtreeProvider.getGrantUrl(this.entity.getCreatorGrant()), false, false));
        }

        if ( this.entity.getLastModifier() != null ) {
            properties.add(
                new ResolvedHrefProperty(
                    Constants.LAST_MODIFIER,
                    SubjectsSubtreeProvider.getSubjectUrl(this.entity.getLastModifier()),
                    false,
                    false));
        }
        else if ( this.entity.getLastModifiedGrant() != null ) {
            properties.add(
                new ResolvedHrefProperty(Constants.CREATOR, SubjectsSubtreeProvider.getGrantUrl(this.entity.getLastModifiedGrant()), false, false));
        }
    }


    /**
     * @param properties
     */
    private void addQuotaProperties ( List<DavProperty<?>> properties ) {
        if ( this.quotaAvailable != null ) {
            properties.add(new DefaultDavProperty<>(Constants.QUOTA_AVAIL, this.quotaAvailable));
        }

        if ( this.quotaUsed != null ) {
            properties.add(new DefaultDavProperty<>(Constants.QUOTA_USED, this.quotaUsed));
        }
        else if ( ( (VFSContainerEntity) this.entity ).getChildrenSize() != null ) {
            properties.add(new DefaultDavProperty<>(Constants.QUOTA_USED, ( (VFSContainerEntity) this.entity ).getChildrenSize()));
        }
        else {
            properties.add(new DefaultDavProperty<>(Constants.QUOTA_USED, 0));
        }
    }


    /**
     * @return the tree layout
     */
    public DAVLayout getLayout () {
        return this.layout;
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
     * @param perms
     * @param shared
     * @return
     */
    private static String makeOCPErmissions ( Set<GrantPermission> perms, VFSEntity e, boolean shared ) {
        StringBuilder sb = new StringBuilder();

        // K - add directories
        // C - add files
        // W - write file
        // D - delete
        // S - shared directory
        // N - rename
        // V - move
        // R - reshare
        // M - mounted

        if ( perms == null ) {
            return StringUtils.EMPTY;
        }

        if ( perms.contains(GrantPermission.UPLOAD) ) {
            sb.append('K');
            sb.append('C');
        }

        if ( perms.contains(GrantPermission.EDIT) ) {
            sb.append('W');
            sb.append('D');
            sb.append('N');
            sb.append('V');
        }

        if ( shared ) {
            sb.append('S');
        }

        if ( ! ( e instanceof ContentEntity ) ) {
            sb.append('M');
        }

        return sb.toString();
    }


    // +GENERATED
    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.entity == null ) ? 0 : this.entity.hashCode() );
        result = prime * result + ( ( this.grantId == null ) ? 0 : this.grantId.hashCode() );
        return result;
    }

    // -GENERATED


    // +GENERATED
    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        EntityDAVNode other = (EntityDAVNode) obj;
        if ( this.entity == null ) {
            if ( other.entity != null )
                return false;
        }
        else if ( !this.entity.equals(other.entity) )
            return false;
        if ( this.grantId == null ) {
            if ( other.grantId != null )
                return false;
        }
        else if ( !this.grantId.equals(other.grantId) )
            return false;
        return true;
    }

    // -GENERATED

}
