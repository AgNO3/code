/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.resourcelibrary;


import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.OptimisticLock;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.joda.time.DateTime;

import eu.agno3.orchestrator.config.model.realm.AbstractObject;
import eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl;


/**
 * @author mbechler
 *
 */
@Entity
@Table ( name = "resource_libraries", uniqueConstraints = {
    @UniqueConstraint ( columnNames = {
        "anchor", "name", "type"
        } )
})
@Inheritance ( strategy = InheritanceType.JOINED )
@PersistenceUnit ( unitName = "config" )
@Audited
public class ResourceLibrary extends AbstractObject {

    /**
     * 
     */
    private static final long serialVersionUID = 7761891128435144784L;

    private String name;
    private String type;
    private boolean builtin;
    private ResourceLibrary parent;
    private Set<ResourceLibrary> children;

    private AbstractStructuralObjectImpl anchor;

    private DateTime lastModified;


    /**
     * @return the name
     */
    public String getName () {
        return this.name;
    }


    /**
     * @param name
     *            the name to set
     */
    public void setName ( String name ) {
        this.name = name;
    }


    /**
     * @return the lastModified
     */
    @NotAudited
    @OptimisticLock ( excluded = true )
    public DateTime getLastModified () {
        return this.lastModified;
    }


    /**
     * @param lastModified
     */
    public void setLastModified ( DateTime lastModified ) {
        this.lastModified = lastModified;
    }


    /**
     * @return the type
     */
    public String getType () {
        return this.type;
    }


    /**
     * @param type
     *            the type to set
     */
    public void setType ( String type ) {
        this.type = type;
    }


    /**
     * @return the builtin
     */
    public boolean getBuiltin () {
        return this.builtin;
    }


    /**
     * @param builtin
     *            the builtin to set
     */
    public void setBuiltin ( boolean builtin ) {
        this.builtin = builtin;
    }


    /**
     * 
     * @return the object that this configuration is attached to
     */
    @ManyToOne ( fetch = FetchType.LAZY, cascade = {} )
    @JoinColumn ( name = "anchor" )
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
     * @return the parent
     */
    @ManyToOne ( fetch = FetchType.LAZY, cascade = {} )
    public ResourceLibrary getParent () {
        return this.parent;
    }


    /**
     * @param parent
     *            the parent to set
     */
    public void setParent ( ResourceLibrary parent ) {
        this.parent = parent;
    }


    /**
     * @return the children
     */
    @OneToMany ( mappedBy = "parent" )
    public Set<ResourceLibrary> getChildren () {
        return this.children;
    }


    /**
     * @param children
     *            the children to set
     */
    public void setChildren ( Set<ResourceLibrary> children ) {
        this.children = children;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format("TrustLibrary %s @ %s", this.name, this.anchor); //$NON-NLS-1$
    }
}
