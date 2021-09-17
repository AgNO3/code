/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.storage;


import javax.persistence.Basic;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * @param <T>
 *
 */
@Entity
@Table ( name = "config_hostconfig_storage_mount" )
@Audited
@PersistenceUnit ( unitName = "config" )
@DiscriminatorValue ( "hc_st_mnt" )
@Inheritance ( strategy = InheritanceType.JOINED )
@MapAs ( MountEntry.class )
public abstract class MountEntryImpl <T extends MountEntry> extends AbstractConfigurationObject<T> implements MountEntry, MountEntryMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 5928093051123309242L;

    private String alias;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.hostconfig.storage.MountEntry#getAlias()
     */
    @Override
    @Basic
    public String getAlias () {
        return this.alias;
    }


    /**
     * @param alias
     *            the alias to set
     */
    @Override
    public void setAlias ( String alias ) {
        this.alias = alias;
    }


    /**
     * @param obj
     */
    public abstract void clone ( MountEntry obj );

}
