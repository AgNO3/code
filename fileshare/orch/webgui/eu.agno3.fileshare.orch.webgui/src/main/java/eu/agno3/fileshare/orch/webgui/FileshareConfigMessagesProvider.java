/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.07.2015 by mbechler
 */
package eu.agno3.fileshare.orch.webgui;


import java.util.ResourceBundle;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.fileshare.orch.common.i18n.FileshareConfigurationMessages;
import eu.agno3.orchestrator.server.webgui.CoreServiceProvider;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "fs_cfgmsgs" )
public class FileshareConfigMessagesProvider {

    @Inject
    private CoreServiceProvider csp;


    /**
     * @return config resource bundle
     */
    public ResourceBundle getBundle () {
        return this.csp.getLocalizationService().getBundle(
            FileshareConfigurationMessages.BASE_PACKAGE,
            FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }
}
