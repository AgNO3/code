/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.02.2015 by mbechler
 */
package eu.agno3.runtime.jsf.prefs;


import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.runtime.jsf.i18n.BaseMessages;
import eu.agno3.runtime.jsf.view.stacking.DialogContext;


/**
 * @author mbechler
 *
 */
@Named ( "preferenceController" )
@ApplicationScoped
public class PreferenceController {

    @Inject
    private UserPreferences prefs;


    /**
     * @return outcome
     */
    public String save () {
        if ( this.prefs.savePreferences() ) {
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_INFO,
                        BaseMessages.get("settings.preferencesSaved"), //$NON-NLS-1$
                        StringUtils.EMPTY));
            return null;
        }

        return null;
    }


    /**
     * 
     * @return outcome
     */
    public String resetToDefaults () {
        if ( this.prefs.resetPreferences() ) {
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_INFO,
                        BaseMessages.get("settings.preferencesReset"), //$NON-NLS-1$
                        StringUtils.EMPTY));
            return null;
        }

        return null;
    }


    /**
     * @return outcome
     */
    public String saveAndClose () {
        if ( this.prefs.savePreferences() ) {
            return DialogContext.closeDialog(true);
        }
        return null;
    }


    /**
     * @return outcome
     */
    public String resetAndClose () {
        this.prefs.loadPreferences();
        return DialogContext.closeDialog(null);
    }

}
