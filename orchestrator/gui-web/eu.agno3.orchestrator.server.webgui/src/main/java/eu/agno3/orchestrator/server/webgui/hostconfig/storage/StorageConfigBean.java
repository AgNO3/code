/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.03.2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.hostconfig.storage;


import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.orchestrator.config.hostconfig.HostConfiguration;
import eu.agno3.orchestrator.config.hostconfig.desc.HostConfigServiceTypeDescriptor;
import eu.agno3.orchestrator.config.hostconfig.storage.MountEntry;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.components.AbstractObjectEditor;
import eu.agno3.orchestrator.server.webgui.components.OuterWrapper;
import eu.agno3.orchestrator.server.webgui.config.ConfigContext;
import eu.agno3.orchestrator.server.webgui.config.EffectiveConfigCacheBean;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.util.completer.Completer;
import eu.agno3.orchestrator.server.webgui.util.completer.ListCompleter;


/**
 * @author mbechler
 *
 */
@Named ( "storageConfigBean" )
@ApplicationScoped
public class StorageConfigBean {

    /**
     * 
     */
    private static final String HC_TYPE = "urn:agno3:objects:1.0:hostconfig"; //$NON-NLS-1$

    /**
     * 
     */
    private static final String SYSTEM_STORAGE = "system"; //$NON-NLS-1$

    @Inject
    private EffectiveConfigCacheBean hcCache;


    public Completer<String> getMountCompleter ( ConfigContext<?, ?> ctx ) {
        try {
            if ( ctx == null || ctx.getAbstract() ) {
                return fallbackCompleter();
            }
            StructuralObject anchor = ctx.getAnchor();
            if ( anchor == null ) {
                return fallbackCompleter();
            }
            HostConfiguration hc = (HostConfiguration) this.hcCache
                    .getEffectiveSingletonConfig(anchor, HostConfigServiceTypeDescriptor.HOSTCONFIG_SERVICE_TYPE, HC_TYPE);
            if ( hc == null ) {
                return fallbackCompleter();
            }
            return makeCompleter(hc.getStorageConfiguration().getMountEntries());
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return fallbackCompleter();
        }
    }


    @SuppressWarnings ( "unchecked" )
    public Completer<String> getHostConfigMountCompleter ( OuterWrapper<?> outer )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {

        AbstractObjectEditor<?> storageConfig = outer.resolve(
            HC_TYPE, // $NON-NLS-1$
            "storageConfiguration/mountEntries");//$NON-NLS-1$

        if ( storageConfig == null ) {
            return fallbackCompleter();
        }
        return makeCompleter((Collection<MountEntry>) storageConfig.getEffective());
    }


    /**
     * @return
     */
    ListCompleter fallbackCompleter () {
        return new ListCompleter(SYSTEM_STORAGE);
    }


    /**
     * @param mounts
     * @return
     */
    Completer<String> makeCompleter ( final Collection<MountEntry> mounts ) {
        return new Completer<String>() {

            @Override
            public List<String> complete ( String query ) {
                List<String> res = new LinkedList<>();
                for ( MountEntry e : mounts ) {
                    String alias = e.getAlias();
                    if ( alias != null && !alias.isEmpty() && alias.startsWith(query) ) {
                        res.add(alias);
                    }
                }
                if ( StringUtils.isBlank(query) || SYSTEM_STORAGE.startsWith(query) ) { // $NON-NLS-1$
                    res.add(SYSTEM_STORAGE);
                }
                return res;
            }
        };
    }
}
