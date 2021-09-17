/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.07.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config.web;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import eu.agno3.orchestrator.config.web.SMTPConfiguration;
import eu.agno3.orchestrator.config.web.SSLClientMode;
import eu.agno3.orchestrator.server.webgui.components.AbstractObjectEditor;
import eu.agno3.orchestrator.server.webgui.components.OuterWrapper;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;


/**
 * @author mbechler
 *
 */
@Named ( "smtpClientConfigBean" )
@ApplicationScoped
public class SMTPClientConfigBean {

    /**
     * 
     * @param outer
     * @return the current default port
     */
    public String getDefaultPort ( OuterWrapper<?> outer ) {
        if ( isSSL(outer) ) {
            return "465"; //$NON-NLS-1$
        }
        return "25"; //$NON-NLS-1$
    }


    /**
     * 
     * @param outer
     * @return the current scheme
     */
    public String getScheme ( OuterWrapper<?> outer ) {

        if ( isSSL(outer) ) {
            return "smtps"; //$NON-NLS-1$
        }

        return "smtp"; //$NON-NLS-1$
    }


    public String[] getAuthMechs () {
        return new String[] {
            "LOGIN", //$NON-NLS-1$
            "PLAIN", //$NON-NLS-1$
            "DIGEST-MD5" //$NON-NLS-1$
        };
    }


    public String translateAuthMech ( String mech ) {
        return mech;
    }


    /**
     * @param editor
     * @return
     */
    private static boolean isSSL ( OuterWrapper<?> outer ) {
        AbstractObjectEditor<?> editor = outer.getEditor();
        try {
            if ( editor != null && editor.getCurrent() != null ) {
                SMTPConfiguration cfg = (SMTPConfiguration) editor.getCurrent();
                if ( cfg.getSslClientMode() != null && cfg.getSslClientMode() == SSLClientMode.SSL ) {
                    return true;
                }
            }

            if ( editor != null && editor.getDefaults() != null ) {
                SMTPConfiguration cfg = (SMTPConfiguration) editor.getCurrent();
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
