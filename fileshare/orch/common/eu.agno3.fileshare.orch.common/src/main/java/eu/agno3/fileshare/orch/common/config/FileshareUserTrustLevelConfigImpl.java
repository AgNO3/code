/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.06.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
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
@MapAs ( FileshareUserTrustLevelConfig.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_fileshare_user_trustLevels" )
@Audited
@DiscriminatorValue ( "filesh_user_tls" )
public class FileshareUserTrustLevelConfigImpl extends AbstractConfigurationObject<FileshareUserTrustLevelConfig> implements
        FileshareUserTrustLevelConfig, FileshareUserTrustLevelConfigMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 2944185256926060188L;

    private String groupTrustLevel;
    private String mailTrustLevel;
    private String linkTrustLevel;

    private Set<FileshareUserTrustLevel> trustLevels = new HashSet<>();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<FileshareUserTrustLevelConfig> getType () {
        return FileshareUserTrustLevelConfig.class;
    }


    /**
     * @return the trustLevels
     */
    @Override
    @ManyToMany ( fetch = FetchType.LAZY, targetEntity = FileshareUserTrustLevelImpl.class )
    public Set<FileshareUserTrustLevel> getTrustLevels () {
        return this.trustLevels;
    }


    /**
     * @param trustLevels
     *            the trustLevels to set
     */
    @Override
    public void setTrustLevels ( Set<FileshareUserTrustLevel> trustLevels ) {
        this.trustLevels = trustLevels;
    }


    /**
     * @return the groupTrustLevel
     */
    @Override
    public String getGroupTrustLevel () {
        return this.groupTrustLevel;
    }


    /**
     * @param groupTrustLevel
     *            the groupTrustLevel to set
     */
    @Override
    public void setGroupTrustLevel ( String groupTrustLevel ) {
        this.groupTrustLevel = groupTrustLevel;
    }


    /**
     * @return the mailTrustLevel
     */
    @Override
    public String getMailTrustLevel () {
        return this.mailTrustLevel;
    }


    /**
     * @param mailTrustLevel
     *            the mailTrustLevel to set
     */
    @Override
    public void setMailTrustLevel ( String mailTrustLevel ) {
        this.mailTrustLevel = mailTrustLevel;
    }


    /**
     * @return the linkTrustLevel
     */
    @Override
    public String getLinkTrustLevel () {
        return this.linkTrustLevel;
    }


    /**
     * @param linkTrustLevel
     *            the linkTrustLevel to set
     */
    @Override
    public void setLinkTrustLevel ( String linkTrustLevel ) {
        this.linkTrustLevel = linkTrustLevel;
    }
}
