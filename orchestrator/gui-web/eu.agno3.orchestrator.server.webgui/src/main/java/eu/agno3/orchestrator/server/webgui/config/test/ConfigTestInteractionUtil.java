/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jan 15, 2017 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config.test;


import java.io.Serializable;
import java.security.cert.X509Certificate;
import java.util.Collections;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.component.UIComponent;
import javax.inject.Named;

import eu.agno3.orchestrator.server.webgui.components.AbstractObjectEditor;
import eu.agno3.orchestrator.server.webgui.components.OuterWrapper;
import eu.agno3.runtime.jsf.view.stacking.DialogContext;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "configTestInteractionUtil" )
public class ConfigTestInteractionUtil {

    public String trustCertificate ( X509Certificate cert ) {

        if ( cert == null ) {
            return null;
        }

        return DialogContext.closeDialog((Serializable) Collections.singletonList(new TrustCertificateInteraction(cert)));
    }


    public ConfigReturnWrapper makeReturnWrapper ( UIComponent comp, ConfigTestReturnHandler handler ) {
        return new ConfigReturnWrapper(resolveEditor(comp), handler);
    }


    /**
     * @param comp
     * @return next outerwrapper in component tree
     */
    public static OuterWrapper<?> resolveEditor ( UIComponent comp ) {
        UIComponent cur = comp;
        while ( cur != null ) {
            if ( cur instanceof AbstractObjectEditor<?> ) {
                return ( (AbstractObjectEditor<?>) cur ).getOuterWrapper();
            }
            cur = cur.getParent();
        }
        return null;
    }
}
