/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.03.2015 by mbechler
 */
package eu.agno3.fileshare.model;


import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;

import org.joda.time.DateTime;


/**
 * @author mbechler
 *
 */
@PersistenceUnit ( unitName = "fileshare" )
@Entity
@Table ( name = "used_tokens" )
public class UsedTokenTracker {

    private UUID id;
    private DateTime expires;


    /**
     * @return the id
     */
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
     * @return the expires
     */
    public DateTime getExpires () {
        return this.expires;
    }


    /**
     * @param expires
     *            the expires to set
     */
    public void setExpires ( DateTime expires ) {
        this.expires = expires;
    }
}
