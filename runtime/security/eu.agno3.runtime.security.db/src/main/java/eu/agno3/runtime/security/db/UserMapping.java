/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.10.2014 by mbechler
 */
package eu.agno3.runtime.security.db;


import java.util.Calendar;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;


/**
 * @author mbechler
 *
 */
@Entity
@PersistenceUnit ( unitName = "auth" )
@Table ( name = "user_mapping", indexes = @Index ( columnList = "username, realm", unique = true ) )
public class UserMapping extends BaseAuthObject {

    /**
     * 
     */
    private static final long serialVersionUID = -8561615320635898860L;
    private UUID userId;
    private String userName;
    private String realName;
    private Calendar lastUsed;


    /**
     * @return the userId
     */
    @Column ( name = "userId", length = 16, nullable = false, updatable = false )
    public UUID getUserId () {
        return this.userId;
    }


    /**
     * @param userId
     *            the userId to set
     */
    public void setUserId ( UUID userId ) {
        this.userId = userId;
    }


    /**
     * @return the realName
     */
    @Column ( name = "realm", nullable = false )
    public String getRealName () {
        return this.realName;
    }


    /**
     * @param realName
     *            the realName to set
     */
    public void setRealName ( String realName ) {
        this.realName = realName;
    }


    /**
     * @return the userName
     */
    @Column ( name = "username", nullable = false )
    public String getUserName () {
        return this.userName;
    }


    /**
     * @param userName
     *            the userName to set
     */
    public void setUserName ( String userName ) {
        this.userName = userName;
    }


    /**
     * @return the lastUsed
     */
    @Column ( name = "last_used", nullable = true )
    public Calendar getLastUsed () {
        return this.lastUsed;
    }


    /**
     * @param lastUsed
     *            the lastUsed to set
     */
    public void setLastUsed ( Calendar lastUsed ) {
        this.lastUsed = lastUsed;
    }
}
