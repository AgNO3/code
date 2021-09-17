/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.07.2015 by mbechler
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
@MapAs ( FileshareStorageConfig.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_fileshare_storage" )
@Audited
@DiscriminatorValue ( "filesh_stor" )
public class FileshareStorageConfigImpl extends AbstractConfigurationObject<FileshareStorageConfig>
        implements FileshareStorageConfig, FileshareStorageConfigMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -8438784239781872084L;

    private String fileStorage;
    private String localStorage;
    private Set<FilesharePassthroughGroup> passthroughGroups = new HashSet<>();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<FileshareStorageConfig> getType () {
        return FileshareStorageConfig.class;
    }


    /**
     * @return the fileStorage
     */
    @Override
    public String getFileStorage () {
        return this.fileStorage;
    }


    /**
     * @param fileStorage
     *            the fileStorage to set
     */
    @Override
    public void setFileStorage ( String fileStorage ) {
        this.fileStorage = fileStorage;
    }


    /**
     * @return the localStorage
     */
    @Override
    public String getLocalStorage () {
        return this.localStorage;
    }


    /**
     * @param localStorage
     *            the localStorage to set
     */
    @Override
    public void setLocalStorage ( String localStorage ) {
        this.localStorage = localStorage;
    }


    /**
     * @return the passthroughGroups
     */
    @Override
    @ManyToMany ( fetch = FetchType.LAZY, targetEntity = AbstractFilesharePassthroughGroupImpl.class )
    public Set<FilesharePassthroughGroup> getPassthroughGroups () {
        return this.passthroughGroups;
    }


    /**
     * @param passthroughGroups
     *            the passthroughGroups to set
     */
    @Override
    public void setPassthroughGroups ( Set<FilesharePassthroughGroup> passthroughGroups ) {
        this.passthroughGroups = passthroughGroups;
    }
}
