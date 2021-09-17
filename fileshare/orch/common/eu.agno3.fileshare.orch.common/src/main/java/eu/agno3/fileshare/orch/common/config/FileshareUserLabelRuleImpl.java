/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.07.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
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
@MapAs ( FileshareUserLabelRule.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_fileshare_user_labelrule" )
@Audited
@DiscriminatorValue ( "filesh_userlr" )
public class FileshareUserLabelRuleImpl extends AbstractConfigurationObject<FileshareUserLabelRule> implements FileshareUserLabelRule,
        FileshareUserLabelRuleMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 7283654741957339933L;

    private String matchRole;
    private String assignLabel;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<FileshareUserLabelRule> getType () {
        return FileshareUserLabelRule.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.orch.common.config.FileshareUserLabelRule#getMatchRole()
     */
    @Override
    public String getMatchRole () {
        return this.matchRole;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.orch.common.config.FileshareUserLabelRuleMutable#setMatchRole(java.lang.String)
     */
    @Override
    public void setMatchRole ( String role ) {
        this.matchRole = role;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.orch.common.config.FileshareUserLabelRule#getAssignLabel()
     */
    @Override
    public String getAssignLabel () {
        return this.assignLabel;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.orch.common.config.FileshareUserLabelRuleMutable#setAssignLabel(java.lang.String)
     */
    @Override
    public void setAssignLabel ( String label ) {
        this.assignLabel = label;
    }

}
