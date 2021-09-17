/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.07.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config.auth;


import java.util.ResourceBundle;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.orchestrator.config.auth.i18n.AuthenticationConfigurationMessages;
import eu.agno3.orchestrator.config.auth.ldap.LDAPAuthenticatorConfig;
import eu.agno3.orchestrator.config.auth.ldap.LDAPAuthenticatorConfigObjectTypeDescriptor;
import eu.agno3.orchestrator.config.web.LDAPAuthType;
import eu.agno3.orchestrator.config.web.LDAPServerType;
import eu.agno3.orchestrator.server.webgui.CoreServiceProvider;
import eu.agno3.orchestrator.server.webgui.components.OuterWrapper;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;


/**
 * @author mbechler
 *
 */
@Named ( "ldapAuthConfigBean" )
@ApplicationScoped
public class LDAPAuthConfigBean {

    @Inject
    private CoreServiceProvider csp;


    /**
     * 
     * @return the server types
     */
    public LDAPServerType[] getServerTypes () {
        return LDAPServerType.values();
    }


    /**
     * 
     * @return the auth types
     */
    public LDAPAuthType[] getAuthTypes () {
        return LDAPAuthType.values();
    }


    /**
     * 
     * @param val
     * @return the translated server type
     */
    public String translateServerType ( Object val ) {
        return translateEnumValue(LDAPServerType.class, val);
    }


    /**
     * 
     * @param val
     * @return the translated auth type
     */
    public String translateAuthType ( Object val ) {
        return translateEnumValue(LDAPAuthType.class, val);
    }


    public static boolean isActiveDirectory ( OuterWrapper<?> outer ) {
        LDAPServerType type = getServerType(outer);
        if ( type == null ) {
            return false;
        }

        return type == LDAPServerType.AD;
    }


    public static LDAPServerType getServerType ( OuterWrapper<?> outer ) {
        if ( outer == null ) {
            return null;
        }
        OuterWrapper<?> outerWrapper = outer.get(LDAPAuthenticatorConfigObjectTypeDescriptor.TYPE_NAME);
        if ( outerWrapper == null ) {
            return null;
        }

        try {
            LDAPAuthenticatorConfig current = (LDAPAuthenticatorConfig) outerWrapper.getEditor().getCurrent();

            if ( current != null && current.getConnectionConfig() != null && current.getConnectionConfig().getServerType() != null ) {
                return current.getConnectionConfig().getServerType();
            }

            LDAPAuthenticatorConfig defaults = (LDAPAuthenticatorConfig) outerWrapper.getEditor().getDefaults();
            if ( defaults != null && defaults.getConnectionConfig() != null && defaults.getConnectionConfig().getServerType() != null ) {
                return defaults.getConnectionConfig().getServerType();
            }
            return null;
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return null;
        }

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
                .getBundle(AuthenticationConfigurationMessages.BASE_PACKAGE, FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }

}
