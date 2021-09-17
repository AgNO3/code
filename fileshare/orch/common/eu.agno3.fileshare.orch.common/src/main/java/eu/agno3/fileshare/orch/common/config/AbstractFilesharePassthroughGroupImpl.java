/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.02.2016 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;


/**
 * @author mbechler
 * @param <T>
 *
 */
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_fileshare_passthrough" )
@Audited
@DiscriminatorValue ( "filesh_passthr" )
@Inheritance ( strategy = InheritanceType.JOINED )
public abstract class AbstractFilesharePassthroughGroupImpl <T extends FilesharePassthroughGroup> extends AbstractConfigurationObject<T>
        implements FilesharePassthroughGroup, FilesharePassthroughGroupMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -2964930914349420520L;
    private String groupName;
    private String securityPolicy;
    private Boolean allowSharing;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.orch.common.config.FilesharePassthroughGroup#getGroupName()
     */
    @Override
    public String getGroupName () {
        return this.groupName;
    }


    /**
     * @param groupName
     *            the groupName to set
     */
    @Override
    public void setGroupName ( String groupName ) {
        this.groupName = groupName;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.orch.common.config.FilesharePassthroughGroup#getSecurityPolicy()
     */
    @Override
    public String getSecurityPolicy () {
        return this.securityPolicy;
    }


    /**
     * @param securityPolicy
     *            the securityPolicy to set
     */
    @Override
    public void setSecurityPolicy ( String securityPolicy ) {
        this.securityPolicy = securityPolicy;
    }


    /**
     * @return the allowSharing
     */
    @Override
    public Boolean getAllowSharing () {
        return this.allowSharing;
    }


    /**
     * @param allowSharing
     *            the allowSharing to set
     */
    @Override
    public void setAllowSharing ( Boolean allowSharing ) {
        this.allowSharing = allowSharing;
    }


    /**
     * @param obj
     */
    public void doClone ( FilesharePassthroughGroup obj ) {
        this.groupName = obj.getGroupName();
        this.securityPolicy = obj.getSecurityPolicy();
        this.allowSharing = obj.getAllowSharing();
    }
}
