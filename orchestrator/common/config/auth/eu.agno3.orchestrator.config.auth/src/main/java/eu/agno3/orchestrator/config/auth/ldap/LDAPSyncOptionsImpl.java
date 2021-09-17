/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth.ldap;


import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;
import org.joda.time.Duration;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( LDAPSyncOptions.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_auth_ldap_sync" )
@Audited
@DiscriminatorValue ( "auth_ldapsyn" )
public class LDAPSyncOptionsImpl extends AbstractConfigurationObject<LDAPSyncOptions> implements LDAPSyncOptions, LDAPSyncOptionsMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -387717594593898647L;

    private Boolean synchronizeRemovals;
    private Boolean removeUsingUUID;
    private Integer pageSize;

    private Duration syncInterval;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<LDAPSyncOptions> getType () {
        return LDAPSyncOptions.class;
    }


    /**
     * @return the synchronizeRemovals
     */
    @Override
    public Boolean getSynchronizeRemovals () {
        return this.synchronizeRemovals;
    }


    /**
     * @param synchronizeRemovals
     *            the synchronizeRemovals to set
     */
    @Override
    public void setSynchronizeRemovals ( Boolean synchronizeRemovals ) {
        this.synchronizeRemovals = synchronizeRemovals;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.auth.ldap.LDAPSyncOptions#getSyncInterval()
     */
    @Override
    public Duration getSyncInterval () {
        return this.syncInterval;
    }


    /**
     * @param syncInterval
     *            the syncInterval to set
     */
    @Override
    public void setSyncInterval ( Duration syncInterval ) {
        this.syncInterval = syncInterval;
    }


    /**
     * @return the removeUsingUUID
     */
    @Override
    public Boolean getRemoveUsingUUID () {
        return this.removeUsingUUID;
    }


    /**
     * @param removeUsingUUID
     *            the removeUsingUUID to set
     */
    @Override
    public void setRemoveUsingUUID ( Boolean removeUsingUUID ) {
        this.removeUsingUUID = removeUsingUUID;
    }


    /**
     * @return the pageSize
     */
    @Override
    public Integer getPageSize () {
        return this.pageSize;
    }


    /**
     * @param pageSize
     *            the pageSize to set
     */
    @Override
    public void setPageSize ( Integer pageSize ) {
        this.pageSize = pageSize;
    }


    /**
     * @param syncOptions
     * @return cloned entry
     */
    public static LDAPSyncOptionsImpl clone ( LDAPSyncOptions syncOptions ) {
        LDAPSyncOptionsImpl cloned = new LDAPSyncOptionsImpl();
        cloned.pageSize = syncOptions.getPageSize();
        cloned.removeUsingUUID = syncOptions.getRemoveUsingUUID();
        cloned.synchronizeRemovals = syncOptions.getSynchronizeRemovals();
        return cloned;
    }

}
