/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.07.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config.web;


import java.util.ResourceBundle;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.orchestrator.config.web.WebReverseProxySSLType;
import eu.agno3.orchestrator.config.web.WebReverseProxyType;
import eu.agno3.orchestrator.config.web.i18n.WebConfigurationMessages;
import eu.agno3.orchestrator.server.webgui.CoreServiceProvider;


/**
 * @author mbechler
 *
 */
@Named ( "webReverseProxyConfigBean" )
@ApplicationScoped
public class WebReverseProxyConfigBean {

    @Inject
    private CoreServiceProvider csp;


    public WebReverseProxyType[] getProxyTypes () {
        return WebReverseProxyType.values();
    }


    public String translateProxyType ( Object o ) {
        return translateEnumValue(WebReverseProxyType.class, o);
    }


    public WebReverseProxySSLType[] getProxySSLCiphersTypes () {
        return WebReverseProxySSLType.values();
    }


    public String translateProxySSLCiphersType ( Object o ) {
        return translateEnumValue(WebReverseProxySSLType.class, o);
    }


    public boolean isNoneType ( Object o ) {
        return o == WebReverseProxyType.NONE;
    }


    public boolean isCustomType ( Object o ) {
        return o == WebReverseProxyType.CUSTOM;
    }


    public boolean isNoneSSLType ( Object o ) {
        return o == WebReverseProxySSLType.NONE;
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
        return this.csp.getLocalizationService().getBundle(
            WebConfigurationMessages.BASE_PACKAGE,
            FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }

}
