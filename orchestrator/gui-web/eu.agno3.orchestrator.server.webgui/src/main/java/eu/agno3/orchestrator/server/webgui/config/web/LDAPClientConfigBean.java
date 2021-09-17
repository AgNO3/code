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

import eu.agno3.orchestrator.config.web.LDAPConfiguration;
import eu.agno3.orchestrator.config.web.LDAPSearchScope;
import eu.agno3.orchestrator.config.web.SASLQOP;
import eu.agno3.orchestrator.config.web.SSLClientMode;
import eu.agno3.orchestrator.config.web.i18n.WebConfigurationMessages;
import eu.agno3.orchestrator.server.webgui.CoreServiceProvider;
import eu.agno3.orchestrator.server.webgui.components.AbstractObjectEditor;
import eu.agno3.orchestrator.server.webgui.components.OuterWrapper;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.runtime.ldap.client.LDAPConfigUtil;


/**
 * @author mbechler
 *
 */
@Named ( "ldapClientConfigBean" )
@ApplicationScoped
public class LDAPClientConfigBean {

    @Inject
    private CoreServiceProvider csp;


    /**
     * 
     * @param outer
     * @return the current default port
     */
    public String getDefaultPort ( OuterWrapper<?> outer ) {
        if ( isSSL(outer) ) {
            return "636"; //$NON-NLS-1$
        }
        return "389"; //$NON-NLS-1$
    }


    /**
     * 
     * @param outer
     * @return the current scheme
     */
    public String getScheme ( OuterWrapper<?> outer ) {
        if ( isSSL(outer) ) {
            return "ldaps"; //$NON-NLS-1$
        }
        return "ldap"; //$NON-NLS-1$
    }


    public LDAPSearchScope[] getSearchScopes () {
        return LDAPSearchScope.values();
    }


    public String translateSearchScope ( Object val ) {
        return translateEnumValue(LDAPSearchScope.class, val);
    }


    public SASLQOP[] getSaslQOPs () {
        return SASLQOP.values();
    }


    public String translateSaslQOP ( Object val ) {
        return translateEnumValue(SASLQOP.class, val);
    }


    public String[] getSaslMechanisms () {
        return LDAPConfigUtil.getSupportedSASLMechanisms();
    }


    public boolean mechanismSupportsQOP ( String mech ) {
        return LDAPConfigUtil.mechanismSupportsQOP(mech);
    }


    /**
     * @param editor
     * @return
     */
    private static boolean isSSL ( OuterWrapper<?> outer ) {
        AbstractObjectEditor<?> editor = outer.getEditor();
        try {
            if ( editor != null && editor.getCurrent() != null ) {
                LDAPConfiguration cfg = (LDAPConfiguration) editor.getCurrent();
                if ( cfg.getSslClientMode() != null && cfg.getSslClientMode() == SSLClientMode.SSL ) {
                    return true;
                }
            }

            if ( editor != null && editor.getDefaults() != null ) {
                LDAPConfiguration cfg = (LDAPConfiguration) editor.getCurrent();
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
