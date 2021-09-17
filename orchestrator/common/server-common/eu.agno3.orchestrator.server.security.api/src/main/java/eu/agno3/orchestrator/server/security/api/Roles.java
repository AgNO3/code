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
public class Roles {

    private Set<String> roles = new HashSet<>();


    /**
     * 
     */
    public Roles () {}


    /**
     * @param roles
     */
    public Roles ( Set<String> roles ) {
        this.roles = roles;
    }


    /**
     * @return the roles
     */
    public Set<String> getRoles () {
        return this.roles;
    }


    /**
     * @param roles
     *            the roles to set
     */
    public void setRoles ( Set<String> roles ) {
        this.roles = roles;
    }
}
