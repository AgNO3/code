/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.09.2015 by mbechler
 */
package eu.agno3.fileshare.orch.webgui.admin;


import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.joda.time.DateTime;

import eu.agno3.fileshare.exceptions.AuthenticationException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.UserNotFoundException;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.UserCreateData;
import eu.agno3.fileshare.model.UserDetails;
import eu.agno3.fileshare.orch.common.config.desc.FileshareServiceTypeDescriptor;
import eu.agno3.fileshare.orch.common.service.FileshareUserServerService;
import eu.agno3.fileshare.service.admin.UserServiceMBean;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentOfflineException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;
import eu.agno3.runtime.security.password.PasswordPolicyException;
import eu.agno3.runtime.security.principal.UserInfo;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
public class FileshareUserServiceWrapper implements UserServiceMBean {

    @Inject
    private StructureViewContextBean context;

    @Inject
    private ServerServiceProvider ssp;


    /**
     * @return
     * @throws GuiWebServiceException
     * @throws FileshareException
     */
    private FileshareUserServerService getServerService () throws FileshareException {
        try {
            return this.ssp.getService(FileshareUserServerService.class);
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
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#getCurrentUser()
     */
    @Override
    public User getCurrentUser () throws AuthenticationException, UserNotFoundException {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#getUser(java.util.UUID)
     */
    @Override
    public User getUser ( UUID userId ) throws FileshareException {
        try {
            return getServerService().getUser(getContext(), userId);
        }
        catch (
            ModelObjectNotFoundException |
            AgentDetachedException |
            ModelServiceException |
            AgentCommunicationErrorException |
            AgentOfflineException e ) {
            throw new FileshareException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#enableLocalUser(java.util.UUID)
     */
    @Override
    public void enableLocalUser ( UUID userId ) throws FileshareException {
        try {
            getServerService().enableLocalUser(getContext(), userId);
        }
        catch (
            ModelObjectNotFoundException |
            AgentDetachedException |
            ModelServiceException |
            AgentCommunicationErrorException |
            AgentOfflineException e ) {
            throw new FileshareException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#disableLocalUser(java.util.UUID)
     */
    @Override
    public void disableLocalUser ( UUID userId ) throws FileshareException {
        try {
            getServerService().disableLocalUser(getContext(), userId);
        }
        catch (
            ModelObjectNotFoundException |
            AgentDetachedException |
            ModelServiceException |
            AgentCommunicationErrorException |
            AgentOfflineException e ) {
            throw new FileshareException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#getLocalUserInfo(java.util.UUID)
     */
    @Override
    public UserInfo getLocalUserInfo ( UUID userId ) throws FileshareException {
        try {
            return getServerService().getLocalUserInfo(getContext(), userId);
        }
        catch (
            ModelObjectNotFoundException |
            AgentDetachedException |
            ModelServiceException |
            AgentCommunicationErrorException |
            AgentOfflineException e ) {
            throw new FileshareException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#getCurrentUserGroupClosure()
     */
    @Override
    public Set<Group> getCurrentUserGroupClosure () throws FileshareException {
        return Collections.EMPTY_SET;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#isCurrentUserMember(java.util.UUID)
     */
    @Override
    public boolean isCurrentUserMember ( UUID groupId ) throws FileshareException {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#getUserGroups(java.util.UUID)
     */
    @Override
    public List<Group> getUserGroups ( UUID userId ) throws FileshareException {
        try {
            return getServerService().getUserGroups(getContext(), userId);
        }
        catch (
            ModelObjectNotFoundException |
            AgentDetachedException |
            ModelServiceException |
            AgentCommunicationErrorException |
            AgentOfflineException e ) {
            throw new FileshareException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#getUserGroupClosure(java.util.UUID)
     */
    @Override
    public List<Group> getUserGroupClosure ( UUID userId ) throws FileshareException {
        try {
            return getServerService().getUserGroupClosure(getContext(), userId);
        }
        catch (
            ModelObjectNotFoundException |
            AgentDetachedException |
            ModelServiceException |
            AgentCommunicationErrorException |
            AgentOfflineException e ) {
            throw new FileshareException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#listUsers(int, int)
     */
    @Override
    public List<User> listUsers ( int off, int limit ) throws FileshareException {
        try {
            return getServerService().listUsers(getContext(), off, limit);
        }
        catch (
            ModelObjectNotFoundException |
            AgentDetachedException |
            ModelServiceException |
            AgentCommunicationErrorException |
            AgentOfflineException e ) {
            throw new FileshareException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#getUserCount()
     */
    @Override
    public long getUserCount () throws FileshareException {
        try {
            return getServerService().getUserCount(getContext());
        }
        catch (
            ModelObjectNotFoundException |
            AgentDetachedException |
            ModelServiceException |
            AgentCommunicationErrorException |
            AgentOfflineException e ) {
            throw new FileshareException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#createLocalUser(eu.agno3.fileshare.model.UserCreateData)
     */
    @Override
    public User createLocalUser ( UserCreateData userData ) throws FileshareException {
        try {
            return getServerService().createLocalUser(getContext(), userData);
        }
        catch (
            ModelObjectNotFoundException |
            AgentDetachedException |
            ModelServiceException |
            AgentCommunicationErrorException |
            AgentOfflineException e ) {
            throw new FileshareException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#deleteUsers(java.util.List)
     */
    @Override
    public void deleteUsers ( List<UUID> userIds ) throws FileshareException {
        try {
            getServerService().deleteUsers(getContext(), userIds);
        }
        catch (
            ModelObjectNotFoundException |
            AgentDetachedException |
            ModelServiceException |
            AgentCommunicationErrorException |
            AgentOfflineException e ) {
            throw new FileshareException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#changePassword(java.util.UUID, java.lang.String)
     */
    @Override
    public void changePassword ( UUID userId, String newPassword ) throws FileshareException {
        try {
            getServerService().changePassword(getContext(), userId, newPassword);
        }
        catch (
            ModelObjectNotFoundException |
            AgentDetachedException |
            ModelServiceException |
            AgentCommunicationErrorException |
            AgentOfflineException e ) {
            throw new FileshareException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#updateUserLabel(java.util.UUID, java.lang.String)
     */
    @Override
    public void updateUserLabel ( UUID id, String label ) throws FileshareException {
        try {
            getServerService().updateUserLabel(getContext(), id, label);
        }
        catch (
            ModelObjectNotFoundException |
            AgentDetachedException |
            ModelServiceException |
            AgentCommunicationErrorException |
            AgentOfflineException e ) {
            throw new FileshareException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#changeCurrentUserPassword(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void changeCurrentUserPassword ( String oldPassword, String newPassword ) throws FileshareException, PasswordPolicyException {
        // not applicable
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#getUserDetails(java.util.UUID)
     */
    @Override
    public UserDetails getUserDetails ( UUID userId ) throws FileshareException {
        try {
            return getServerService().getUserDetails(getContext(), userId);
        }
        catch (
            ModelObjectNotFoundException |
            AgentDetachedException |
            ModelServiceException |
            AgentCommunicationErrorException |
            AgentOfflineException e ) {
            throw new FileshareException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#updateUserDetails(java.util.UUID,
     *      eu.agno3.fileshare.model.UserDetails)
     */
    @Override
    public UserDetails updateUserDetails ( UUID userId, UserDetails data ) throws FileshareException {
        try {
            return getServerService().updateUserDetails(getContext(), userId, data);
        }
        catch (
            ModelObjectNotFoundException |
            AgentDetachedException |
            ModelServiceException |
            AgentCommunicationErrorException |
            AgentOfflineException e ) {
            throw new FileshareException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#updateUserQuota(java.util.UUID, java.lang.Long)
     */
    @Override
    public void updateUserQuota ( UUID userId, Long quota ) throws FileshareException {
        try {
            getServerService().updateUserQuota(getContext(), userId, quota);
        }
        catch (
            ModelObjectNotFoundException |
            AgentDetachedException |
            ModelServiceException |
            AgentCommunicationErrorException |
            AgentOfflineException e ) {
            throw new FileshareException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#enableUserRoot(java.util.UUID)
     */
    @Override
    public void enableUserRoot ( UUID id ) throws FileshareException {
        try {
            getServerService().enableUserRoot(getContext(), id);
        }
        catch (
            ModelObjectNotFoundException |
            AgentDetachedException |
            ModelServiceException |
            AgentCommunicationErrorException |
            AgentOfflineException e ) {
            throw new FileshareException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#disableUserRoot(java.util.UUID)
     */
    @Override
    public void disableUserRoot ( UUID id ) throws FileshareException {
        try {
            getServerService().disableUserRoot(getContext(), id);
        }
        catch (
            ModelObjectNotFoundException |
            AgentDetachedException |
            ModelServiceException |
            AgentCommunicationErrorException |
            AgentOfflineException e ) {
            throw new FileshareException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#setUserExpiry(java.util.UUID, org.joda.time.DateTime)
     */
    @Override
    public void setUserExpiry ( UUID id, DateTime expiration ) throws FileshareException {
        try {
            getServerService().setUserExpiry(getContext(), id, expiration);
        }
        catch (
            ModelObjectNotFoundException |
            AgentDetachedException |
            ModelServiceException |
            AgentCommunicationErrorException |
            AgentOfflineException e ) {
            throw new FileshareException(e);
        }
    }
}
