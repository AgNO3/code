/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 14, 2016 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.license;


import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Version;

import org.joda.time.DateTime;

import eu.agno3.orchestrator.config.model.base.BaseObject;
import eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl;


/**
 * @author mbechler
 *
 */
@Entity
@Table ( name = "licenses" )
@PersistenceUnit ( unitName = "config" )
public class LicenseStorage implements Serializable, BaseObject {

    /**
     * 
     */
    private static final long serialVersionUID = -924576518516370561L;

    private UUID id;
    private Long version;

    private DateTime issued;
    private DateTime expiration;
    private Set<String> serviceTypes;
    private byte[] data;

    private AbstractStructuralObjectImpl anchor;
    private AbstractStructuralObjectImpl assignedTo;


    /**
     * 
     * @return license id
     */
    @Override
    @Id
    @Column ( length = 16 )
    public UUID getId () {
        return this.id;
    }


    /**
     * @param id
     *            the id to set
     */
    public void setId ( UUID id ) {
        this.id = id;
    }


    /**
     * 
     * @return oplock version
     */
    @Override
    @Version
    @Column ( nullable = false )
    public Long getVersion () {
        return this.version;
    }


    /**
     * @param version
     *            the version to set
     */
    public void setVersion ( Long version ) {
        this.version = version;
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
     * @return the assignedTo
     */
    @OneToOne ( fetch = FetchType.LAZY, cascade = {} )
    @JoinColumn ( name = "assignedTo" )
    public AbstractStructuralObjectImpl getAssignedTo () {
        return this.assignedTo;
    }


    /**
     * @param as
     *            the assignedTo to set
     */
    public void setAssignedTo ( AbstractStructuralObjectImpl as ) {
        this.assignedTo = as;
    }


    /**
     * @return the issued
     */
    public DateTime getIssued () {
        return this.issued;
    }


    /**
     * @param issued
     *            the issued to set
     */
    public void setIssued ( DateTime issued ) {
        this.issued = issued;
    }


    /**
     * @return the expiration
     */
    public DateTime getExpiration () {
        return this.expiration;
    }


    /**
     * @param expiration
     *            the expiration to set
     */
    public void setExpiration ( DateTime expiration ) {
        this.expiration = expiration;
    }


    /**
     * @return the serviceTypes
     */
    @ElementCollection
    @CollectionTable ( name = "licenses_serviceTypes" )
    public Set<String> getServiceTypes () {
        return this.serviceTypes;
    }


    /**
     * @param serviceTypes
     *            the serviceTypes to set
     */
    public void setServiceTypes ( Set<String> serviceTypes ) {
        this.serviceTypes = serviceTypes;
    }


    /**
     * @return the data
     */
    @Lob
    public byte[] getData () {
        return this.data;
    }


    /**
     * @param data
     *            the data to set
     */
    public void setData ( byte[] data ) {
        this.data = data;
    }
}
