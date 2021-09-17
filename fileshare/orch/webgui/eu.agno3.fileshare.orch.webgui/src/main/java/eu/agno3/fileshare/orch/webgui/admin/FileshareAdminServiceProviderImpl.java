/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.09.2015 by mbechler
 */
package eu.agno3.fileshare.orch.webgui.admin;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import eu.agno3.fileshare.service.admin.GroupServiceMBean;
import eu.agno3.fileshare.service.admin.SubjectServiceMBean;
import eu.agno3.fileshare.service.admin.UserServiceMBean;
import eu.agno3.fileshare.webgui.admin.FileshareAdminConfigProvider;
import eu.agno3.fileshare.webgui.admin.FileshareAdminServiceProvider;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;
import eu.agno3.runtime.security.RoleTranslator;
import eu.agno3.runtime.security.password.PasswordPolicyChecker;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
public class FileshareAdminServiceProviderImpl implements FileshareAdminServiceProvider {

    @Inject
    private FileshareAdminConfigProvider configProvider;

    @Inject
    private FilesharePasswordPolicyChecker passwordPolicy;

    @Inject
    private FileshareAdminRoleTranslator roleTranslator;

    @Inject
    private FileshareGroupServiceWrapper groupServiceWrapper;

    @Inject
    private FileshareUserServiceWrapper userServiceWrapper;

    @Inject
    private FileshareSubjectServiceWrapper subjectServiceWrapper;

    @Inject
    private StructureViewContextBean structureContext;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.admin.FileshareAdminServiceProvider#getConfigurationProvider()
     */
    @Override
    public FileshareAdminConfigProvider getConfigurationProvider () {
        return this.configProvider;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.admin.FileshareAdminServiceProvider#getPasswordPolicy()
     */
    @Override
    public PasswordPolicyChecker getPasswordPolicy () {
        return this.passwordPolicy;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.admin.FileshareAdminServiceProvider#getRoleTranslator()
     */
    @Override
    public RoleTranslator getRoleTranslator () {
        return this.roleTranslator;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.admin.FileshareAdminServiceProvider#getGroupService()
     */
    @Override
    public GroupServiceMBean getGroupService () {
        return this.groupServiceWrapper;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.admin.FileshareAdminServiceProvider#getSubjectService()
     */
    @Override
    public SubjectServiceMBean getSubjectService () {
        return this.subjectServiceWrapper;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.admin.FileshareAdminServiceProvider#getUserService()
     */
    @Override
    public UserServiceMBean getUserService () {
        return this.userServiceWrapper;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.admin.FileshareAdminServiceProvider#wrapURL(java.lang.String)
     */
    @Override
    public String wrapURL ( String dialogUrl ) {
        int queryPos = dialogUrl.indexOf('?');
        String extraQuery = "service=" + this.structureContext.getSelectedObjectId(); //$NON-NLS-1$
        if ( this.structureContext.getSelectedAnchorId() != null ) {
            extraQuery += "&anchor=" + this.structureContext.getSelectedAnchorId(); //$NON-NLS-1$
        }
        if ( queryPos >= 0 ) {
            return dialogUrl + "&" + extraQuery; //$NON-NLS-1$
        }
        return dialogUrl + "?" + extraQuery; //$NON-NLS-1$
    }

}
