/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.security.api;


import java.util.HashSet;
import java.util.Set;


/**
 * @author mbechler
 *
 */
public class Permissions {

    private Set<String> permissions = new HashSet<>();


    /**
     * 
     */
    public Permissions () {}


    /**
     * @param permissions
     */
    public Permissions ( Set<String> permissions ) {
        this.permissions = permissions;
    }


    /**
     * @return the permissions
     */
    public Set<String> getPermissions () {
        return this.permissions;
    }


    /**
     * @param permissions
     *            the permissions to set
     */
    public void setPermissions ( Set<String> permissions ) {
        this.permissions = permissions;
    }

}
