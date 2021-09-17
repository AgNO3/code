/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.07.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.hostconfig;


import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.hostconfig.HostConfiguration;
import eu.agno3.orchestrator.config.hostconfig.desc.HostConfigServiceTypeDescriptor;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.server.webgui.config.EffectiveConfigCacheBean;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;


/**
 * @author mbechler
 *
 */
@ViewScoped
public class EffectiveHostConfigProvider implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4003836431576303780L;

    private static final Logger log = Logger.getLogger(EffectiveHostConfigProvider.class);

    @Inject
    private StructureViewContextBean context;

    @Inject
    private EffectiveConfigCacheBean cacheBean;


    public HostConfiguration getEffectiveHostConfiguration () {
        if ( !this.context.isInstanceSelected() && !this.context.isServiceSelected() ) {
            return null;
        }

        try {
            StructuralObject selectedObject = this.context.getSelectedObject();

            if ( log.isDebugEnabled() ) {
                log.debug("Loading host configuration for " + selectedObject); //$NON-NLS-1$
            }
            ConfigurationObject effectiveConfig = this.cacheBean.getEffectiveSingletonConfig(
                selectedObject,
                HostConfigServiceTypeDescriptor.HOSTCONFIG_SERVICE_TYPE,
                "urn:agno3:objects:1.0:hostconfig"); //$NON-NLS-1$

            if ( effectiveConfig == null ) {
                log.debug("Config not found"); //$NON-NLS-1$
                return null;
            }

            if ( !HostConfiguration.class.isAssignableFrom(effectiveConfig.getType()) ) {
                log.error("Return configuration is not compatible " + effectiveConfig.getType()); //$NON-NLS-1$
                return null;
            }

            return (HostConfiguration) effectiveConfig;
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return null;
        }
    }
}
