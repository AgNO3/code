/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.06.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.admin.integration;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.ops4j.pax.cdi.api.OsgiService;

import eu.agno3.fileshare.service.admin.GroupServiceMBean;
import eu.agno3.fileshare.service.admin.SubjectServiceMBean;
import eu.agno3.fileshare.service.admin.UserServiceMBean;
import eu.agno3.fileshare.webgui.admin.FileshareAdminConfigProvider;
import eu.agno3.fileshare.webgui.admin.FileshareAdminServiceProvider;
import eu.agno3.fileshare.webgui.login.AuthInfoBean;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.runtime.security.RoleTranslator;
import eu.agno3.runtime.security.password.PasswordPolicyChecker;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
public class AdminServiceProviderImpl implements FileshareAdminServiceProvider {

    @Inject
    private FileshareServiceProvider fsp;

    @Inject
    private AuthInfoBean authInfo;

    @Inject
    @OsgiService ( dynamic = true, timeout = 100 )
    private RoleTranslator roleTranslator;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.admin.FileshareAdminServiceProvider#getGroupService()
     */
    @Override
    public GroupServiceMBean getGroupService () {
        return this.fsp.getGroupService();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.admin.FileshareAdminServiceProvider#getConfigurationProvider()
     */
    @Override
    public FileshareAdminConfigProvider getConfigurationProvider () {
        return new ConfigProviderWrapper(this.fsp.getConfigurationProvider(), this.authInfo);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.admin.FileshareAdminServiceProvider#getSubjectService()
     */
    @Override
    public SubjectServiceMBean getSubjectService () {
        return this.fsp.getSubjectService();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.admin.FileshareAdminServiceProvider#getUserService()
     */
    @Override
    public UserServiceMBean getUserService () {
        return this.fsp.getUserService();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.admin.FileshareAdminServiceProvider#getPasswordPolicy()
     */
    @Override
    public PasswordPolicyChecker getPasswordPolicy () {
        return this.fsp.getPasswordPolicy();
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
     * @see eu.agno3.fileshare.webgui.admin.FileshareAdminServiceProvider#wrapURL(java.lang.String)
     */
    @Override
    public String wrapURL ( String dialogUrl ) {
        return dialogUrl;
    }
}
