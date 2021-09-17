/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.02.2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.orchestrator;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.orchestrator.config.orchestrator.OrchestratorWebConfiguration;
import eu.agno3.orchestrator.server.webgui.components.OuterWrapper;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;


/**
 * @author mbechler
 *
 */
@Named ( "orch_webConfigBean" )
@ApplicationScoped
public class OrchestratorWebConfigBean {

    /**
     * @param outer
     * @return the selected theme library
     */
    public String getSelectedThemeLibrary ( OuterWrapper<?> outer ) {
        OuterWrapper<?> outerWrapper = outer.get("urn:agno3:objects:1.0:orchestrator:web"); //$NON-NLS-1$
        if ( outerWrapper == null ) {
            return null;
        }

        try {
            Object cur = outerWrapper.getEditor().getCurrent();
            if ( cur instanceof OrchestratorWebConfiguration ) {
                if ( !StringUtils.isBlank( ( (OrchestratorWebConfiguration) cur ).getThemeLibrary()) ) {
                    return ( (OrchestratorWebConfiguration) cur ).getThemeLibrary();
                }
            }

            Object def = outerWrapper.getEditor().getDefaults();
            if ( def instanceof OrchestratorWebConfiguration ) {
                if ( !StringUtils.isBlank( ( (OrchestratorWebConfiguration) def ).getThemeLibrary()) ) {
                    return ( (OrchestratorWebConfiguration) def ).getThemeLibrary();
                }
            }
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }
}
