/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.01.2015 by mbechler
 */
package eu.agno3.fileshare.model;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.OptimisticLock;
import org.joda.time.DateTime;


/**
 * @author mbechler
 *
 */
@PersistenceUnit ( unitName = "fileshare" )
@Entity
@Inheritance ( strategy = InheritanceType.JOINED )
@Table ( name = "content_container" )
public class ContainerEntity extends ContentEntity implements VFSContainerEntity {

    /**
     * 
     */
    private static final long serialVersionUID = -80557458947246786L;
    private Set<ContentEntity> elements;
    private boolean allowFileOverwrite;
    private boolean sendNotifications;

    private DateTime recursiveLastModified;

    private long childrenSize;
    private Long numChildren;

    private Set<ContainerChangeEntry> changes = new HashSet<>();


    /**
     * 
     */
    public ContainerEntity () {}


    /**
     * 
     * @param e
     * @param refs
     */
    public ContainerEntity ( ContainerEntity e, boolean refs ) {
        super(e, refs);
        this.allowFileOverwrite = e.allowFileOverwrite;
        this.sendNotifications = e.sendNotifications;
        this.childrenSize = e.childrenSize;
        this.numChildren = e.getNumChildren();
    }


    /**
     * @return the number of child entries
     */
    @Override
    @Transient
    public Long getNumChildren () {
        if ( this.elements != null ) {
            return (long) this.elements.size();
        }
        return this.numChildren;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSContainerEntity#isEmpty()
     */
    @Override
    @Transient
    public boolean isEmpty () {
        return this.getNumChildren() == 0;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.ContentEntity#cloneShallow(boolean)
     */
    @Override
    public ContainerEntity cloneShallow ( boolean refs ) {
        return new ContainerEntity(this, refs);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.ContentEntity#cloneShallow()
     */
    @Override
    public ContainerEntity cloneShallow () {
        return (ContainerEntity) super.cloneShallow();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.ContentEntity#getEntityType()
     */
    @Override
    @Transient
    public EntityType getEntityType () {
        return EntityType.DIRECTORY;
    }


    /**
     * @return the elements
     */
    @OneToMany ( mappedBy = "parent", fetch = FetchType.LAZY, cascade = {
        CascadeType.REMOVE
    } )
    public Set<ContentEntity> getElements () {
        return this.elements;
    }


    /**
     * @param elements
     *            the elements to set
     */
    public void setElements ( Set<ContentEntity> elements ) {
        this.elements = elements;
    }


    /**
     * @return the changes
     */
    @OneToMany ( mappedBy = "container", fetch = FetchType.LAZY, cascade = {
        CascadeType.REMOVE
    } )
    public Set<ContainerChangeEntry> getChanges () {
        return this.changes;
    }


    /**
     * @param changes
     *            the changes to set
     */
    public void setChanges ( Set<ContainerChangeEntry> changes ) {
        this.changes = changes;
    }


    /**
     * @return whether to allow file overwriting
     */
    @Override
    public boolean getAllowFileOverwrite () {
        return this.allowFileOverwrite;
    }


    /**
     * @param allowFileOverwrite
     *            the allowFileOverwrite to set
     */
    @Override
    public void setAllowFileOverwrite ( boolean allowFileOverwrite ) {
        this.allowFileOverwrite = allowFileOverwrite;
    }


    /**
     * @return the sendNotifications
     */
    @Override
    public boolean getSendNotifications () {
        return this.sendNotifications;
    }


    /**
     * @param sendNotifications
     *            the sendNotifications to set
     */
    @Override
    public void setSendNotifications ( boolean sendNotifications ) {
        this.sendNotifications = sendNotifications;
    }


    /**
     * @return the childrenSize
     */
    @Override
    @OptimisticLock ( excluded = true )
    public Long getChildrenSize () {
        return this.childrenSize;
    }


    /**
     * @param childrenSize
     *            the childrenSize to set
     */
    public void setChildrenSize ( Long childrenSize ) {
        this.childrenSize = childrenSize;
    }


    /**
     * @return the recursiveLastModified
     */
    @OptimisticLock ( excluded = true )
    public DateTime getRecursiveLastModified () {
        return this.recursiveLastModified;
    }


    /**
     * @param recursiveLastModified
     *            the recursiveLastModified to set
     */
    public void setRecursiveLastModified ( DateTime recursiveLastModified ) {
        this.recursiveLastModified = recursiveLastModified;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#isStaticSharable()
     */
    @Override
    @Transient
    public boolean isStaticSharable () {
        return true;
    }

}
