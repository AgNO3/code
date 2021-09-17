/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.06.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.admin;


import eu.agno3.fileshare.service.admin.GroupServiceMBean;
import eu.agno3.fileshare.service.admin.SubjectServiceMBean;
import eu.agno3.fileshare.service.admin.UserServiceMBean;
import eu.agno3.runtime.security.RoleTranslator;
import eu.agno3.runtime.security.password.PasswordPolicyChecker;


/**
 * @author mbechler
 *
 */
public interface FileshareAdminServiceProvider {

    /**
     * @return the configuration provider
     */
    FileshareAdminConfigProvider getConfigurationProvider ();


    /**
     * @return the group service
     */
    GroupServiceMBean getGroupService ();


    /**
     * @return the subject service
     */
    SubjectServiceMBean getSubjectService ();


    /**
     * @return the user service
     */
    UserServiceMBean getUserService ();


    /**
     * @return the password policy checker
     */
    PasswordPolicyChecker getPasswordPolicy ();


    /**
     * 
     * @return the role translator
     */
    RoleTranslator getRoleTranslator ();


    /**
     * 
     * @param dialogUrl
     * @return a wrapped dialogUrl
     */
    String wrapURL ( String dialogUrl );

}
