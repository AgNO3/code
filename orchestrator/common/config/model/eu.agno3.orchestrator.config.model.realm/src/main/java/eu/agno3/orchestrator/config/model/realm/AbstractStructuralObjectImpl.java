/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.OptimisticLock;
import org.hibernate.envers.AuditOverride;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.joda.time.DateTime;

import eu.agno3.orchestrator.config.model.base.tree.NestedSetNodeImpl;
import eu.agno3.orchestrator.config.model.base.tree.TreeNodeHolder;
import eu.agno3.orchestrator.config.model.realm.license.LicenseStorage;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibrary;


/**
 * @author mbechler
 * 
 */
@Entity
@Table ( name = "structure", indexes = {
    @Index ( columnList = "l, r", name = "structTree" )
}, uniqueConstraints = {
    @UniqueConstraint ( columnNames = "l", name = "uniqueL" ), @UniqueConstraint ( columnNames = "r", name = "uniqueR" )
})
@Inheritance ( strategy = InheritanceType.JOINED )
// TODO: disabled because of hibernate bug HHH-9303
// @DiscriminatorColumn ( name = "type", discriminatorType = DiscriminatorType.STRING )
// @Cache ( usage = CacheConcurrencyStrategy.TRANSACTIONAL )
@PersistenceUnit ( unitName = "config" )
@Audited
@XmlTransient
public abstract class AbstractStructuralObjectImpl extends AbstractObject
        implements StructuralObject, StructuralObjectMutable, StructuralObjectReference, TreeNodeHolder {

    /**
     * 
     */
    private static final long serialVersionUID = 7810126951389462192L;
    private String displayName;

    private NestedSetNodeImpl treeNode = new NestedSetNodeImpl();

    private Set<ConfigurationObjectMutable> objectPool = new HashSet<>();

    private Set<ConfigurationObjectMutable> defaultObjects = new HashSet<>();
    private Set<ConfigurationObjectMutable> enforcedObjects = new HashSet<>();

    private Set<ResourceLibrary> resourceLibraries = new HashSet<>();

    private Set<LicenseStorage> licensePool = new HashSet<>();

    private StructuralObjectState overallState;
    private StructuralObjectState persistentState;

    private DateTime resourceLibraryLastSync;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.StructuralObjectMutable#getType()
     */
    @Override
    @Transient
    public abstract StructuralObjectType getType ();


    /**
     * @return the treeNode
     */
    @Override
    @Embedded
    @AuditOverride ( isAudited = false )
    public NestedSetNodeImpl getTreeNode () {
        return this.treeNode;
    }


    /**
     * @param treeNode
     *            the treeNode to set
     */
    public void setTreeNode ( NestedSetNodeImpl treeNode ) {
        this.treeNode = treeNode;
    }


    /**
     * @return the persistentState
     */
    @Enumerated ( EnumType.STRING )
    public StructuralObjectState getPersistentState () {
        return this.persistentState;
    }


    /**
     * @param persistentState
     *            the persistentState to set
     */
    public void setPersistentState ( StructuralObjectState persistentState ) {
        this.persistentState = persistentState;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.StructuralObject#getOverallState()
     */
    @Override
    @Transient
    public StructuralObjectState getOverallState () {
        return this.overallState;
    }


    /**
     * @param overallState
     *            the overallState to set
     */
    public void setOverallState ( StructuralObjectState overallState ) {
        this.overallState = overallState;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.StructuralObjectMutable#getDisplayName()
     */
    @Override
    @Basic
    @Column ( nullable = true )
    public String getDisplayName () {
        return this.displayName;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.StructuralObjectMutable#setDisplayName(java.lang.String)
     */
    @Override
    public void setDisplayName ( String displayName ) {
        this.displayName = displayName;
    }


    /**
     * @return the resourceLibraryLastSync
     */
    @NotAudited
    @OptimisticLock ( excluded = true )
    public DateTime getResourceLibraryLastSync () {
        return this.resourceLibraryLastSync;
    }


    /**
     * @param resourceLibraryLastSync
     *            the resourceLibraryLastSync to set
     */
    public void setResourceLibraryLastSync ( DateTime resourceLibraryLastSync ) {
        this.resourceLibraryLastSync = resourceLibraryLastSync;
    }


    /**
     * 
     * @return the attached objects
     */
    @OneToMany ( cascade = {
        CascadeType.REMOVE
    }, mappedBy = "anchor", fetch = FetchType.LAZY, orphanRemoval = true, targetEntity = AbstractConfigurationObject.class )
    public Set<ConfigurationObjectMutable> getObjectPool () {
        return this.objectPool;
    }


    /**
     * 
     * @param obj
     */
    public void addObject ( ConfigurationObjectMutable obj ) {
        this.getObjectPool().add(obj);
        if ( obj instanceof AbstractConfigurationObject<?> ) {
            ( (AbstractConfigurationObject<?>) obj ).setAnchor(this);
        }
    }


    /**
     * 
     * @param objectPool
     */
    public void setObjectPool ( Set<ConfigurationObjectMutable> objectPool ) {
        this.objectPool = objectPool;
    }


    /**
     * @return the defaultObjects
     */
    @ManyToMany ( cascade = {}, fetch = FetchType.LAZY, targetEntity = AbstractConfigurationObject.class )
    @JoinTable (
        name = "structural_defaults",
        joinColumns = @JoinColumn ( referencedColumnName = "id" ) ,
        inverseJoinColumns = @JoinColumn ( referencedColumnName = "id" ) )
    public Set<ConfigurationObjectMutable> getDefaultObjects () {
        return this.defaultObjects;
    }


    /**
     * @param defaultObjects
     *            the defaultObjects to set
     */
    public void setDefaultObjects ( Set<ConfigurationObjectMutable> defaultObjects ) {
        this.defaultObjects = defaultObjects;
    }


    /**
     * @return the enforcedObjects
     */
    @JoinTable (
        name = "structural_enforment",
        joinColumns = @JoinColumn ( referencedColumnName = "id" ) ,
        inverseJoinColumns = @JoinColumn ( referencedColumnName = "id" ) )
    @ManyToMany ( cascade = {}, fetch = FetchType.LAZY, targetEntity = AbstractConfigurationObject.class )
    public Set<ConfigurationObjectMutable> getEnforcedObjects () {
        return this.enforcedObjects;
    }


    /**
     * @param enforcedObjects
     *            the enforcedObjects to set
     */
    public void setEnforcedObjects ( Set<ConfigurationObjectMutable> enforcedObjects ) {
        this.enforcedObjects = enforcedObjects;
    }


    /**
     * @return the resourceLibraries
     */
    @OneToMany ( cascade = {
        CascadeType.PERSIST, CascadeType.REMOVE
    }, mappedBy = "anchor", fetch = FetchType.LAZY, orphanRemoval = true )
    public Set<ResourceLibrary> getResourceLibraries () {
        return this.resourceLibraries;
    }


    /**
     * @param resourceLibraries
     *            the resourceLibraries to set
     */
    public void setResourceLibraries ( Set<ResourceLibrary> resourceLibraries ) {
        this.resourceLibraries = resourceLibraries;
    }


    /**
     * @return the attachedLicenses
     */
    @OneToMany ( cascade = {
        CascadeType.PERSIST, CascadeType.REMOVE
    }, mappedBy = "anchor", fetch = FetchType.LAZY, orphanRemoval = true )
    @NotAudited
    public Set<LicenseStorage> getLicensePool () {
        return this.licensePool;
    }


    /**
     * @param attachedLicenses
     *            the attachedLicenses to set
     */
    public void setLicensePool ( Set<LicenseStorage> attachedLicenses ) {
        this.licensePool = attachedLicenses;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format("%s: %s (%s)", this.getType(), this.getDisplayName(), this.getId()); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {

        if ( obj instanceof AbstractStructuralObjectImpl ) {
            AbstractStructuralObjectImpl struct = (AbstractStructuralObjectImpl) obj;

            if ( !struct.getType().equals(this.getType()) ) {
                return false;
            }

            if ( this.getId() == null ) {
                return super.equals(obj);
            }

            return this.getId().equals(struct.getId());
        }

        return super.equals(obj);
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        if ( this.getId() == null ) {
            return super.hashCode();
        }
        return this.getType().hashCode() + 5 * this.getId().hashCode();
    }
}
