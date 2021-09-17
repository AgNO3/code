/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.02.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.admin;


import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.fileshare.model.SecurityLabel;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "app_fs_adm_securityLabelBean" )
public class SecurityLabelBean {

    @Inject
    private FileshareAdminServiceProvider fsp;


    /**
     * @param obj
     * @return the display value for the security label
     */
    public String translateSecurityLabel ( Object obj ) {
        if ( ! ( obj instanceof SecurityLabel ) ) {
            return FileshareAdminMessages.get(FileshareAdminMessages.UNLABELED_ENTITY);
        }
        SecurityLabel sl = (SecurityLabel) obj;
        return sl.getLabel();
    }


    /**
     * 
     * @return the defined security labels
     */
    public List<String> getDefinedSecurityLabels () {
        return new ArrayList<>(this.fsp.getConfigurationProvider().getDefinedSecurityLabels());
    }

}
