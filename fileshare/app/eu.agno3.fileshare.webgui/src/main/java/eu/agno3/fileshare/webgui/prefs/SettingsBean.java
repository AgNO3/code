/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 6, 2016 by mbechler
 */
package eu.agno3.fileshare.webgui.prefs;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.runtime.jsf.prefs.PreferenceController;
import eu.agno3.runtime.security.web.gui.PasswordChangeBean;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "settingsBean" )
public class SettingsBean {

    @Inject
    private PreferenceController preferenceController;

    @Inject
    private PasswordChangeBean pwChange;


    /**
     * 
     * @return dialog close
     */
    public String saveAndClose () {
        this.pwChange.reset(null);
        return this.preferenceController.saveAndClose();
    }


    /**
     * 
     * @return dialog close
     */
    public String resetAndClose () {
        this.pwChange.reset(null);
        return this.preferenceController.resetAndClose();
    }
}
