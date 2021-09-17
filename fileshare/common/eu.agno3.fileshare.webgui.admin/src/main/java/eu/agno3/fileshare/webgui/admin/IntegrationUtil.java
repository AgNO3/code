/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.09.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.admin;


import javax.inject.Inject;
import javax.inject.Named;


/**
 * @author mbechler
 *
 */
@Named ( "app_fs_adm_integration" )
public class IntegrationUtil {

    @Inject
    private FileshareAdminServiceProvider sp;


    /**
     * @param dialog
     * @return a dialog url adjusted for the specific context
     */
    public String wrapDialog ( String dialog ) {
        return this.sp.wrapURL(dialog);
    }
}
