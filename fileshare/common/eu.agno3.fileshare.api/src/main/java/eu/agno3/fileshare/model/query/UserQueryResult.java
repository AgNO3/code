/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.01.2015 by mbechler
 */
package eu.agno3.fileshare.model.query;


import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.fileshare.model.NameSource;
import eu.agno3.fileshare.model.SubjectType;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.UserInfo;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
public class UserQueryResult extends SubjectQueryResult implements Serializable, UserInfo {

    /**
     * 
     */
    private static final long serialVersionUID = -1100132617482338108L;

    private static final String UTF8 = "UTF-8"; //$NON-NLS-1$

    private String userDisplayName;
    private UserPrincipal principal;
    private NameSource nameSource;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.UserInfo#getUserDisplayName()
     */
    @Override
    public String getUserDisplayName () {
        return this.userDisplayName;
    }


    /**
     * @param userDisplayName
     *            the userDisplayName to set
     */
    public void setUserDisplayName ( String userDisplayName ) {
        this.userDisplayName = userDisplayName;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.SubjectInfo#getRealm()
     */
    @Override
    public String getRealm () {
        return this.principal != null ? this.principal.getRealmName() : null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.SubjectInfo#getNameSource()
     */
    @Override
    public NameSource getNameSource () {
        return this.nameSource;
    }


    /**
     * @param nameSource
     *            the nameSource to set
     */
    public void setNameSource ( NameSource nameSource ) {
        this.nameSource = nameSource;
    }


    /**
     * @return the principal
     */
    @Override
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
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        try {
            return String.format(
                "user/%s/%s/%s/%s/%s#%s", //$NON-NLS-1$
                this.getId(),
                this.getType() != null ? this.getType() : StringUtils.EMPTY,
                this.getTrustLevel(),
                this.getPrincipal(),
                this.getNameSource(),
                URLEncoder.encode(this.getUserDisplayName(), UTF8));
        }
        catch ( UnsupportedEncodingException e ) {
            throw new IllegalStateException(e);
        }
    }


    /**
     * @param u
     * @return the group as a query result
     */
    public static UserQueryResult fromUser ( User u ) {
        UserQueryResult r = new UserQueryResult();
        r.setId(u.getId());
        r.setType(u.getType());
        if ( r.getType() == null ) {
            r.setType(SubjectType.LOCAL);
        }
        r.setPrincipal(u.getPrincipal());
        r.setUserDisplayName(u.getUserDisplayName());
        r.setNameSource(u.getNameSource());
        return r;
    }


    /**
     * @param id
     * @param type
     * @param trustLevel
     * @param data
     * @return the query result
     */
    public static SubjectQueryResult fromString ( UUID id, SubjectType type, String trustLevel, String data ) {
        UserQueryResult r = new UserQueryResult();
        r.setId(id);
        r.setTrustLevel(trustLevel);
        String[] userParts = StringUtils.split(data, "#", 2); //$NON-NLS-1$
        if ( userParts == null || userParts.length != 2 ) {
            return null;
        }

        String[] userSubParts = StringUtils.split(userParts[ 0 ], "/", 2); //$NON-NLS-1$
        if ( userSubParts == null || userSubParts.length != 2 ) {
            return null;
        }

        NameSource nameSource = NameSource.valueOf(userSubParts[ 1 ]);
        r.setNameSource(nameSource);

        String[] princParts = StringUtils.split(userSubParts[ 0 ], "@", 2); //$NON-NLS-1$
        if ( princParts == null || princParts.length != 2 ) {
            return null;
        }
        r.setPrincipal(new UserPrincipal(princParts[ 1 ], id, princParts[ 0 ]));

        try {
            r.setUserDisplayName(URLDecoder.decode(userParts[ 1 ], UTF8));
        }
        catch ( UnsupportedEncodingException e ) {
            throw new IllegalStateException(e);
        }

        return r;
    }
}
