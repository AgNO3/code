/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.07.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config.auth;


import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import eu.agno3.orchestrator.config.auth.RoleConfig;
import eu.agno3.orchestrator.config.auth.RoleConfigImpl;
import eu.agno3.orchestrator.config.auth.i18n.AuthenticationConfigurationMessages;
import eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean;
import eu.agno3.orchestrator.server.webgui.config.ConfigContext;


/**
 * @author mbechler
 *
 */
@Named ( "roleConfigBean" )
@ApplicationScoped
public class RoleConfigBean extends AbstractConfigObjectBean<RoleConfig, RoleConfigImpl> {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractBaseConfigObjectBean#getMessageBase()
     */
    @Override
    protected String getMessageBase () {
        return AuthenticationConfigurationMessages.BASE_PACKAGE;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractBaseConfigObjectBean#getObjectType()
     */
    @Override
    protected Class<RoleConfig> getObjectType () {
        return RoleConfig.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#getInstanceType()
     */
    @Override
    protected Class<RoleConfigImpl> getInstanceType () {
        return RoleConfigImpl.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractBaseConfigObjectBean#labelForInternal(eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    protected String labelForInternal ( RoleConfig obj ) {
        if ( obj == null ) {
            return null;
        }
        return obj.getRoleId();
    }


    /**
     * 
     * @return a comparator
     */
    public Comparator<RoleConfig> getComparator () {
        return new RoleConfigComparator();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractBaseConfigObjectBean#cloneInternal(eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    protected void cloneInternal ( ConfigContext<?, ?> ctx, RoleConfigImpl cloned, RoleConfig obj, RoleConfig def ) {
        cloned.setRoleId(obj.getRoleId());
        cloned.setHidden(obj.getHidden());

        if ( obj.getDescriptions() != null && !obj.getDescriptions().isEmpty() ) {
            cloned.setDescriptions(new HashMap<>(obj.getDescriptions()));
        }
        else if ( def.getDescriptions() != null ) {
            cloned.setDescriptions(new HashMap<>(def.getDescriptions()));
        }

        if ( obj.getPermissions() != null && !obj.getPermissions().isEmpty() ) {
            cloned.setPermissions(new HashSet<>(obj.getPermissions()));
        }
        else if ( def.getPermissions() != null ) {
            cloned.setPermissions(new HashSet<>(def.getPermissions()));
        }
        if ( obj.getTitles() != null && !obj.getTitles().isEmpty() ) {
            cloned.setTitles(new HashMap<>(obj.getTitles()));
        }
        else if ( def.getTitles() != null ) {
            cloned.setTitles(new HashMap<>(def.getTitles()));
        }
    }

}
