/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.07.2015 by mbechler
 */
package eu.agno3.fileshare.orch.webgui.config;


import java.lang.reflect.UndeclaredThrowableException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.fileshare.orch.common.config.FileshareConfiguration;
import eu.agno3.fileshare.orch.common.config.FileshareConfigurationObjectTypeDescriptor;
import eu.agno3.fileshare.orch.common.config.FileshareNotificationConfig;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.components.AbstractObjectEditor;
import eu.agno3.orchestrator.server.webgui.components.OuterWrapper;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;


/**
 * @author mbechler
 *
 */
@Named ( "fs_notificationConfigBean" )
@ApplicationScoped
public class NotificationConfigBean {

    /**
     * 
     * @param wr
     * @return whether mailing is enabled
     */
    public boolean isMailingEnabled ( OuterWrapper<?> wr ) {
        if ( wr == null ) {
            return true;
        }

        OuterWrapper<?> outerWrapper = wr.get(FileshareConfigurationObjectTypeDescriptor.TYPE_NAME);
        if ( outerWrapper == null ) {
            return true;
        }

        AbstractObjectEditor<?> editor = outerWrapper.getEditor();

        try {
            FileshareConfiguration current = (FileshareConfiguration) editor.getCurrent();

            if ( current == null || current.getNotificationConfiguration() == null
                    || current.getNotificationConfiguration().getNotificationDisabled() == null ) {

                FileshareConfiguration defaults = (FileshareConfiguration) editor.getDefaults();

                if ( defaults == null || defaults.getNotificationConfiguration() == null
                        || defaults.getNotificationConfiguration().getNotificationDisabled() == null ) {
                    return true;
                }
                return !defaults.getNotificationConfiguration().getNotificationDisabled();
            }

            return !current.getNotificationConfiguration().getNotificationDisabled();
        }
        catch (
            ModelObjectNotFoundException |
            ModelServiceException |
            GuiWebServiceException e ) {
            ExceptionHandler.handle(e);
            return true;
        }
    }


    /**
     * @param outer
     * @return the selected theme library
     */
    public String getSelectedTemplateLibrary ( OuterWrapper<?> outer ) {
        OuterWrapper<?> outerWrapper = outer.get("urn:agno3:objects:1.0:fileshare:notification"); //$NON-NLS-1$
        if ( outerWrapper == null ) {
            return null;
        }

        try {
            Object cur = outerWrapper.getEditor().getCurrent();
            if ( cur instanceof FileshareNotificationConfig ) {
                if ( !StringUtils.isBlank( ( (FileshareNotificationConfig) cur ).getTemplateLibrary()) ) {
                    return ( (FileshareNotificationConfig) cur ).getTemplateLibrary();
                }
            }

            Object def = outerWrapper.getEditor().getDefaults();
            if ( def instanceof FileshareNotificationConfig ) {
                if ( !StringUtils.isBlank( ( (FileshareNotificationConfig) def ).getTemplateLibrary()) ) {
                    return ( (FileshareNotificationConfig) def ).getTemplateLibrary();
                }
            }
        }
        catch (
            ModelObjectNotFoundException |
            ModelServiceException |
            UndeclaredThrowableException |
            GuiWebServiceException e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }
}
