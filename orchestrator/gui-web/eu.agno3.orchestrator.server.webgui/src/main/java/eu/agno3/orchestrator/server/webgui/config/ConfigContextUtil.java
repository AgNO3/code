/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.02.2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config;


import javax.enterprise.context.ApplicationScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObjectMutable;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.structure.AgentStateTracker;
import eu.agno3.orchestrator.server.webgui.structure.ServiceStateTracker;
import eu.agno3.orchestrator.server.webgui.structure.StructureCacheBean;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "configContextUtil" )
public class ConfigContextUtil {

    @Inject
    private ServiceStateTracker serviceStateTracker;

    @Inject
    private AgentStateTracker agentStateTracker;

    @Inject
    private StructureCacheBean structureCache;


    public SelectItem[] getDetailLevels () {
        SelectItem[] res = new SelectItem[3];
        res[ 0 ] = new SelectItem(1, GuiMessages.get("config.detail.1")); //$NON-NLS-1$
        res[ 1 ] = new SelectItem(2, GuiMessages.get("config.detail.2")); //$NON-NLS-1$
        res[ 2 ] = new SelectItem(3, GuiMessages.get("config.detail.3")); //$NON-NLS-1$
        return res;
    }


    public boolean isInstance ( Object o ) {
        if ( ! ( o instanceof AbstractConfigContextBean ) ) {
            return false;
        }

        AbstractConfigContextBean<ConfigurationObject, @Nullable ConfigurationObjectMutable> ctx = cast(o);
        return !ctx.getAbstract();
    }


    public boolean isOnline ( Object o ) {
        if ( !this.isInstance(o) ) {
            return false;
        }

        AbstractConfigContextBean<ConfigurationObject, @Nullable ConfigurationObjectMutable> ctx = cast(o);
        try {
            StructuralObject anchor = ctx.getAnchor();

            if ( ! ( anchor instanceof ServiceStructuralObject ) ) {
                return false;
            }

            return this.serviceStateTracker.isServiceOnline((ServiceStructuralObject) anchor);
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return false;
    }


    public boolean isInstanceOnline ( Object o ) {
        InstanceStructuralObject instance = getInstance(o);
        if ( instance == null ) {
            return false;
        }
        return this.agentStateTracker.isAgentOnline(instance);
    }


    public InstanceStructuralObject getInstance ( Object o ) {
        if ( !this.isInstance(o) ) {
            return null;
        }

        AbstractConfigContextBean<ConfigurationObject, @Nullable ConfigurationObjectMutable> ctx = cast(o);
        try {
            StructuralObject anchor = ctx.getAnchor();

            if ( ! ( anchor instanceof ServiceStructuralObject ) ) {
                return null;
            }

            StructuralObject parent = this.structureCache.getParentFor(anchor);

            if ( parent instanceof InstanceStructuralObject ) {
                return (InstanceStructuralObject) parent;
            }
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;

    }


    public String getInstanceDisabledMessage ( Object o ) {
        if ( !isInstanceOnline(o) ) {
            return GuiMessages.get("config.instance.offline"); //$NON-NLS-1$
        }

        return GuiMessages.get("config.instance.online"); //$NON-NLS-1$
    }


    public String getDisabledMessage ( Object o ) {
        if ( !isOnline(o) ) {
            return GuiMessages.get("config.service.offline"); //$NON-NLS-1$
        }

        return GuiMessages.get("config.service.online"); //$NON-NLS-1$
    }


    /**
     * @param o
     * @return
     */
    @SuppressWarnings ( "unchecked" )
    private static AbstractConfigContextBean<ConfigurationObject, @Nullable ConfigurationObjectMutable> cast ( Object o ) {
        return (AbstractConfigContextBean<ConfigurationObject, @Nullable ConfigurationObjectMutable>) o;
    }

}
