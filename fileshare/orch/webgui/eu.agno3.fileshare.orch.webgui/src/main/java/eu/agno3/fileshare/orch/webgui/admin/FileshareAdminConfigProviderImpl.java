/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.09.2015 by mbechler
 */
package eu.agno3.fileshare.orch.webgui.admin;


import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import eu.agno3.fileshare.orch.common.config.FileshareConfiguration;
import eu.agno3.fileshare.orch.common.config.FileshareConfigurationObjectTypeDescriptor;
import eu.agno3.fileshare.orch.common.config.FileshareSecurityPolicy;
import eu.agno3.fileshare.orch.common.config.desc.FileshareServiceTypeDescriptor;
import eu.agno3.fileshare.webgui.admin.FileshareAdminConfigProvider;
import eu.agno3.orchestrator.config.model.base.AbstractModelException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.config.EffectiveConfigCacheBean;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
public class FileshareAdminConfigProviderImpl implements FileshareAdminConfigProvider {

    private static final Logger log = Logger.getLogger(FileshareAdminConfigProviderImpl.class);

    @Inject
    private StructureViewContextBean context;

    @Inject
    private EffectiveConfigCacheBean cacheBean;


    /**
     * @return the fileshare configuration effective at the current structure object
     */
    public FileshareConfiguration getEffectiveFileshareConfiguration () {
        if ( !this.context.isInstanceSelected() && !this.context.isServiceSelected() ) {
            return null;
        }

        try {
            StructuralObject selectedObject = this.context.getSelectedObject();

            if ( log.isDebugEnabled() ) {
                log.debug("Loading fileshare configuration for " + selectedObject); //$NON-NLS-1$
            }
            ConfigurationObject effectiveConfig = this.cacheBean.getEffectiveSingletonConfig(
                selectedObject,
                FileshareServiceTypeDescriptor.FILESHARE_SERVICE_TYPE,
                FileshareConfigurationObjectTypeDescriptor.TYPE_NAME);

            if ( effectiveConfig == null ) {
                log.debug("Config not found"); //$NON-NLS-1$
                return null;
            }

            if ( !FileshareConfiguration.class.isAssignableFrom(effectiveConfig.getType()) ) {
                log.error("Return configuration is not compatible " + effectiveConfig.getType()); //$NON-NLS-1$
                return null;
            }

            return (FileshareConfiguration) effectiveConfig;
        }
        catch (
            AbstractModelException |
            UndeclaredThrowableException |
            GuiWebServiceException e ) {
            ExceptionHandler.handle(e);
            return null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.admin.FileshareAdminConfigProvider#getDefinedSecurityLabels()
     */
    @Override
    public List<String> getDefinedSecurityLabels () {
        List<String> labels = new ArrayList<>();
        FileshareConfiguration config = getEffectiveFileshareConfiguration();
        if ( config == null ) {
            return Collections.EMPTY_LIST;
        }
        for ( FileshareSecurityPolicy pol : config.getSecurityPolicyConfiguration().getPolicies() ) {
            labels.add(pol.getLabel());
        }

        // TODO: may need to sort

        return labels;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.admin.FileshareAdminConfigProvider#getGlobalDefaultQuota()
     */
    @Override
    public Long getGlobalDefaultQuota () {
        FileshareConfiguration config = getEffectiveFileshareConfiguration();
        if ( config == null ) {
            return null;
        }
        return config.getUserConfiguration().getQuotaConfig().getGlobalDefaultQuota();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.admin.FileshareAdminConfigProvider#getUserDefaultRoles()
     */
    @Override
    public Set<String> getUserDefaultRoles () {
        FileshareConfiguration config = getEffectiveFileshareConfiguration();
        if ( config == null ) {
            return Collections.EMPTY_SET;
        }
        return Collections.unmodifiableSet(config.getUserConfiguration().getDefaultRoles());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.admin.FileshareAdminConfigProvider#isMultiRealm()
     */
    @Override
    public boolean isMultiRealm () {
        FileshareConfiguration config = getEffectiveFileshareConfiguration();
        if ( config == null ) {
            return false;
        }
        return config.getAuthConfiguration().getAuthenticators().getAuthenticators().size() > 1;
    }
}
