/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.10.2014 by mbechler
 */
package eu.agno3.runtime.security.db;


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
@Table ( name = "roles_permissions", indexes = @Index ( columnList = "role_name,permission", unique = true ) )
public class BasePermission extends BaseAuthObject {

    /**
     * 
     */
    private static final long serialVersionUID = -8028371115518798300L;
    private String permission;
    private String rolename;


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


    /**
     * @return the permission
     */
    @Basic
    @NotNull
    @Column ( nullable = false, name = "permission", length = 100 )
    public String getPermission () {
        return this.permission;
    }


    /**
     * @param permission
     *            the permission to set
     */
    public void setPermission ( String permission ) {
        this.permission = permission;
    }
}
