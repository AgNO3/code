/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.roles;


import java.lang.reflect.UndeclaredThrowableException;
import java.util.MissingResourceException;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.fileshare.webgui.admin.FileshareAdminServiceProvider;


/**
 * @author mbechler
 *
 */
@Named ( "app_fs_adm_roleDisplayBean" )
@ApplicationScoped
public class RoleDisplayBean {

    private static final Logger log = Logger.getLogger(RoleDisplayBean.class);

    @Inject
    private FileshareAdminServiceProvider asp;


    /**
     * @param role
     * @return the role name to display
     */
    public String getRoleDisplayName ( String role ) {
        try {
            return this.asp.getRoleTranslator().getRoleTitle(role, FacesContext.getCurrentInstance().getViewRoot().getLocale());
        }
        catch (
            MissingResourceException |
            UndeclaredThrowableException e ) {
            log.debug("Role translation not found", e); //$NON-NLS-1$
            return role;
        }
    }


    /**
     * @param role
     * @return the role name to display
     */
    public String getRoleDescription ( String role ) {
        try {
            return this.asp.getRoleTranslator().getRoleDescription(role, FacesContext.getCurrentInstance().getViewRoot().getLocale());
        }
        catch (
            MissingResourceException |
            UndeclaredThrowableException e ) {
            log.debug("Role description not found", e); //$NON-NLS-1$
            return StringUtils.EMPTY;
        }
    }
}
