/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.10.2015 by mbechler
 */
package eu.agno3.fileshare.model;


import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import eu.agno3.runtime.util.serialization.SafeSerialization;


/**
 * @author mbechler
 *
 */
@PersistenceUnit ( unitName = "fileshare" )
@Entity
@Table ( name = "content_entity_reference" )
@SafeSerialization
public class EntityReferenceStorage implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1697086441802901858L;
    private String relativePath;
    private UUID id;


    /**
     * 
     */
    public EntityReferenceStorage () {}


    /**
     * 
     * @param relativePath
     */
    public EntityReferenceStorage ( String relativePath ) {
        this.relativePath = relativePath;
    }


    /**
     * @return the id
     */
    @Id
    @GeneratedValue ( generator = "system-uuid" )
    @GenericGenerator ( name = "system-uuid", strategy = "uuid2" )
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
     * @return the entities relative path
     */
    @Lob
    @Column ( length = 4096 )
    public String getRelativePath () {
        return this.relativePath;
    }


    /**
     * @param path
     */
    public void setRelativePath ( String path ) {
        this.relativePath = path;
    }
}
