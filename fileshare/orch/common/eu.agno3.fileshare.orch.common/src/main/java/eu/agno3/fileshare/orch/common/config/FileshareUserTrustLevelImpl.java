/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.07.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.MapKeyColumn;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( FileshareUserTrustLevel.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_fileshare_user_trustLevel" )
@Audited
@DiscriminatorValue ( "filesh_user_tl" )
public class FileshareUserTrustLevelImpl extends AbstractConfigurationObject<FileshareUserTrustLevel> implements FileshareUserTrustLevel,
        FileshareUserTrustLevelMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -5124616459776871141L;

    private String trustLevelId;
    private String title;
    private String color;
    private Map<Locale, String> messages = new HashMap<>();
    private Set<String> matchRoles = new HashSet<>();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<FileshareUserTrustLevel> getType () {
        return FileshareUserTrustLevel.class;
    }


    /**
     * @return the trustLevelId
     */
    @Override
    public String getTrustLevelId () {
        return this.trustLevelId;
    }


    /**
     * @param trustLevelId
     *            the trustLevelId to set
     */
    @Override
    public void setTrustLevelId ( String trustLevelId ) {
        this.trustLevelId = trustLevelId;
    }


    /**
     * @return the title
     */
    @Override
    public String getTitle () {
        return this.title;
    }


    /**
     * @param title
     *            the title to set
     */
    @Override
    public void setTitle ( String title ) {
        this.title = title;
    }


    /**
     * @return the color
     */
    @Override
    public String getColor () {
        return this.color;
    }


    /**
     * @param color
     *            the color to set
     */
    @Override
    public void setColor ( String color ) {
        this.color = color;
    }


    /**
     * @return the matchRoles
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_fileshare_user_trustLevel_roles" )
    public Set<String> getMatchRoles () {
        return this.matchRoles;
    }


    /**
     * @param matchRoles
     *            the matchRoles to set
     */
    @Override
    public void setMatchRoles ( Set<String> matchRoles ) {
        this.matchRoles = matchRoles;
    }


    /**
     * @return the messages
     */
    @Override
    @ElementCollection
    @MapKeyColumn ( name = "locale" )
    @Column ( name = "msg" )
    @CollectionTable ( name = "config_fileshare_user_trustLevel_msgs" )
    public Map<Locale, String> getMessages () {
        return this.messages;
    }


    /**
     * @param messages
     *            the messages to set
     */
    @Override
    public void setMessages ( Map<Locale, String> messages ) {
        this.messages = messages;
    }

}
