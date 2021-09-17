/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 24, 2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config.auth;


import java.io.Serializable;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.auth.UserPasswordAuthTestParams;
import eu.agno3.orchestrator.config.auth.ldap.LDAPAuthenticatorConfig;
import eu.agno3.orchestrator.config.auth.ldap.LDAPAuthenticatorConfigObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.service.ConfigTestService;
import eu.agno3.orchestrator.config.web.SSLClientConfiguration;
import eu.agno3.orchestrator.config.web.SSLClientConfigurationMutable;
import eu.agno3.orchestrator.server.webgui.components.ObjectEditor;
import eu.agno3.orchestrator.server.webgui.components.OuterWrapper;
import eu.agno3.orchestrator.server.webgui.config.ConfigContext;
import eu.agno3.orchestrator.server.webgui.config.test.ConfigTestBean;
import eu.agno3.orchestrator.server.webgui.config.test.ConfigTestInteraction;
import eu.agno3.orchestrator.server.webgui.config.test.ConfigTestReturnHandler;
import eu.agno3.orchestrator.server.webgui.config.test.TrustCertificateInteraction;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.types.entities.crypto.PublicKeyEntry;


/**
 * @author mbechler
 *
 */
@Named ( "auth_authenticator_ldap_testBean" )
@ViewScoped
public class LDAPAuthConfigTestBean implements Serializable, ConfigTestReturnHandler {

    /**
     * 
     */
    private static final long serialVersionUID = 8079146099548035795L;

    private static final Logger log = Logger.getLogger(LDAPAuthConfigTestBean.class);

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private ConfigTestBean configTest;

    private String username;

    private String password;


    /**
     * @return the username
     */
    public String getUsername () {
        return this.username;
    }


    /**
     * @param username
     *            the username to set
     */
    public void setUsername ( String username ) {
        this.username = username;
    }


    /**
     * @return the password
     */
    public String getPassword () {
        return this.password;
    }


    /**
     * @param password
     *            the password to set
     */
    public void setPassword ( String password ) {
        this.password = password;
    }


    public String test ( ConfigContext<ConfigurationObject, ?> ctx ) {
        try {
            ConfigurationObject curCfg = ctx.getCurrent();
            UserPasswordAuthTestParams tp = new UserPasswordAuthTestParams();
            tp.setUsername(this.getUsername());
            tp.setPassword(this.getPassword());
            this.configTest.setResults(
                this.ssp.getService(ConfigTestService.class)
                        .test(ctx.getAnchor(), curCfg, this.configTest.getObjectType(), this.configTest.getObjectPath(), tp));
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }

        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.test.ConfigTestReturnHandler#interact(eu.agno3.orchestrator.server.webgui.components.OuterWrapper,
     *      eu.agno3.orchestrator.server.webgui.config.test.ConfigTestInteraction)
     */
    @Override
    public void interact ( OuterWrapper<?> ctx, ConfigTestInteraction inter ) {

        if ( ctx == null ) {
            log.debug("No context"); //$NON-NLS-1$
            return;
        }

        if ( inter instanceof TrustCertificateInteraction ) {
            TrustCertificateInteraction tci = (TrustCertificateInteraction) inter;
            X509Certificate certificate = tci.getCertificate();
            if ( log.isDebugEnabled() ) {
                log.debug("Should trust certificate " + certificate.getSubjectDN()); //$NON-NLS-1$
            }

            OuterWrapper<?> ldapactx = ctx.get(LDAPAuthenticatorConfigObjectTypeDescriptor.TYPE_NAME);
            if ( ldapactx == null || ! ( ldapactx.getEditor() instanceof ObjectEditor ) ) {
                log.debug("Invalid context"); //$NON-NLS-1$
                return;
            }

            ObjectEditor editor = (ObjectEditor) ldapactx.getEditor();

            try {
                Object cfg = editor.getCurrent();

                if ( ! ( cfg instanceof LDAPAuthenticatorConfig ) ) {
                    return;
                }

                LDAPAuthenticatorConfig lcf = (LDAPAuthenticatorConfig) cfg;
                SSLClientConfiguration sslConfig = lcf.getConnectionConfig().getSslClientConfiguration();
                if ( sslConfig == null ) {
                    return;
                }

                Set<PublicKeyEntry> pk = sslConfig.getPinnedPublicKeys();

                if ( pk == null ) {
                    pk = new HashSet<>();
                    ( (SSLClientConfigurationMutable) sslConfig ).setPinnedPublicKeys(pk);
                }

                pk.add(new PublicKeyEntry(certificate.getPublicKey(), String.format(
                    "%s (0x%s)", //$NON-NLS-1$
                    certificate.getSubjectX500Principal().getName(),
                    certificate.getSerialNumber().toString(16))));

                // need to reset so that the collection will be updated in the UI
                editor.resetComponent();
            }
            catch ( Exception e ) {
                ExceptionHandler.handle(e);
                return;
            }

        }
        else if ( log.isDebugEnabled() ) {
            log.debug("Unsupported interaction " + inter.getClass().getName()); //$NON-NLS-1$
        }

    }
}
