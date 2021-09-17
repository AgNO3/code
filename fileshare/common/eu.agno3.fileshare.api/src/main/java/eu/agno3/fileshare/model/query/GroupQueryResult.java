/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.01.2015 by mbechler
 */
package eu.agno3.fileshare.model.query;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.GroupInfo;
import eu.agno3.fileshare.model.NameSource;
import eu.agno3.fileshare.model.SubjectType;


/**
 * @author mbechler
 *
 */
public class GroupQueryResult extends SubjectQueryResult implements Serializable, GroupInfo {

    /**
     * 
     */
    private static final long serialVersionUID = -1100132617482338108L;

    private String name;
    private String realm;


    /**
     * @return the name
     */
    @Override
    public String getName () {
        return this.name;
    }


    /**
     * @param name
     *            the name to set
     */
    public void setName ( String name ) {
        this.name = name;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.GroupInfo#getRealm()
     */
    @Override
    public String getRealm () {
        return this.realm;
    }


    /**
     * @param realm
     *            the realm to set
     */
    public void setRealm ( String realm ) {
        this.realm = realm;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.SubjectInfo#getNameSource()
     */
    @Override
    public NameSource getNameSource () {
        return NameSource.GROUP_NAME;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format("group/%s/%s/%s/%s/%s", //$NON-NLS-1$
            this.getId(),
            this.getType() != null ? this.getType() : StringUtils.EMPTY,
            this.getTrustLevel(),
            this.name,
            this.realm != null ? this.realm : StringUtils.EMPTY);
    }


    /**
     * @param g
     * @return the group as a query result
     */
    public static GroupQueryResult fromGroup ( Group g ) {
        GroupQueryResult r = new GroupQueryResult();
        r.setId(g.getId());
        r.setType(g.getType());
        if ( r.getType() == null ) {
            r.setType(SubjectType.LOCAL);
        }
        r.setName(g.getName());
        r.setRealm(g.getRealm());
        return r;
    }


    /**
     * @param id
     * @param t
     * @param trustLevel
     * @param data
     * @return the query result
     */
    public static SubjectQueryResult fromString ( UUID id, SubjectType t, String trustLevel, String data ) {
        GroupQueryResult r = new GroupQueryResult();
        r.setId(id);
        r.setType(t);

        String[] parts = StringUtils.split(data, '/');
        r.setName(parts[ 0 ]);
        if ( parts.length > 1 ) {
            r.setRealm(parts[ 1 ]);
        }
        r.setTrustLevel(trustLevel);
        return r;
    }


    /**
     * @param list
     * @return wrapped list
     */
    public static List<GroupQueryResult> fromGroupList ( List<Group> list ) {
        List<GroupQueryResult> res = new ArrayList<>();
        for ( Group g : list ) {
            res.add(fromGroup(g));
        }
        return res;
    }
}
