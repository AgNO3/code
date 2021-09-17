/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: May 20, 2017 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.prefs;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.runtime.jsf.prefs.UserPreferences;


/**
 * @author mbechler
 *
 */
@Named ( "expertPreferenceController" )
@ApplicationScoped
public class ExpertPreferencesController {

    @Inject
    private UserPreferences prefs;


    /**
     * 
     * @return outcome
     */
    public String saveGoHome () {
        if ( !this.prefs.savePreferences() ) {
            return null;
        }
        return "/index.xhtml?faces-redirect=true"; //$NON-NLS-1$
    }
}
