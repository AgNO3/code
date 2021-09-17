/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 12, 2017 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config;


import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.realm.ConfigApplyChallenge;
import eu.agno3.orchestrator.server.webgui.CoreServiceProvider;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.runtime.i18n.I18NUtil;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "configChallengeBean" )
public class ConfigChallengeBean {

    private static final Logger log = Logger.getLogger(ConfigChallengeBean.class);

    @Inject
    private CoreServiceProvider csp;


    public String translateLabel ( ConfigApplyChallenge chlg ) {

        if ( chlg.getLabelTemplate() == null ) {
            return chlg.getKey();
        }

        try {
            if ( chlg.getMessageBase() == null ) {
                if ( chlg.getLabelArgs() == null ) {
                    return GuiMessages.get(chlg.getLabelTemplate());
                }
                return GuiMessages.format(chlg.getLabelTemplate(), chlg.getLabelArgs().toArray());
            }

            Locale l = FacesContext.getCurrentInstance().getViewRoot().getLocale();
            ResourceBundle bundle = this.csp.getLocalizationService().getBundle(chlg.getMessageBase(), l, this.getClass().getClassLoader());

            String msg = bundle.getString(chlg.getLabelTemplate());
            if ( chlg.getLabelArgs() == null ) {
                return msg;
            }
            return I18NUtil.format(msg, l, chlg.getLabelArgs().toArray());
        }
        catch ( MissingResourceException e ) {
            log.warn(String.format("Missing resource %s:%s", chlg.getMessageBase(), chlg.getLabelTemplate())); //$NON-NLS-1$
            return chlg.getLabelTemplate();
        }
    }
}
