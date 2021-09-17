/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm;


import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.joda.time.DateTime;

import eu.agno3.orchestrator.config.model.realm.license.LicenseStorage;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * 
 */
@Entity
@Table ( name = "instances" )
@Inheritance ( strategy = InheritanceType.JOINED )
@PersistenceUnit ( unitName = "config" )
@MapAs ( InstanceStructuralObject.class )
@Audited
@DiscriminatorValue ( "instance" )
public class InstanceStructuralObjectImpl extends AbstractStructuralObjectImpl implements InstanceStructuralObject {

    /**
     * 
     */
    private static final long serialVersionUID = -7390884989565659155L;
    private UUID agentId;

    private String imageType;
    private String releaseStream;
    private LicenseStorage assignedLicense;
    private DateTime demoExpiration;
    private Integer entropy;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl#getType()
     */
    @Override
    @Transient
    public StructuralObjectType getType () {
        return StructuralObjectType.INSTANCE;
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
        return getImageType();
    }


    /**
     * @return the imageType
     */
    @Override
    public String getImageType () {
        return this.imageType;
    }


    /**
     * @param imageType
     *            the imageType to set
     */
    public void setImageType ( String imageType ) {
        this.imageType = imageType;
    }


    /**
     * @return the releaseTrain
     */
    @Override
    public String getReleaseStream () {
        return this.releaseStream;
    }


    /**
     * @param releaseStream
     *            the releaseStream to set
     */
    public void setReleaseStream ( String releaseStream ) {
        this.releaseStream = releaseStream;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject#getAgentId()
     */
    @Override
    @Column ( name = "agentId", nullable = true, length = 16 )
    public UUID getAgentId () {
        return this.agentId;
    }


    /**
     * @param agentId
     *            the agentId to set
     */
    public void setAgentId ( UUID agentId ) {
        this.agentId = agentId;
    }


    @Override
    @OneToOne ( mappedBy = "assignedTo", fetch = FetchType.LAZY, cascade = {} )
    @NotAudited
    public LicenseStorage getAssignedLicense () {
        return this.assignedLicense;
    }


    /**
     * @param assignedLicense
     *            the assignedLicense to set
     */
    public void setAssignedLicense ( LicenseStorage assignedLicense ) {
        this.assignedLicense = assignedLicense;
    }


    /**
     * @return the demoExpiration
     */
    @Override
    public DateTime getDemoExpiration () {
        return this.demoExpiration;
    }


    /**
     * @param demoExpiration
     *            the demoExpiration to set
     */
    public void setDemoExpiration ( DateTime demoExpiration ) {
        this.demoExpiration = demoExpiration;
    }


    /**
     * @return the entropy
     */
    public Integer getBootstrapPasswordEntropy () {
        return this.entropy;
    }


    /**
     * @param entropy
     */
    public void setBootstrapPasswordEntropy ( Integer entropy ) {
        this.entropy = entropy;
    }
}
