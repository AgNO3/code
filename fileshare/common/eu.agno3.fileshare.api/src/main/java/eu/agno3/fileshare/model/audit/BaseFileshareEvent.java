/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.06.2015 by mbechler
 */
package eu.agno3.fileshare.model.audit;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import eu.agno3.runtime.eventlog.Event;
import eu.agno3.runtime.eventlog.EventSeverity;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
@JsonInclude ( JsonInclude.Include.NON_NULL )
public abstract class BaseFileshareEvent implements Event, Serializable {

    /**
     * 
     */
    public static final String TYPE_LINK = "link"; //$NON-NLS-1$
    /**
     * 
     */
    public static final String TYPE_MAIL = "mail"; //$NON-NLS-1$
    /**
     * 
     */
    public static final String TYPE_SUBJECT = "subject"; //$NON-NLS-1$
    /**
     * 
     */
    public static final String TYPE = "type"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String PROPERTIES = "properties"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String PRINCIPAL = "principal"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String ACTION = "action"; //$NON-NLS-1$

    /**
     * 
     */
    private static final long serialVersionUID = -2328216221478952003L;
    private String id;
    private String dedupKey;
    private DateTime timestamp;
    private DateTime expiration;
    private EventSeverity severity;
    private String status;
    private String action;
    private String authType;
    private UserPrincipal principal;
    private String remoteAddr;
    private String proxiedVia;

    private Map<String, Object> properties = new HashMap<>();


    /**
     * 
     */
    public BaseFileshareEvent () {
        super();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.Event#getId()
     */
    @Override
    public String getId () {
        return this.id;
    }


    /**
     * @param id
     *            the id to set
     */
    public void setId ( String id ) {
        this.id = id;
    }


    /**
     * @param dedupKey
     *            the dedupKey to set
     */
    public void setDedupKey ( String dedupKey ) {
        this.dedupKey = dedupKey;
    }


    /**
     * @param expiration
     *            the expiration to set
     */
    public void setExpiration ( DateTime expiration ) {
        this.expiration = expiration;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.Event#getDedupKey()
     */
    @Override
    public String getDedupKey () {
        return this.dedupKey;
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
     * @param ts
     */
    public void setTimestamp ( DateTime ts ) {
        this.timestamp = ts;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.Event#getExpiration()
     */
    @Override
    public DateTime getExpiration () {
        return this.expiration;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.Event#getSeverity()
     */
    @Override
    public EventSeverity getSeverity () {
        return this.severity;
    }


    /**
     * @param severity
     */
    public void setSeverity ( EventSeverity severity ) {
        this.severity = severity;
    }


    /**
     * @param reason
     */
    public void setStatus ( String reason ) {
        this.status = reason;
    }


    /**
     * @return the status
     */
    public String getStatus () {
        return this.status;
    }


    /**
     * @param action
     */
    public void setAction ( String action ) {
        this.action = action;
    }


    /**
     * @return the action
     */
    public String getAction () {
        return this.action;
    }


    /**
     * @return the authType
     */
    public String getAuthType () {
        return this.authType;
    }


    /**
     * @param authType
     */
    public void setAuthType ( String authType ) {
        this.authType = authType;
    }


    /**
     * @return the principal
     */
    public UserPrincipal getPrincipal () {
        return this.principal;
    }


    /**
     * @param principal
     *            the principal to set
     */
    public void setPrincipal ( UserPrincipal principal ) {
        this.principal = principal;
    }


    /**
     * @param remoteAddr
     */
    public void setRemoteAddr ( String remoteAddr ) {
        this.remoteAddr = remoteAddr;
    }


    /**
     * @return the remoteAddr
     */
    public String getRemoteAddr () {
        return this.remoteAddr;
    }


    /**
     * @return the proxiedVia
     */
    public String getProxiedVia () {
        return this.proxiedVia;
    }


    /**
     * @param proxiedVia
     *            the proxiedVia to set
     */
    public void setProxiedVia ( String proxiedVia ) {
        this.proxiedVia = proxiedVia;
    }


    /**
     * @return the properties
     */
    public Map<String, Object> getProperties () {
        return this.properties;
    }


    /**
     * @param properties
     *            the properties to set
     */
    public void setProperties ( Map<String, Object> properties ) {
        this.properties = properties;
    }

}