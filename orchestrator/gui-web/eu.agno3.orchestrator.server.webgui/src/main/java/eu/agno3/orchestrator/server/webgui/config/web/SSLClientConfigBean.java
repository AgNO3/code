/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.07.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config.web;


import java.util.ResourceBundle;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.orchestrator.config.web.SSLClientMode;
import eu.agno3.orchestrator.config.web.i18n.WebConfigurationMessages;
import eu.agno3.orchestrator.server.webgui.CoreServiceProvider;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "sslClientConfigBean" )
public class SSLClientConfigBean {

    @Inject
    private CoreServiceProvider csp;


    public SSLClientMode[] getClientModes () {
        // manually remove try startTLS as this is nonsense
        return new SSLClientMode[] {
            SSLClientMode.DISABLE, SSLClientMode.SSL, SSLClientMode.REQUIRE_STARTTLS
        };
    }


    public String translateClientMode ( Object tsm ) {
        return translateEnumValue(SSLClientMode.class, tsm);
    }


    public <TEnum extends Enum<TEnum>> String translateEnumValue ( Class<TEnum> en, Object val ) {
        if ( val == null || !en.isAssignableFrom(val.getClass()) ) {
            return null;
        }
        @SuppressWarnings ( "unchecked" )
        TEnum enumVal = (TEnum) val;
        StringBuilder key = new StringBuilder();
        key.append(en.getSimpleName());
        key.append('.');
        key.append(enumVal.name());
        return this.getLocalizationBundle().getString(key.toString());
    }


    public ResourceBundle getLocalizationBundle () {
        return this.csp.getLocalizationService()
                .getBundle(WebConfigurationMessages.BASE_PACKAGE, FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }
}
