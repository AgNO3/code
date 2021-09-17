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
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;


/**
 * @author mbechler
 * @param <T>
 *            the configuration object type
 * 
 */
@Entity
@Table ( name = "config" )
@Inheritance ( strategy = InheritanceType.JOINED )
@PersistenceUnit ( unitName = "config" )
// TODO: disabled because of hibernate bug HHH-9303
// @DiscriminatorColumn ( name = "type", discriminatorType = DiscriminatorType.STRING )
@Audited
@XmlTransient
public abstract class AbstractConfigurationObject <T extends ConfigurationObject> extends AbstractObject implements ConfigurationObjectMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -298286182501415454L;
    private String localName;
    private String displayName;

    private Long revision;

    private AbstractStructuralObjectImpl anchor;

    private ConfigurationObject inherits;

    private Set<@NonNull ConfigurationObject> inheritedBy = new HashSet<>();

    private Set<@NonNull ConfigurationObject> uses = new HashSet<>();
    private Set<@NonNull ConfigurationObject> usedBy = new HashSet<>();

    private ConfigurationObject outerObject;
    private Set<@NonNull ConfigurationObject> subObjects = new HashSet<>();

    private Set<@NonNull StructuralObject> defaultFor = new HashSet<>();
    private Set<@NonNull StructuralObject> enforcedFor = new HashSet<>();


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.ConfigurationObject#getType()
     */
    @Override
    @Transient
    @NonNull
    public abstract Class<T> getType ();


    /**
     * @return the revision
     */
    @Override
    @Transient
    public Long getRevision () {
        return this.revision;
    }


    /**
     * @param revision
     *            the revision to set
     */
    public void setRevision ( Long revision ) {
        this.revision = revision;
    }


    /**
     * @return the name
     */
    @Override
    @Basic
    @Column ( nullable = true )
    public String getName () {
        return this.localName;
    }


    /**
     * @param name
     *            the name to set
     */
    @Override
    public void setName ( String name ) {
        this.localName = name;
    }


    @Override
    @Basic
    @Column ( nullable = true )
    public String getDisplayName () {
        return this.displayName;
    }


    /**
     * @param displayName
     */
    public void setDisplayName ( String displayName ) {
        this.displayName = displayName;
    }


    /**
     * 
     * @return the object that this configuration is attached to
     */
    @ManyToOne ( fetch = FetchType.LAZY, cascade = {} )
    public AbstractStructuralObjectImpl getAnchor () {
        return this.anchor;
    }


    /**
     * @param anchor
     *            the anchor to set
     */
    public void setAnchor ( AbstractStructuralObjectImpl anchor ) {
        this.anchor = anchor;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.ConfigurationObject#getInherits()
     */
    @Override
    @ManyToOne ( cascade = {}, fetch = FetchType.LAZY, targetEntity = AbstractConfigurationObject.class )
    public ConfigurationObject getInherits () {
        return this.inherits;
    }


    /**
     * @param inherits
     *            the inherits to set
     */
    @Override
    public void setInherits ( ConfigurationObject inherits ) {
        if ( inherits instanceof AbstractConfigurationObject<?> && ( (AbstractConfigurationObject<?>) inherits ).getInheritedBy() != null ) {
            ( (AbstractConfigurationObject<?>) inherits ).getInheritedBy().add(this);
        }
        this.inherits = inherits;
    }


    /**
     * 
     * @return the objects that inherit this object
     */
    @OneToMany ( mappedBy = "inherits", targetEntity = AbstractConfigurationObject.class )
    public Set<@NonNull ConfigurationObject> getInheritedBy () {
        return this.inheritedBy;
    }


    /**
     * @param inheritedBy
     *            the inheritedBy to set
     */
    public void setInheritedBy ( Set<@NonNull ConfigurationObject> inheritedBy ) {
        this.inheritedBy = inheritedBy;
    }


    /**
     * @return the outerObject
     */
    // @JoinColumn ( updatable = false, nullable = true, name = "outer", referencedColumnName = "id", insertable = true
    // )
    @ManyToOne ( cascade = {}, fetch = FetchType.LAZY, targetEntity = AbstractConfigurationObject.class, optional = true )
    public ConfigurationObject getOuterObject () {
        return this.outerObject;
    }


    /**
     * @param outerObject
     *            the outerObject to set
     */
    public void setOuterObject ( ConfigurationObject outerObject ) {
        this.outerObject = outerObject;
    }


    /**
     * @return the subObjects
     */
    @OneToMany ( mappedBy = "outerObject", fetch = FetchType.LAZY, cascade = {
        CascadeType.REMOVE
    }, targetEntity = AbstractConfigurationObject.class )
    public Set<@NonNull ConfigurationObject> getSubObjects () {
        return this.subObjects;
    }


    /**
     * @param subObjects
     *            the subObjects to set
     */
    public void setSubObjects ( Set<@NonNull ConfigurationObject> subObjects ) {
        this.subObjects = subObjects;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        String dn = "anonymous"; //$NON-NLS-1$

        if ( this.localName != null ) {
            dn = this.localName;
        }
        else if ( this.displayName != null ) {
            dn = this.displayName;
        }

        return String.format("%s: %s (%s)", this.getType().getName(), dn, this.getId()); //$NON-NLS-1$
    }


    /**
     * @return the uses
     */
    @ManyToMany ( targetEntity = AbstractConfigurationObject.class, fetch = FetchType.LAZY, cascade = {} )
    public Set<@NonNull ConfigurationObject> getUses () {
        return this.uses;
    }


    /**
     * @param uses
     *            the uses to set
     */
    public void setUses ( Set<@NonNull ConfigurationObject> uses ) {
        this.uses = uses;
    }


    /**
     * @return the usedBy
     */
    @ManyToMany ( targetEntity = AbstractConfigurationObject.class, fetch = FetchType.LAZY, mappedBy = "uses", cascade = {} )
    public Set<@NonNull ConfigurationObject> getUsedBy () {
        return this.usedBy;
    }


    /**
     * @param usedBy
     *            the usedBy to set
     */
    public void setUsedBy ( Set<@NonNull ConfigurationObject> usedBy ) {
        this.usedBy = usedBy;
    }


    /**
     * @return the defaultFor
     */
    @ManyToMany ( targetEntity = AbstractStructuralObjectImpl.class, fetch = FetchType.LAZY, mappedBy = "defaultObjects", cascade = {} )
    public Set<@NonNull StructuralObject> getDefaultFor () {
        return this.defaultFor;
    }


    /**
     * @param defaultFor
     *            the defaultFor to set
     */
    public void setDefaultFor ( Set<@NonNull StructuralObject> defaultFor ) {
        this.defaultFor = defaultFor;
    }


    /**
     * @return the enforcedFor
     */
    @ManyToMany ( targetEntity = AbstractStructuralObjectImpl.class, fetch = FetchType.LAZY, mappedBy = "enforcedObjects", cascade = {} )
    public Set<@NonNull StructuralObject> getEnforcedFor () {
        return this.enforcedFor;
    }


    /**
     * @param enforcedFor
     *            the enforcedFor to set
     */
    public void setEnforcedFor ( Set<@NonNull StructuralObject> enforcedFor ) {
        this.enforcedFor = enforcedFor;
    }


    @Override
    public boolean equals ( Object o ) {
        if ( o instanceof ConfigurationObject && idEquals((ConfigurationObject) o) ) {
            return true;
        }
        return super.equals(o);
    }


    private boolean idEquals ( ConfigurationObject o ) {
        return this.getId() != null && this.getId().equals(o.getId());
    }


    @Override
    public int hashCode () {
        if ( this.getId() == null ) {
            return super.hashCode();
        }

        return this.getId().hashCode();
    }

}
