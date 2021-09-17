/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.06.2015 by mbechler
 */
package eu.agno3.runtime.security.event;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;

import eu.agno3.runtime.eventlog.Event;
import eu.agno3.runtime.eventlog.EventSeverity;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
public class LoginEvent implements Event, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -9092192650829205860L;

    private DateTime timestamp;
    private UserPrincipal userPrincipal;

    private String status;

    private Map<String, Serializable> properties = new HashMap<>();

    private String authType;
    private String loginRealmId;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.Event#getId()
     */
    @Override
    public String getId () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.Event#getDedupKey()
     */
    @Override
    public String getDedupKey () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.Event#getTimestamp()
     */
    @Override
    public DateTime getTimestamp () {
        return this.timestamp;
    }


    /**
     * @param timestamp
     */
    public void setTimestamp ( DateTime timestamp ) {
        this.timestamp = timestamp;
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
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.Event#getExpiration()
     */
    @Override
    public DateTime getExpiration () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.Event#getSeverity()
     */
    @Override
    public EventSeverity getSeverity () {
        return EventSeverity.AUDIT;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.Event#getType()
     */
    @Override
    public String getType () {
        return "login-event"; //$NON-NLS-1$
    }


    /**
     * @param status
     */
    public void setStatus ( String status ) {
        this.status = status;
    }


    /**
     * @return the status
     */
    public String getStatus () {
        return this.status;
    }


    /**
     * @return the authType
     */
    public String getAuthType () {
        return this.authType;
    }


    /**
     * @param name
     */
    public void setAuthType ( String name ) {
        this.authType = name;
    }


    /**
     * @return the loginRealmId
     */
    public String getLoginRealmId () {
        return this.loginRealmId;
    }


    /**
     * @param id
     */
    public void setLoginRealm ( String id ) {
        this.loginRealmId = id;
    }


    /**
     * @return the properties
     */
    public Map<String, Serializable> getProperties () {
        return this.properties;
    }


    /**
     * @param properties
     *            the properties to set
     */
    public void setProperties ( Map<String, Serializable> properties ) {
        this.properties = properties;
    }
}
