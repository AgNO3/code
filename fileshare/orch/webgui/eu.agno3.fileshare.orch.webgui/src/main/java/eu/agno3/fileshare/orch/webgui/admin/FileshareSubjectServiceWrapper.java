/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.09.2015 by mbechler
 */
package eu.agno3.fileshare.orch.webgui.admin;


import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.SecurityLabel;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.query.SubjectQueryResult;
import eu.agno3.fileshare.orch.common.config.FileshareConfiguration;
import eu.agno3.fileshare.orch.common.config.desc.FileshareServiceTypeDescriptor;
import eu.agno3.fileshare.orch.common.service.FileshareSubjectServerService;
import eu.agno3.fileshare.service.admin.SubjectServiceMBean;
import eu.agno3.orchestrator.config.auth.RoleConfig;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
public class FileshareSubjectServiceWrapper implements SubjectServiceMBean {

    @Inject
    private StructureViewContextBean context;

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private FileshareAdminConfigProviderImpl configProvider;


    /**
     * @return
     * @throws GuiWebServiceException
     * @throws FileshareException
     */
    private FileshareSubjectServerService getServerService () throws FileshareException {
        try {
            return this.ssp.getService(FileshareSubjectServerService.class);
        }
        catch (
            GuiWebServiceException |
            UndeclaredThrowableException e ) {
            throw new FileshareException("Failed to get subject service", e); //$NON-NLS-1$
        }
    }


    /**
     * @return
     * @throws FileshareException
     * 
     */
    private ServiceStructuralObject getContext () throws FileshareException {
        try {
            ServiceStructuralObject selectedService = this.context.getSelectedService();

            if ( selectedService == null ) {
                throw new FileshareException("No service selected"); //$NON-NLS-1$
            }

            if ( !FileshareServiceTypeDescriptor.FILESHARE_SERVICE_TYPE.equals(selectedService.getServiceType()) ) {
                throw new FileshareException("Selected service is not a fileshare service " + selectedService.getServiceType()); //$NON-NLS-1$
            }
            return selectedService;
        }
        catch (
            ModelObjectNotFoundException |
            ModelServiceException |
            GuiWebServiceException |
            UndeclaredThrowableException e ) {
            throw new FileshareException("Failed to get fileshare service", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.SubjectServiceMBean#getSubject(java.util.UUID)
     */
    @Override
    public Subject getSubject ( UUID id ) throws FileshareException {
        try {
            return getServerService().getSubject(getContext(), id);
        }
        catch (
            ModelObjectNotFoundException |
            AgentDetachedException |
            ModelServiceException |
            AgentCommunicationErrorException e ) {
            throw new FileshareException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.SubjectServiceMBean#getSubjectInfo(java.util.UUID)
     */
    @Override
    public SubjectQueryResult getSubjectInfo ( UUID id ) throws FileshareException {
        try {
            return getServerService().getSubjectInfo(getContext(), id);
        }
        catch (
            ModelObjectNotFoundException |
            AgentDetachedException |
            ModelServiceException |
            AgentCommunicationErrorException e ) {
            throw new FileshareException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.SubjectServiceMBean#getUserInfo(eu.agno3.runtime.security.principal.UserPrincipal)
     */
    @Override
    public SubjectQueryResult getUserInfo ( UserPrincipal principal ) throws FileshareException {
        try {
            return getServerService().getUserInfo(getContext(), principal);
        }
        catch (
            ModelObjectNotFoundException |
            AgentDetachedException |
            ModelServiceException |
            AgentCommunicationErrorException e ) {
            throw new FileshareException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.SubjectServiceMBean#querySubjects(java.lang.String, int)
     */
    @Override
    public List<SubjectQueryResult> querySubjects ( String query, int i ) throws FileshareException {
        try {
            return getServerService().querySubjects(getContext(), query, i);
        }
        catch (
            ModelObjectNotFoundException |
            AgentDetachedException |
            ModelServiceException |
            AgentCommunicationErrorException e ) {
            throw new FileshareException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.SubjectServiceMBean#querySubjectsExcludingMembers(java.lang.String,
     *      java.util.UUID, int)
     */
    @Override
    public List<SubjectQueryResult> querySubjectsExcludingMembers ( String query, UUID groupId, int i ) throws FileshareException {
        try {
            return getServerService().querySubjectsExcludingMembers(getContext(), query, groupId, i);
        }
        catch (
            ModelObjectNotFoundException |
            AgentDetachedException |
            ModelServiceException |
            AgentCommunicationErrorException e ) {
            throw new FileshareException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.SubjectServiceMBean#addRole(java.util.UUID, java.lang.String)
     */
    @Override
    public void addRole ( UUID id, String role ) throws FileshareException {
        try {
            getServerService().addRole(getContext(), id, role);
        }
        catch (
            ModelObjectNotFoundException |
            AgentDetachedException |
            ModelServiceException |
            AgentCommunicationErrorException e ) {
            throw new FileshareException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.SubjectServiceMBean#setRoles(java.util.UUID, java.util.Set)
     */
    @Override
    public void setRoles ( UUID id, Set<String> roles ) throws FileshareException {
        try {
            getServerService().setRoles(getContext(), id, roles);
        }
        catch (
            ModelObjectNotFoundException |
            AgentDetachedException |
            ModelServiceException |
            AgentCommunicationErrorException e ) {
            throw new FileshareException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.SubjectServiceMBean#removeRole(java.util.UUID, java.lang.String)
     */
    @Override
    public void removeRole ( UUID id, String role ) throws FileshareException {
        try {
            getServerService().removeRole(getContext(), id, role);
        }
        catch (
            ModelObjectNotFoundException |
            AgentDetachedException |
            ModelServiceException |
            AgentCommunicationErrorException e ) {
            throw new FileshareException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.SubjectServiceMBean#getEffectiveRoles(java.util.UUID)
     */
    @Override
    public Set<String> getEffectiveRoles ( UUID id ) throws FileshareException {
        try {
            return getServerService().getEffectiveRoles(getContext(), id);
        }
        catch (
            ModelObjectNotFoundException |
            AgentDetachedException |
            ModelServiceException |
            AgentCommunicationErrorException e ) {
            ExceptionHandler.handle(e);
            throw new FileshareException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.SubjectServiceMBean#getAvailableRoles()
     */
    @Override
    public Collection<String> getAvailableRoles () throws FileshareException {
        FileshareConfiguration cfg = this.configProvider.getEffectiveFileshareConfiguration();
        if ( cfg == null ) {
            return Collections.EMPTY_LIST;
        }
        Set<String> roles = new HashSet<>();
        for ( RoleConfig roleConfig : cfg.getAuthConfiguration().getRoleConfig().getRoles() ) {
            if ( !roleConfig.getHidden() ) {
                roles.add(roleConfig.getRoleId());
            }
        }
        return roles;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.SubjectServiceMBean#getSubjectRootLabel(java.util.UUID)
     */
    @Override
    public SecurityLabel getSubjectRootLabel ( UUID subjectId ) throws FileshareException {
        try {
            return getServerService().getSubjectRootLabel(getContext(), subjectId);
        }
        catch (
            ModelObjectNotFoundException |
            AgentDetachedException |
            ModelServiceException |
            AgentCommunicationErrorException e ) {
            throw new FileshareException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.SubjectServiceMBean#setSubjectRootSecurityLabel(java.util.UUID,
     *      java.lang.String)
     */
    @Override
    public void setSubjectRootSecurityLabel ( UUID id, String label ) throws FileshareException {
        try {
            getServerService().setSubjectRootSecurityLabel(getContext(), id, label);
        }
        catch (
            ModelObjectNotFoundException |
            AgentDetachedException |
            ModelServiceException |
            AgentCommunicationErrorException e ) {
            throw new FileshareException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.SubjectServiceMBean#setSubjectRootSecurityLabelRecursive(java.util.UUID,
     *      java.lang.String)
     */
    @Override
    public void setSubjectRootSecurityLabelRecursive ( UUID id, String label ) throws FileshareException {
        try {
            getServerService().setSubjectRootSecurityLabelRecursive(getContext(), id, label);
        }
        catch (
            ModelObjectNotFoundException |
            AgentDetachedException |
            ModelServiceException |
            AgentCommunicationErrorException e ) {
            throw new FileshareException(e);
        }
    }

}
