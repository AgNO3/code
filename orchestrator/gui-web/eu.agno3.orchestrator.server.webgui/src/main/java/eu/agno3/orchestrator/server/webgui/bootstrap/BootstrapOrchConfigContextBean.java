/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.03.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.bootstrap;


import javax.faces.FacesException;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.groups.Default;

import org.apache.myfaces.extensions.validator.beanval.annotation.BeanValidation;
import org.apache.myfaces.extensions.validator.beanval.annotation.ModelValidation;
import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectConflictException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectReferentialIntegrityException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectValidationException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.versioning.RevisionProvider;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.context.ConfigurationEditContext;
import eu.agno3.orchestrator.config.model.realm.service.ServiceService;
import eu.agno3.orchestrator.config.model.validation.Instance;
import eu.agno3.orchestrator.config.orchestrator.OrchestratorConfiguration;
import eu.agno3.orchestrator.config.orchestrator.OrchestratorConfigurationMutable;
import eu.agno3.orchestrator.config.orchestrator.desc.OrchestratorServiceTypeDescriptor;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.server.webgui.config.AbstractConfigContextBean;
import eu.agno3.orchestrator.server.webgui.config.ConfigApplyContextBean;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;


/**
 * @author mbechler
 * 
 */
@Named ( "bootstrapOrchConfigContext" )
@ViewScoped
@BeanValidation ( useGroups = {
    Default.class
}, modelValidation = @ModelValidation ( isActive = true ) )
public class BootstrapOrchConfigContextBean extends AbstractConfigContextBean<OrchestratorConfiguration, @Nullable OrchestratorConfigurationMutable> {

    /**
     * 
     */
    private static final long serialVersionUID = 4338591484487573077L;

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private BootstrapContextProvider bcp;


    @Override
    protected void checkContext () throws ModelServiceException {
        if ( this.bcp.getContext() == null ) {
            throw new ModelServiceException("No bootstrap context available"); //$NON-NLS-1$
        }
    }


    @Override
    public boolean isShowDefaultReset () {
        return false;
    }


    /**
     * @return
     */
    private ServiceStructuralObject getOrchestratorService () {
        return this.bcp.getContext().getOrchConfigService();
    }


    public String getServiceTypeName () {
        return OrchestratorServiceTypeDescriptor.ORCHESTRATOR_SERVICE_TYPE;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigContextBean#getInnerEditorDialogTemplate()
     */
    @Override
    public String getInnerEditorDialogTemplate () {
        throw new FacesException("Not supported"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.ConfigContext#getTestTemplate()
     */
    @Override
    public String getTestTemplate () {
        throw new FacesException("Not supported"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * 
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigContextBean#getObjectTypeName()
     */
    @Override
    public String getObjectTypeName () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        return "urn:agno3:objects:1.0:orchestrator"; //$NON-NLS-1$
    }


    @Override
    public StructuralObject getAnchor () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        return this.getOrchestratorService();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigContextBean#getAbstract()
     */
    @Override
    public boolean getAbstract () {
        return false;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * 
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigContextBean#fetchContext()
     */
    @Override
    protected ConfigurationEditContext<ConfigurationObject, ConfigurationObject> fetchContext ()
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        return this.ssp.getService(ServiceService.class).getEditContext(this.getOrchestratorService());
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelObjectConflictException
     * 
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigContextBean#internalDoUpdate()
     */
    @Override
    protected void internalDoUpdate () throws ModelServiceException, ModelObjectValidationException, ModelObjectNotFoundException,
            GuiWebServiceException, ModelObjectConflictException {}


    @Override
    public RevisionProvider getRevisionProvider () {
        return null;
    }


    @Override
    protected JobInfo internalApplyConfiguration ( ConfigApplyContextBean applyCtx )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException, ModelObjectReferentialIntegrityException {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigContextBean#getValidationGroups()
     */
    @Override
    protected Class<?>[] getValidationGroups () {
        return new Class<?>[] {
            Instance.class
        };
    }

}
