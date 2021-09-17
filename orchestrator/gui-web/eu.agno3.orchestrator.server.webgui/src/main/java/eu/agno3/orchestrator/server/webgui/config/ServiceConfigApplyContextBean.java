/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 12, 2017 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config;


import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectReferentialIntegrityException;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.context.ConfigApplyContext;
import eu.agno3.orchestrator.config.model.realm.service.ConfigApplyService;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.config.instance.InstanceConfigContextBean;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.exceptions.ModelExceptionHandler;
import eu.agno3.orchestrator.server.webgui.structure.StructureCacheBean;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "serviceConfigApplyContextBean" )
public class ServiceConfigApplyContextBean extends BaseConfigApplyContextBean {

    /**
     * 
     */
    private static final long serialVersionUID = 6156546569054610752L;

    private static final Logger log = Logger.getLogger(InstanceConfigApplyContextBean.class);

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private InstanceConfigContextBean instanceConfig;

    @Inject
    private StructureCacheBean structureCache;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.BaseConfigApplyContextBean#getTargetInstance()
     */
    @Override
    protected InstanceStructuralObject getTargetInstance () {
        try {
            StructuralObject parent = this.structureCache.getParentFor(this.instanceConfig.getAnchor());
            if ( parent instanceof InstanceStructuralObject ) {
                return (InstanceStructuralObject) parent;
            }
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }


    /**
     * 
     */
    public void init () {
        ServiceStructuralObject service;
        try {
            service = this.instanceConfig.getAnchor();
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return;
        }
        if ( service == null ) {
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
                setApplyContext(
                    this.ssp.getService(ConfigApplyService.class).preApplyServiceConfiguration(service, this.instanceConfig.getRevision()));
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
