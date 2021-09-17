/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.09.2015 by mbechler
 */
package eu.agno3.fileshare.orch.webgui.admin;


import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.joda.time.DateTime;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.query.GroupQueryResult;
import eu.agno3.fileshare.orch.common.config.desc.FileshareServiceTypeDescriptor;
import eu.agno3.fileshare.orch.common.service.FileshareGroupServerService;
import eu.agno3.fileshare.service.admin.GroupServiceMBean;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
public class FileshareGroupServiceWrapper implements GroupServiceMBean {

    @Inject
    private StructureViewContextBean context;

    @Inject
    private ServerServiceProvider ssp;


    /**
     * @return
     * @throws GuiWebServiceException
     * @throws FileshareException
     */
    private FileshareGroupServerService getServerService () throws FileshareException {
        try {
            return this.ssp.getService(FileshareGroupServerService.class);
        }
        catch (
            GuiWebServiceException |
            UndeclaredThrowableException e ) {
            throw new FileshareException("Failed to get group service", e); //$NON-NLS-1$
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
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#listGroups(int, int)
     */
    @Override
    public List<Group> listGroups ( int off, int limit ) throws FileshareException {
        try {
            return getServerService().listGroups(getContext(), off, limit);
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
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#getGroupCount()
     */
    @Override
    public long getGroupCount () throws FileshareException {
        try {
            return getServerService().getGroupCount(getContext());
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
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#getGroup(java.util.UUID)
     */
    @Override
    public Group getGroup ( UUID id ) throws FileshareException {
        try {
            return getServerService().getGroup(getContext(), id);
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
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#getGroupInfo(java.lang.String)
     */
    @Override
    public GroupQueryResult getGroupInfo ( String subjectName ) throws FileshareException {
        try {
            return getServerService().getGroupInfo(getContext(), subjectName);
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
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#createGroup(eu.agno3.fileshare.model.Group, boolean)
     */
    @Override
    public Group createGroup ( Group group, boolean createRoot ) throws FileshareException {
        try {
            Group createGroup = getServerService().createGroup(getContext(), group, createRoot);
            return createGroup;
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
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#deleteGroups(java.util.List)
     */
    @Override
    public void deleteGroups ( List<UUID> ids ) throws FileshareException {
        try {
            getServerService().deleteGroups(getContext(), ids);
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
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#queryGroups(java.lang.String, int)
     */
    @Override
    public List<GroupQueryResult> queryGroups ( String query, int limit ) throws FileshareException {
        try {
            return getServerService().queryGroups(getContext(), query, limit);
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
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#queryGroupsExcludingUserGroups(java.lang.String,
     *      java.util.UUID, int)
     */
    @Override
    public List<GroupQueryResult> queryGroupsExcludingUserGroups ( String query, UUID userId, int limit ) throws FileshareException {
        try {
            return getServerService().queryGroupsExcludingUserGroups(getContext(), query, userId, limit);
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
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#addToGroup(java.util.UUID, java.util.UUID)
     */
    @Override
    public void addToGroup ( UUID userId, UUID groupId ) throws FileshareException {
        try {
            getServerService().addToGroup(getContext(), userId, groupId);
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
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#addToGroups(java.util.UUID, java.util.Set)
     */
    @Override
    public void addToGroups ( UUID id, Set<UUID> groupIds ) throws FileshareException {
        try {
            getServerService().addToGroups(getContext(), id, groupIds);
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
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#getMembers(java.util.UUID)
     */
    @Override
    public List<Subject> getMembers ( UUID groupId ) throws FileshareException {
        try {
            return getServerService().getMembers(getContext(), groupId);
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
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#removeFromGroup(java.util.UUID, java.util.UUID)
     */
    @Override
    public void removeFromGroup ( UUID userId, UUID groupId ) throws FileshareException {
        try {
            getServerService().removeFromGroup(getContext(), userId, groupId);
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
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#removeFromGroups(java.util.UUID, java.util.Set)
     */
    @Override
    public void removeFromGroups ( UUID userId, Set<UUID> groupIds ) throws FileshareException {
        try {
            getServerService().removeFromGroups(getContext(), userId, groupIds);
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
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#addMembers(java.util.UUID, java.util.List)
     */
    @Override
    public void addMembers ( UUID groupId, List<UUID> subjectIds ) throws FileshareException {
        try {
            getServerService().addMembers(getContext(), groupId, subjectIds);
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
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#removeMembers(java.util.UUID, java.util.List)
     */
    @Override
    public void removeMembers ( UUID groupId, List<UUID> subjectIds ) throws FileshareException {
        try {
            getServerService().removeMembers(getContext(), groupId, subjectIds);
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
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#setNotificationDisabled(java.util.UUID, boolean)
     */
    @Override
    public void setNotificationDisabled ( UUID id, boolean disableNotifications ) throws FileshareException {
        try {
            getServerService().setNotificationDisabled(getContext(), id, disableNotifications);
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
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#setNotificationOverride(java.util.UUID, java.lang.String)
     */
    @Override
    public void setNotificationOverride ( UUID id, String overrideAddress ) throws FileshareException {
        try {
            getServerService().setNotificationOverride(getContext(), id, overrideAddress);
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
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#setGroupLocale(java.util.UUID, java.util.Locale)
     */
    @Override
    public void setGroupLocale ( UUID id, Locale groupLocale ) throws FileshareException {
        try {
            getServerService().setGroupLocale(getContext(), id, groupLocale);
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
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#updateGroupQuota(java.util.UUID, java.lang.Long)
     */
    @Override
    public void updateGroupQuota ( UUID groupId, Long quota ) throws FileshareException {
        try {
            getServerService().updateGroupQuota(getContext(), groupId, quota);
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
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#getGroupsLastModified()
     */
    @Override
    public DateTime getGroupsLastModified () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#getGroupsRecursiveLastModified()
     */
    @Override
    public DateTime getGroupsRecursiveLastModified () {
        return null;
    }
}
