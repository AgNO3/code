/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.12.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.frontpage;


import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;

import eu.agno3.orchestrator.config.model.realm.StructuralObjectMutable;
import eu.agno3.orchestrator.config.model.realm.service.StructuralObjectService;
import eu.agno3.orchestrator.server.webgui.bootstrap.BootstrapContextProvider;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.structure.StructureUtil;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "frontpageController" )
public class FrontpageController {

    private static final Logger log = Logger.getLogger(FrontpageController.class);

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private BootstrapContextProvider bcp;


    public void redirectIfNeccessary ( ComponentSystemEvent ev ) {
        try {
            log.debug("redirectIfNeccessary called"); //$NON-NLS-1$

            if ( SecurityUtils.getSubject().hasRole("ADMIN") && this.bcp.getContext() != null ) { //$NON-NLS-1$
                log.debug("redirecting to installer"); //$NON-NLS-1$
                redirectToInstaller();
                return;
            }

            redirectToTopLevelObject();
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
    }


    /**
     * 
     */
    private static void redirectToInstaller () {
        getNavigationHandler().performNavigation("/installer/index.xhtml?faces-redirect=true"); //$NON-NLS-1$
    }


    /**
     * 
     */
    private void redirectToTopLevelObject () {
        StructuralObjectMutable structureRoot;
        try {
            structureRoot = this.ssp.getService(StructuralObjectService.class).getStructureRoot();
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return;
        }

        StructuralObjectMutable topLevel = getTopLevelChild(structureRoot);
        ConfigurableNavigationHandler nh = getNavigationHandler();
        String topLevelOutcome = StructureUtil.getOutcomeForObjectOverview(topLevel);

        if ( log.isDebugEnabled() ) {
            log.debug("Redirecting to " + topLevelOutcome); //$NON-NLS-1$
        }

        nh.performNavigation(topLevelOutcome);
    }


    /**
     * @return
     */
    private static ConfigurableNavigationHandler getNavigationHandler () {
        return (ConfigurableNavigationHandler) FacesContext.getCurrentInstance().getApplication().getNavigationHandler();
    }


    /**
     * @param structureRoot
     * @return
     */
    private StructuralObjectMutable getTopLevelChild ( StructuralObjectMutable root ) {
        StructuralObjectMutable cur = root;
        while ( true ) {
            Set<StructuralObjectMutable> children = null;
            try {
                children = this.ssp.getService(StructuralObjectService.class).fetchChildren(cur);
            }
            catch ( Exception e ) {
                ExceptionHandler.handle(e);
                break;
            }

            if ( children != null && !children.isEmpty() && children.size() == 1 ) {
                cur = children.iterator().next();
            }
            else {
                break;
            }
        }

        return cur;
    }
}
