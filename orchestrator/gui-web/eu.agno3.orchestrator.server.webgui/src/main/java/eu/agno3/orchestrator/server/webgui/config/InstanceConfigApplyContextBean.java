/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: May 16, 2017 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config;


import javax.faces.context.FacesContext;
import javax.faces.event.PreRenderViewEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectReferentialIntegrityException;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.context.ConfigApplyContext;
import eu.agno3.orchestrator.config.model.realm.service.ConfigApplyService;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.exceptions.ModelExceptionHandler;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "instanceConfigApplyContextBean" )
public class InstanceConfigApplyContextBean extends BaseConfigApplyContextBean {

    /**
     * 
     */
    private static final long serialVersionUID = 6871936252616497783L;

    private static final Logger log = Logger.getLogger(InstanceConfigApplyContextBean.class);

    @Inject
    private StructureViewContextBean structureContext;

    @Inject
    private ServerServiceProvider ssp;


    private InstanceStructuralObject getInstance () {
        try {
            InstanceStructuralObject instance = (InstanceStructuralObject) this.structureContext.getSelectedAnchor();
            if ( this.structureContext.getSelectedAnchor() instanceof InstanceStructuralObject ) {
                return instance;
            }
            return this.structureContext.getSelectedInstance();
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.BaseConfigApplyContextBean#getTargetInstance()
     */
    @Override
    protected InstanceStructuralObject getTargetInstance () {
        return getInstance();
    }


    public void init ( PreRenderViewEvent ev ) {
        init();
    }


    /**
     * 
     */
    public void init () {
        InstanceStructuralObject instance = getInstance();
        if ( instance == null ) {
            return;
        }

        if ( this.getApplyContext() != null ) {
            return;
        }

        Object applyCtx = FacesContext.getCurrentInstance().getExternalContext().getFlash().get("applyContext"); //$NON-NLS-1$
        if ( applyCtx instanceof ConfigApplyContext ) {
            log.debug("Have context"); //$NON-NLS-1$
            setApplyContext((ConfigApplyContext) applyCtx);
        }
        else {
            try {
                log.debug("Get fresh context"); //$NON-NLS-1$
                setApplyContext(this.ssp.getService(ConfigApplyService.class).preApplyInstanceConfigurations(instance, null));
            }
            catch (
                ModelObjectReferentialIntegrityException |
                ModelObjectNotFoundException e ) {
                ModelExceptionHandler.handleException(GuiMessages.get(GuiMessages.CONFIG_SSV_FAILED), e);
            }
            catch ( Exception e ) {
                ExceptionHandler.handle(e);
            }
        }
    }

}
