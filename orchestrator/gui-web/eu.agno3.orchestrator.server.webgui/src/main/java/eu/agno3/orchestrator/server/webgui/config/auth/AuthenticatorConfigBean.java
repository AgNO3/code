/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.07.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config.auth;


import java.util.Comparator;
import java.util.ResourceBundle;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.orchestrator.config.auth.AbstractAuthenticatorConfigImpl;
import eu.agno3.orchestrator.config.auth.AuthenticatorConfig;
import eu.agno3.orchestrator.config.auth.i18n.AuthenticationConfigurationMessages;
import eu.agno3.orchestrator.server.webgui.config.AbstractBaseConfigObjectBean;
import eu.agno3.orchestrator.server.webgui.config.ConfigLocalizationProvider;
import eu.agno3.orchestrator.server.webgui.config.ConfigUtil;


/**
 * @author mbechler
 * 
 */
@ApplicationScoped
@Named ( "authenticatorConfigBean" )
public class AuthenticatorConfigBean extends
        AbstractBaseConfigObjectBean<AuthenticatorConfig, AbstractAuthenticatorConfigImpl<? extends AuthenticatorConfig>> {

    @Inject
    private ConfigLocalizationProvider clp;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#getMessageBase()
     */
    @Override
    protected String getMessageBase () {
        return AuthenticationConfigurationMessages.BASE_PACKAGE;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#getObjectType()
     */
    @Override
    protected Class<AuthenticatorConfig> getObjectType () {
        return AuthenticatorConfig.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractBaseConfigObjectBean#cloneInternal(eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    protected void cloneInternal ( AbstractAuthenticatorConfigImpl<? extends AuthenticatorConfig> cloned, AuthenticatorConfig obj ) {
        cloned.setRealm(obj.getRealm());
        cloned.doClone(obj);
    }


    @Override
    public String labelForInternal ( AuthenticatorConfig ife ) {
        ResourceBundle acResourceBundle = this.getLocalizationBundle();

        String type = this.clp.getTypeName(ConfigUtil.getObjectTypeName(ife)) + " - "; //$NON-NLS-1$
        if ( ife.getRealm() == null ) {
            return type + acResourceBundle.getString("authenticator.label.noRealm"); //$NON-NLS-1$
        }

        return type + ife.getRealm();
    }


    public Comparator<AuthenticatorConfig> getComparator () {
        return new AuthenticatorComparator();
    }

}
