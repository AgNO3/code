/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.07.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config.web;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import eu.agno3.orchestrator.config.web.ICAPConfiguration;
import eu.agno3.orchestrator.config.web.SSLClientMode;
import eu.agno3.orchestrator.server.webgui.components.AbstractObjectEditor;
import eu.agno3.orchestrator.server.webgui.components.OuterWrapper;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;


/**
 * @author mbechler
 *
 */
@Named ( "icapClientConfigBean" )
@ApplicationScoped
public class ICAPClientConfigBean {

    /**
     * 
     * @param outer
     * @return the current default port
     */
    public String getDefaultPort ( OuterWrapper<?> outer ) {
        if ( isSSL(outer) ) {
            return "11344"; //$NON-NLS-1$
        }
        return "1344"; //$NON-NLS-1$
    }


    /**
     * 
     * @param outer
     * @return the current scheme
     */
    public String getScheme ( OuterWrapper<?> outer ) {

        if ( isSSL(outer) ) {
            return "icaps"; //$NON-NLS-1$
        }

        return "icap"; //$NON-NLS-1$
    }


    /**
     * @param editor
     * @return
     */
    private static boolean isSSL ( OuterWrapper<?> outer ) {
        AbstractObjectEditor<?> editor = outer.getEditor();
        try {
            if ( editor != null && editor.getCurrent() != null ) {
                ICAPConfiguration cfg = (ICAPConfiguration) editor.getCurrent();
                if ( cfg.getSslClientMode() != null && cfg.getSslClientMode() == SSLClientMode.SSL ) {
                    return true;
                }
            }

            if ( editor != null && editor.getDefaults() != null ) {
                ICAPConfiguration cfg = (ICAPConfiguration) editor.getCurrent();
                if ( cfg.getSslClientMode() != null && cfg.getSslClientMode() == SSLClientMode.SSL ) {
                    return true;
                }
            }
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return false;
    }

}
