/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2015 by mbechler
 */
package eu.agno3.fileshare.model;


import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.runtime.validation.email.ValidEmail;


/**
 * @author mbechler
 *
 */
@PersistenceUnit ( unitName = "fileshare" )
@Entity
@Inheritance ( strategy = InheritanceType.JOINED )
@Table ( name = "groups", indexes = {
    @Index ( columnList = "realm,name", unique = true ), @Index ( columnList = "name" )
} )
public class Group extends Subject implements GroupInfo {

    /**
     * 
     */
    private static final long serialVersionUID = 6523493734841582443L;
    private Set<Subject> members = new HashSet<>();
    private String name;
    private String realm;
    private String notificationOverrideAddress;
    private boolean disableNotifications;
    private Locale groupLocale;


    /**
     * 
     */
    public Group () {}


    /**
     * 
     * @param g
     * @param refs
     */
    public Group ( Group g, boolean refs ) {
        super(g, refs);
        this.name = g.name;
        this.notificationOverrideAddress = g.notificationOverrideAddress;
        this.disableNotifications = g.disableNotifications;
        this.groupLocale = g.groupLocale;
        this.realm = g.realm;
        if ( refs && g.getSubjectRoot() != null ) {
            this.setSubjectRoot(g.getSubjectRoot().cloneShallow(false));
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.Subject#cloneShallow(boolean)
     */
    @Override
    public Group cloneShallow ( boolean refs ) {
        return new Group(this, refs);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.SubjectInfo#getNameSource()
     */
    @Override
    @Transient
    public NameSource getNameSource () {
        return NameSource.GROUP_NAME;
    }


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
     * @return the realm
     */
    @Override
    @Column ( nullable = true )
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
     * @return the members
     */
    @ManyToMany ( fetch = FetchType.LAZY )
    @JoinTable ( name = "group_members" )
    public Set<Subject> getMembers () {
        return this.members;
    }


    /**
     * @param members
     *            the members to set
     */
    public void setMembers ( Set<Subject> members ) {
        this.members = members;
    }


    /**
     * @return the disableNotifications
     */
    @Basic
    public boolean getDisableNotifications () {
        return this.disableNotifications;
    }


    /**
     * @param disableNotifications
     *            the disableNotifications to set
     */
    public void setDisableNotifications ( boolean disableNotifications ) {
        this.disableNotifications = disableNotifications;
    }


    /**
     * 
     * @return the group locale
     */
    @Column ( length = 20 )
    public Locale getGroupLocale () {
        return this.groupLocale;
    }


    /**
     * @param groupLocale
     *            the groupLocale to set
     */
    public void setGroupLocale ( Locale groupLocale ) {
        this.groupLocale = groupLocale;
    }


    /**
     * @return the notificationOverrideAddress
     */
    @Basic
    @Column ( nullable = true )
    @ValidEmail
    public String getNotificationOverrideAddress () {
        return this.notificationOverrideAddress;
    }


    /**
     * @param notificationOverrideAddress
     *            the notificationOverrideAddress to set
     */
    public void setNotificationOverrideAddress ( String notificationOverrideAddress ) {
        this.notificationOverrideAddress = notificationOverrideAddress;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        String realmStr = this.realm != null ? "@" + this.realm : StringUtils.EMPTY; //$NON-NLS-1$
        return "Group: " + getName() + realmStr; //$NON-NLS-1$
    }

}
