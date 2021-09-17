/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.storage;


import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@Entity
@Table ( name = "config_hostconfig_storage_mount_local" )
@Audited
@PersistenceUnit ( unitName = "config" )
@DiscriminatorValue ( "hc_st_mnt_loc" )
@MapAs ( MountEntry.class )
public class LocalMountEntryImpl extends MountEntryImpl<LocalMountEntry> implements LocalMountEntry, LocalMountEntryMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 1790718730978781679L;

    private UUID matchUuid;
    private String matchLabel;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<LocalMountEntry> getType () {
        return LocalMountEntry.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.hostconfig.storage.MountEntryImpl#getMountType()
     */
    @Override
    @Transient
    public MountType getMountType () {
        return MountType.LOCAL;
    }


    /**
     * @param t
     */
    public void setMountType ( MountType t ) {
        // ignore
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.hostconfig.storage.LocalMountEntry#getMatchUuid()
     */
    @Override
    @Basic
    @Column ( length = 16 )
    public UUID getMatchUuid () {
        return this.matchUuid;
    }


    /**
     * @param matchUuid
     *            the matchUuid to set
     */
    @Override
    public void setMatchUuid ( UUID matchUuid ) {
        this.matchUuid = matchUuid;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.hostconfig.storage.LocalMountEntry#getMatchLabel()
     */
    @Override
    @Basic
    public String getMatchLabel () {
        return this.matchLabel;
    }


    /**
     * @param matchLabel
     *            the matchLabel to set
     */
    @Override
    public void setMatchLabel ( String matchLabel ) {
        this.matchLabel = matchLabel;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.hostconfig.storage.MountEntryImpl#clone(eu.agno3.orchestrator.config.hostconfig.storage.MountEntry)
     */
    @Override
    public void clone ( MountEntry obj ) {

        if ( ! ( obj instanceof LocalMountEntry ) ) {
            throw new IllegalArgumentException();
        }

        LocalMountEntry o = (LocalMountEntry) obj;
        this.matchLabel = o.getMatchLabel();
        this.matchUuid = o.getMatchUuid();
    }
}
