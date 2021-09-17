/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm;


import java.util.EnumSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.envers.Audited;

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * 
 */
@Entity
@Table ( name = "groups" )
@Inheritance ( strategy = InheritanceType.JOINED )
@PersistenceUnit ( unitName = "config" )
@MapAs ( GroupStructuralObject.class )
@Audited
@DiscriminatorValue ( "group" )
public class GroupStructuralObjectImpl extends AbstractStructuralObjectImpl implements GroupStructuralObject {

    /**
     * 
     */
    private static final long serialVersionUID = -951821130167814847L;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl#getType()
     */
    @Override
    @Transient
    public StructuralObjectType getType () {
        return StructuralObjectType.GROUP;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.StructuralObject#getAllowedParents()
     */
    @Override
    @Transient
    public Set<StructuralObjectType> getAllowedParents () {
        return EnumSet.of(StructuralObjectType.GROUP);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.StructuralObjectReference#getLocalType()
     */
    @Override
    @Transient
    public String getLocalType () {
        return null;
    }
}
