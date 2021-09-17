/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.storage;


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
@Entity
@Table ( name = "config_hostconfig_storage" )
@Audited
@PersistenceUnit ( unitName = "config" )
@DiscriminatorValue ( "hc_st" )
@MapAs ( StorageConfiguration.class )
public class StorageConfigurationImpl extends AbstractConfigurationObject<StorageConfiguration>
        implements StorageConfiguration, StorageConfigurationMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 7842679973338613190L;
    private Set<MountEntry> mountEntries = new HashSet<>();
    private String backupStorage;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.ConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<StorageConfiguration> getType () {
        return StorageConfiguration.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.hostconfig.storage.StorageConfiguration#getMountEntries()
     */
    @Override
    @ManyToMany ( fetch = FetchType.LAZY, targetEntity = MountEntryImpl.class )
    public Set<MountEntry> getMountEntries () {
        return this.mountEntries;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.hostconfig.storage.StorageConfigurationMutable#setMountEntries(java.util.Set)
     */
    @Override
    public void setMountEntries ( Set<MountEntry> entries ) {
        this.mountEntries = entries;
    }


    /**
     * @return the backupStorage
     */
    @Override
    public String getBackupStorage () {
        return this.backupStorage;
    }


    /**
     * @param backupStorage
     *            the backupStorage to set
     */
    @Override
    public void setBackupStorage ( String backupStorage ) {
        this.backupStorage = backupStorage;
    }
}
