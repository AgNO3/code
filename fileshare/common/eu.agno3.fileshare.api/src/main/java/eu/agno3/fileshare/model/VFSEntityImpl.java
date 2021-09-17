/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.10.2015 by mbechler
 */
package eu.agno3.fileshare.model;


import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTime;

import eu.agno3.runtime.util.serialization.SafeSerialization;


/**
 * @author mbechler
 *
 */
@SafeSerialization
public abstract class VFSEntityImpl implements VFSEntity, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1924284870430793292L;

    private VirtualGroup group;
    private SecurityLabel label;
    private DateTime created;
    private DateTime lastModified;
    private String relativePath;
    private String localName;

    private boolean haveParent;
    private boolean haveGrants;

    private VFSEntityKeyWithPath key;

    private Set<Grant> localValidGrants = new HashSet<>();

    private boolean staticReadOnly;

    private byte[] inode;

    private boolean sharable;


    /**
     * @param relativePath
     * @param group
     * @param readOnly
     * @throws IOException
     */
    public VFSEntityImpl ( String relativePath, VirtualGroup group, boolean readOnly ) throws IOException {
        this.relativePath = relativePath;
        this.key = new VFSEntityKeyWithPath(group.getVfs(), this.relativePath);
        this.staticReadOnly = readOnly;
    }


    /**
     * @param e
     */
    public VFSEntityImpl ( VFSEntityImpl e ) {
        this.key = e.key;
        this.relativePath = e.relativePath;
        this.group = e.group;
        this.label = e.label;
        this.created = e.created;
        this.lastModified = e.lastModified;
        this.localName = e.localName;

        this.haveParent = e.haveParent;
        this.haveGrants = e.haveGrants;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#getInode()
     */
    @Override
    public byte[] getInode () {
        return this.inode;
    }


    /**
     * @param inode
     *            the inode to set
     */
    public void setInode ( byte[] inode ) {
        this.inode = inode;
    }


    /**
     * @return the relative path
     */
    public String getRelativePath () {
        return this.relativePath;
    }


    /**
     * @return the group
     */
    public VirtualGroup getGroup () {
        return this.group;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#getEntityKey()
     */
    @Override
    public EntityKey getEntityKey () {
        return this.key;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#getLocalName()
     */
    @Override
    public String getLocalName () {
        return this.localName;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#setLocalName(java.lang.String)
     */
    @Override
    public void setLocalName ( String newName ) {
        this.localName = newName;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#getOwner()
     */
    @Override
    public Subject getOwner () {
        return this.group;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#setOwner(eu.agno3.fileshare.model.Subject)
     */
    @Override
    public void setOwner ( Subject owner ) {
        if ( ! ( owner instanceof VirtualGroup ) ) {
            throw new IllegalArgumentException();
        }
        this.group = (VirtualGroup) owner;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#getLastModified()
     */
    @Override
    public DateTime getLastModified () {
        return this.lastModified;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#setLastModified(org.joda.time.DateTime)
     */
    @Override
    public void setLastModified ( DateTime lastMod ) {
        this.lastModified = lastMod;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#getCreated()
     */
    @Override
    public DateTime getCreated () {
        return this.created;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#setCreated(org.joda.time.DateTime)
     */
    @Override
    public void setCreated ( DateTime created ) {
        this.created = created;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#getSecurityLabel()
     */
    @Override
    public SecurityLabel getSecurityLabel () {
        return this.label;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#setSecurityLabel(eu.agno3.fileshare.model.SecurityLabel)
     */
    @Override
    public void setSecurityLabel ( SecurityLabel securityLabel ) {
        this.label = securityLabel;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#getCreator()
     */
    @Override
    public User getCreator () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#getCreatorGrant()
     */
    @Override
    public Grant getCreatorGrant () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#getExpires()
     */
    @Override
    public DateTime getExpires () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#setExpires(org.joda.time.DateTime)
     */
    @Override
    public void setExpires ( DateTime newExpires ) {
        // ignore
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#setLastModifier(eu.agno3.fileshare.model.User)
     */
    @Override
    public void setLastModifier ( User currentUser ) {
        // ignore
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#getLastModifier()
     */
    @Override
    public User getLastModifier () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#getLastModifiedGrant()
     */
    @Override
    public Grant getLastModifiedGrant () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#setLastModifiedGrant(eu.agno3.fileshare.model.Grant)
     */
    @Override
    public void setLastModifiedGrant ( Grant grant ) {
        // ignore
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#setCreator(eu.agno3.fileshare.model.User)
     */
    @Override
    public void setCreator ( User currentUser ) {
        // ignore
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#setCreatorGrant(eu.agno3.fileshare.model.Grant)
     */
    @Override
    public void setCreatorGrant ( Grant grant ) {
        // ignore
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#hasParent()
     */
    @Override
    public boolean hasParent () {
        return this.haveParent;
    }


    /**
     * @param haveParent
     *            the haveParent to set
     */
    public void setHaveParent ( boolean haveParent ) {
        this.haveParent = haveParent;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#hasGrants()
     */
    @Override
    public boolean hasGrants () {
        return this.haveGrants;
    }


    /**
     * @param haveGrants
     *            the haveGrants to set
     */
    public void setHaveGrants ( boolean haveGrants ) {
        this.haveGrants = haveGrants;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#hasLocalValidGrants()
     */
    @Override
    public boolean hasLocalValidGrants () {
        return !this.localValidGrants.isEmpty();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#isStaticReadOnly()
     */
    @Override
    public boolean isStaticReadOnly () {
        return this.staticReadOnly;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#getLocalValidGrants()
     */
    @Override
    public Set<Grant> getLocalValidGrants () {
        return this.localValidGrants;
    }


    /**
     * @param localValidGrants
     *            the localValidGrants to set
     */
    public void setLocalValidGrants ( Set<Grant> localValidGrants ) {
        this.localValidGrants = localValidGrants;
    }


    /**
     * @return the sharable
     */
    @Override
    public boolean isStaticSharable () {
        return this.sharable;
    }


    /**
     * @param sharable
     */
    public void setSharable ( boolean sharable ) {
        this.sharable = sharable;
    }


    // +GENERATED
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(this.inode);
        result = prime * result + ( ( this.key == null ) ? 0 : this.key.hashCode() );
        return result;
    }

    // -GENERATED


    // +GENERATED
    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        VFSEntityImpl other = (VFSEntityImpl) obj;
        if ( !Arrays.equals(this.inode, other.inode) )
            return false;
        if ( this.key == null ) {
            if ( other.key != null )
                return false;
        }
        else if ( !this.key.equals(other.key) )
            return false;
        return true;
    }

    // -GENERATED

}
