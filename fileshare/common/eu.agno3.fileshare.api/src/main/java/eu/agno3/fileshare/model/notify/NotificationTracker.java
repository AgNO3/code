/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.03.2015 by mbechler
 */
package eu.agno3.fileshare.model.notify;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;

import org.joda.time.DateTime;


/**
 * @author mbechler
 *
 */
@PersistenceUnit ( unitName = "fileshare" )
@Entity
@Inheritance ( strategy = InheritanceType.JOINED )
@Table ( name = "notification_tracker" )
public class NotificationTracker {

    private String dedupId;
    private DateTime expiration;


    /**
     * @return the dedupId
     */
    @Id
    public String getDedupId () {
        return this.dedupId;
    }


    /**
     * @param dedupId
     *            the dedupId to set
     */
    public void setDedupId ( String dedupId ) {
        this.dedupId = dedupId;
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

}
