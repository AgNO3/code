/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.02.2016 by mbechler
 */
package eu.agno3.fileshare.orch.webgui.config;


import java.lang.reflect.UndeclaredThrowableException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.fileshare.orch.common.config.FileshareWebConfig;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.components.OuterWrapper;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;


/**
 * @author mbechler
 *
 */
@Named ( "fs_webConfigBean" )
@ApplicationScoped
public class FileshareWebConfigBean {

    /**
     * @param outer
     * @return the selected theme library
     */
    public String getSelectedThemeLibrary ( OuterWrapper<?> outer ) {
        OuterWrapper<?> outerWrapper = outer.get("urn:agno3:objects:1.0:fileshare:web"); //$NON-NLS-1$
        if ( outerWrapper == null ) {
            return null;
        }

        try {
            Object cur = outerWrapper.getEditor().getCurrent();
            if ( cur instanceof FileshareWebConfig ) {
                if ( !StringUtils.isBlank( ( (FileshareWebConfig) cur ).getThemeLibrary()) ) {
                    return ( (FileshareWebConfig) cur ).getThemeLibrary();
                }
            }

            Object def = outerWrapper.getEditor().getDefaults();
            if ( def instanceof FileshareWebConfig ) {
                if ( !StringUtils.isBlank( ( (FileshareWebConfig) def ).getThemeLibrary()) ) {
                    return ( (FileshareWebConfig) def ).getThemeLibrary();
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
