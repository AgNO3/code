/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.12.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config;


import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.inject.Inject;

import eu.agno3.orchestrator.config.model.validation.ViolationEntry;
import eu.agno3.orchestrator.config.model.validation.ViolationLevel;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
public class ViolationMessageBuilder {

    @Inject
    private ConfigLocalizationProvider configLocalizer;


    /**
     * @param fieldLabel
     * @param v
     * @return a faces message for the given violation
     */
    public FacesMessage makeMessage ( String fieldLabel, ViolationEntry v ) {
        return new FacesMessage(toSeverity(v.getLevel()), fieldLabel, this.formatMessage(v));
    }


    /**
     * @param v
     * @return
     */
    private String formatMessage ( ViolationEntry v ) {
        return this.configLocalizer.getViolationMessage(v.getObjectType(), v.getMessageTemplate(), v.getMessageArgs());
    }


    /**
     * @param level
     * @return
     */
    private static Severity toSeverity ( ViolationLevel level ) {
        switch ( level ) {
        case ERROR:
            return FacesMessage.SEVERITY_ERROR;
        case WARNING:
            return FacesMessage.SEVERITY_WARN;
        case SUGGESTION:
            return FacesMessage.SEVERITY_INFO;
        default:
            return FacesMessage.SEVERITY_FATAL;
        }
    }
}
