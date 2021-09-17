/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.session;


import java.io.Serializable;
import java.util.Set;

import org.joda.time.DateTime;

import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
public class SessionInfo implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -7050199134246043584L;
    private UserPrincipal userPrincipal;
    private Set<String> roles;
    private Set<String> permissions;
    private String sessionId;
    private long timeout;
    private DateTime openingTime;


    /**
     * @return the sessionId
     */
    public String getSessionId () {
        return this.sessionId;
    }


    /**
     * @param sessionId
     *            the sessionId to set
     */
    public void setSessionId ( String sessionId ) {
        this.sessionId = sessionId;
    }


    /**
     * @return the userPrincipal
     */
    public UserPrincipal getUserPrincipal () {
        return this.userPrincipal;
    }


    /**
     * @param userPrincipal
     *            the userPrincipal to set
     */
    public void setUserPrincipal ( UserPrincipal userPrincipal ) {
        this.userPrincipal = userPrincipal;
    }


    /**
     * @return the timeout
     */
    public long getTimeout () {
        return this.timeout;
    }


    /**
     * @param timeout
     *            the timeout to set
     */
    public void setTimeout ( long timeout ) {
        this.timeout = timeout;
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


    /**
     * @param dateTime
     */
    public void setOpeningTime ( DateTime dateTime ) {
        this.openingTime = dateTime;
    }


    /**
     * @return the openingTime
     */
    public DateTime getOpeningTime () {
        return this.openingTime;
    }
}
