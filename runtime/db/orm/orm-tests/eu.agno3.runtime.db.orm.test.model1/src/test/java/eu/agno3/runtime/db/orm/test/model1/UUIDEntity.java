/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.10.2016 by mbechler
 */
package eu.agno3.runtime.db.orm.test.model1;


import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PersistenceUnit;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;


/**
 * @author mbechler
 *
 */
@Entity
@PersistenceUnit ( unitName = "test" )
@SuppressWarnings ( "javadoc" )
public class UUIDEntity {

    private UUID id;
    private Long version;

    private String val;


    @Id
    @GeneratedValue ( generator = "system-uuid" )
    @GenericGenerator ( name = "system-uuid", strategy = "uuid2-us" )
    @Column ( length = 16 )
    public UUID getId () {
        return this.id;
    }


    public void setId ( UUID id ) {
        this.id = id;
    }


    /**
     * @return the version
     */
    @Version
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


    public String getVal () {
        return this.val;
    }


    public void setVal ( String val ) {
        this.val = val;
    }
}
