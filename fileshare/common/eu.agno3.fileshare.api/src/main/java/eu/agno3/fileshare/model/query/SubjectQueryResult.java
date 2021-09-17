/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.01.2015 by mbechler
 */
package eu.agno3.fileshare.model.query;


import java.io.Serializable;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.SubjectInfo;
import eu.agno3.fileshare.model.SubjectType;
import eu.agno3.fileshare.model.User;
import eu.agno3.runtime.util.serialization.SafeSerialization;


/**
 * @author mbechler
 *
 */
@SafeSerialization
public abstract class SubjectQueryResult implements Serializable, SubjectInfo {

    /**
     * 
     */
    private static final long serialVersionUID = -1100132617482338108L;

    private UUID id;

    private SubjectType type;

    private String trustLevel;


    /**
     * @return the id
     */
    @Override
    public UUID getId () {
        return this.id;
    }


    /**
     * @param id
     *            the id to set
     */
    public void setId ( UUID id ) {
        this.id = id;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.SubjectInfo#getType()
     */
    @Override
    public SubjectType getType () {
        return this.type;
    }


    /**
     * @param type
     *            the type to set
     */
    public void setType ( SubjectType type ) {
        this.type = type;
    }


    /**
     * @return the trustLevel
     */
    public String getTrustLevel () {
        return this.trustLevel;
    }


    /**
     * @param trustLevel
     *            the trustLevel to set
     */
    public void setTrustLevel ( String trustLevel ) {
        this.trustLevel = trustLevel;
    }


    /**
     * @param subj
     * @return the group as a query result
     */
    public static SubjectQueryResult fromSubject ( Subject subj ) {
        if ( subj instanceof Group ) {
            return GroupQueryResult.fromGroup((Group) subj);
        }
        else if ( subj instanceof User ) {
            return UserQueryResult.fromUser((User) subj);
        }

        return null;
    }


    /**
     * @param val
     * @return the parsed result
     */
    public static SubjectQueryResult fromString ( String val ) {
        String[] parts = StringUtils.splitPreserveAllTokens(val, "/", 5); //$NON-NLS-1$

        if ( parts == null || parts.length < 4 ) {
            return null;
        }

        String type = parts[ 0 ];
        String trustLevel;
        UUID id = UUID.fromString(parts[ 1 ]);

        SubjectType t = SubjectType.LOCAL;
        String data;
        if ( parts.length == 5 && !StringUtils.isEmpty(parts[ 2 ]) ) {
            t = SubjectType.valueOf(parts[ 2 ]);

        }
        trustLevel = parts[ 3 ];
        data = parts[ 4 ];

        switch ( type ) {
        case "group": //$NON-NLS-1$
            return GroupQueryResult.fromString(id, t, trustLevel, data);
        case "user": //$NON-NLS-1$
            return UserQueryResult.fromString(id, t, trustLevel, data);

        default:
            return null;
        }

    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    // +GENERATED
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.id == null ) ? 0 : this.id.hashCode() );
        return result;
    }


    // -GENERATED

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    // +GENERATED
    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        SubjectQueryResult other = (SubjectQueryResult) obj;
        if ( this.id == null ) {
            if ( other.id != null )
                return false;
        }
        else if ( !this.id.equals(other.id) )
            return false;
        return true;
    }

    // -GENERATED
}
