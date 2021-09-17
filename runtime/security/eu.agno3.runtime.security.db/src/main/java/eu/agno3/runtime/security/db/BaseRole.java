/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.10.2014 by mbechler
 */
package eu.agno3.runtime.security.db;


import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;


/**
 * @author mbechler
 *
 */
@Entity
@PersistenceUnit ( unitName = "auth" )
@Table ( name = "user_roles", indexes = @Index ( columnList = "userId,role_name", unique = true ) )
public class BaseRole extends BaseAuthObject {

    /**
     * 
     */
    private static final long serialVersionUID = -961805028913827335L;
    private UUID userId;
    private String rolename;


    /**
     * @return the username
     */
    @Basic
    @NotNull
    @Column ( nullable = false, name = "userId", length = 16 )
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
     * @return the rolename
     */
    @Basic
    @NotNull
    @Column ( nullable = false, name = "role_name", length = 100 )
    public String getRoleName () {
        return this.rolename;
    }


    /**
     * @param rolename
     *            the rolename to set
     */
    public void setRoleName ( String rolename ) {
        this.rolename = rolename;
    }
}
