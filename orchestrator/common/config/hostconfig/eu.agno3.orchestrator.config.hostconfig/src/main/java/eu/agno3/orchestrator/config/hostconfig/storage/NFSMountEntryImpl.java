/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.storage;


import java.net.URI;

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
@Table ( name = "config_hostconfig_storage_mount_nfs" )
@Audited
@PersistenceUnit ( unitName = "config" )
@DiscriminatorValue ( "hc_st_mnt_nfs" )
@MapAs ( NFSMountEntry.class )
public class NFSMountEntryImpl extends MountEntryImpl<NFSMountEntry> implements NFSMountEntry, NFSMountEntryMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 7819650251387327504L;

    private URI target;

    private NFSVersion nfsVersion;

    private NFSSecurityType securityType;

    private String authRealm;
    private String authKeytab;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<NFSMountEntry> getType () {
        return NFSMountEntry.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.hostconfig.storage.MountEntry#getMountType()
     */
    @Override
    public MountType getMountType () {
        return MountType.NFS;
    }


    /**
     * 
     * @param t
     */
    public void setMountType ( MountType t ) {
        // ignore
    }


    /**
     * @return the target
     */
    @Override
    public URI getTarget () {
        return this.target;
    }


    /**
     * @param target
     *            the target to set
     */
    @Override
    public void setTarget ( URI target ) {
        this.target = target;
    }


    /**
     * @return the nfsVersion
     */
    @Override
    public NFSVersion getNfsVersion () {
        return this.nfsVersion;
    }


    /**
     * @param nfsVersion
     *            the nfsVersion to set
     */
    @Override
    public void setNfsVersion ( NFSVersion nfsVersion ) {
        this.nfsVersion = nfsVersion;
    }


    /**
     * @return the securityType
     */
    @Override
    public NFSSecurityType getSecurityType () {
        return this.securityType;
    }


    /**
     * @param securityType
     *            the securityType to set
     */
    @Override
    public void setSecurityType ( NFSSecurityType securityType ) {
        this.securityType = securityType;
    }


    /**
     * @return the authRealm
     */
    @Override
    public String getAuthRealm () {
        return this.authRealm;
    }


    /**
     * @param authRealm
     *            the authRealm to set
     */
    @Override
    public void setAuthRealm ( String authRealm ) {
        this.authRealm = authRealm;
    }


    /**
     * @return the authKeytab
     */
    @Override
    public String getAuthKeytab () {
        return this.authKeytab;
    }


    /**
     * @param authKeytab
     *            the authKeytab to set
     */
    @Override
    public void setAuthKeytab ( String authKeytab ) {
        this.authKeytab = authKeytab;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.hostconfig.storage.MountEntryImpl#clone(eu.agno3.orchestrator.config.hostconfig.storage.MountEntry)
     */
    @Override
    public void clone ( MountEntry obj ) {

        if ( ! ( obj instanceof NFSMountEntry ) ) {
            throw new IllegalArgumentException();
        }

        NFSMountEntry o = (NFSMountEntry) obj;
        this.target = o.getTarget();
        this.nfsVersion = o.getNfsVersion();
        this.securityType = o.getSecurityType();
        this.authKeytab = o.getAuthKeytab();
        this.authRealm = o.getAuthRealm();
    }
}
