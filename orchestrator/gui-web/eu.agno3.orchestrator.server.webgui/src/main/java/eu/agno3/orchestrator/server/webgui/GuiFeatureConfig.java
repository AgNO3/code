/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.12.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.orchestrator.server.webgui.prefs.UserPreferencesBean;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "guiFeatureConfig" )
public class GuiFeatureConfig {

    @Inject
    private UserPreferencesBean preferences;


    /**
     * 
     * @return whether the structure tree should be shown
     */
    public boolean getShowStructure () {
        return this.preferences.getEnableMultiHostManagement();
    }


    /**
     * 
     * @return whether to show developer tools
     */
    public boolean getShowDevTools () {
        return this.preferences.getEnableDeveloperMode();
    }


    /**
     * 
     * @return whether to enable the structural default facilities
     */
    public boolean getAllowDefaults () {
        return this.preferences.getEnableExperimentalFeatures();
    }


    /**
     * 
     * @return whether to enable the configuration enforcment facilities
     */
    public boolean getAllowEnforcements () {
        return this.preferences.getEnableExperimentalFeatures();
    }
}
