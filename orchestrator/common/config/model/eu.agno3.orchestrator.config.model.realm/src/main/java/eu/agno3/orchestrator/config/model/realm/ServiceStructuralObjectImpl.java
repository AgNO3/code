/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm;


import java.util.EnumSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.joda.time.DateTime;

import eu.agno3.orchestrator.config.model.base.config.ConfigurationState;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * 
 */
@Entity
@Table ( name = "services", indexes = {
    @Index ( columnList = "serviceType" ), @Index ( columnList = "configuration" )
})
@Inheritance ( strategy = InheritanceType.JOINED )
@PersistenceUnit ( unitName = "config" )
@MapAs ( ServiceStructuralObject.class )
@Audited
@DiscriminatorValue ( "service" )
public class ServiceStructuralObjectImpl extends AbstractStructuralObjectImpl implements ServiceStructuralObject {

    /**
     * 
     */
    private static final long serialVersionUID = 2517671279804786699L;
    private String serviceType;
    private AbstractConfigurationInstance<?> configuration;

    @NonNull
    private ConfigurationState state = ConfigurationState.UNKNOWN;
    private Long appliedRevision;

    private DateTime lastApplied;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.StructuralObjectReference#getLocalType()
     */
    @Override
    @Transient
    public String getLocalType () {
        return getServiceType();
    }


    /**
     * @return the service type
     */
    @Override
    public String getServiceType () {
        return this.serviceType;
    }


    /**
     * @param serviceType
     *            the serviceType to set
     */
    public void setServiceType ( String serviceType ) {
        this.serviceType = serviceType;
    }


    /**
     * @return the lastApplyAttempt
     */
    @NotAudited
    public DateTime getLastApplied () {
        return this.lastApplied;
    }


    /**
     * @param lastApplyAttempt
     *            the lastApplyAttempt to set
     */
    public void setLastApplied ( DateTime lastApplyAttempt ) {
        this.lastApplied = lastApplyAttempt;
    }


    /**
     * @return the configuration
     */
    @Override
    @JoinColumn ( name = "configuration" )
    @OneToOne ( cascade = {
        CascadeType.ALL
    }, fetch = FetchType.LAZY, orphanRemoval = true, targetEntity = AbstractConfigurationInstance.class )
    public ConfigurationInstance getConfiguration () {
        return this.configuration;
    }


    /**
     * @param configuration
     *            the configuration to set
     */
    public void setConfiguration ( AbstractConfigurationInstance<?> configuration ) {
        this.configuration = configuration;
    }


    /**
     * @return the state
     */
    @Override
    @Enumerated ( EnumType.STRING )
    @NonNull
    @NotAudited
    public ConfigurationState getState () {
        return this.state;
    }


    /**
     * @param state
     *            the state to set
     */
    public void setState ( @NonNull ConfigurationState state ) {
        this.state = state;
    }


    /**
     * @return the appliedRevision
     */
    @Override
    @Column ( nullable = true )
    @Basic ( optional = true )
    @NotAudited
    public Long getAppliedRevision () {
        return this.appliedRevision;
    }


    /**
     * @param appliedRevision
     *            the appliedRevision to set
     */
    public void setAppliedRevision ( Long appliedRevision ) {
        this.appliedRevision = appliedRevision;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl#getType()
     */
    @Override
    @Transient
    public StructuralObjectType getType () {
        return StructuralObjectType.SERVICE;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.StructuralObject#getAllowedParents()
     */
    @Override
    @Transient
    public Set<StructuralObjectType> getAllowedParents () {
        return EnumSet.of(StructuralObjectType.INSTANCE);
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        String name = this.getDisplayName();
        if ( StringUtils.isBlank(name) ) {
            name = this.getServiceType();
        }
        return String.format("%s: %s (%s)", this.getType(), name, this.getId()); //$NON-NLS-1$
    }

}
