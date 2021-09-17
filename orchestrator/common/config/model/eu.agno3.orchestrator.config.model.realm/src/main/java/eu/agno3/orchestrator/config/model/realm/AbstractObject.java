/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm;


import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PersistenceUnit;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.GenericGenerator;

import eu.agno3.orchestrator.config.model.base.BaseObject;


/**
 * Base class for persistent objects
 * 
 * @author mbechler
 * 
 */
@MappedSuperclass
@PersistenceUnit ( unitName = "config" )
@XmlTransient
public class AbstractObject implements BaseObject {

    /**
     * 
     */
    private static final long serialVersionUID = -6728178369120711753L;

    private UUID id;
    private Long version;


    /**
     * 
     */
    public AbstractObject () {
        super();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.base.BaseObject#getId()
     */
    @Override
    @Id
    @GeneratedValue ( generator = "system-uuid" )
    @GenericGenerator ( name = "system-uuid", strategy = "uuid2-us" )
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
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.base.BaseObject#getVersion()
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

}