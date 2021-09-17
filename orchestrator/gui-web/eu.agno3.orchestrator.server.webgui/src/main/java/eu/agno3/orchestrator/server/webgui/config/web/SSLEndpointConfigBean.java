/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.07.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config.web;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.NavigationCase;
import javax.faces.application.NavigationHandler;
import javax.faces.application.NavigationHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.render.ResponseStateManager;
import javax.inject.Inject;
import javax.inject.Named;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.myfaces.shared.util.ViewProtectionUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;

import eu.agno3.orchestrator.config.crypto.keystore.ImportKeyPairEntry;
import eu.agno3.orchestrator.config.crypto.keystore.KeystoreConfig;
import eu.agno3.orchestrator.config.crypto.truststore.TruststoreConfig;
import eu.agno3.orchestrator.config.hostconfig.HostConfiguration;
import eu.agno3.orchestrator.config.web.PublicKeyPinMode;
import eu.agno3.orchestrator.config.web.SSLEndpointConfiguration;
import eu.agno3.orchestrator.config.web.SSLEndpointConfigurationMutable;
import eu.agno3.orchestrator.config.web.SSLSecurityMode;
import eu.agno3.orchestrator.config.web.i18n.WebConfigurationMessages;
import eu.agno3.orchestrator.server.webgui.CoreServiceProvider;
import eu.agno3.orchestrator.server.webgui.components.AbstractObjectEditor;
import eu.agno3.orchestrator.server.webgui.components.OuterWrapper;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.hostconfig.EffectiveHostConfigProvider;
import eu.agno3.orchestrator.server.webgui.structure.StructureCacheBean;
import eu.agno3.orchestrator.server.webgui.util.completer.Completer;
import eu.agno3.orchestrator.server.webgui.util.completer.EmptyCompleter;
import eu.agno3.runtime.crypto.tls.TLSCipherComparator;
import eu.agno3.runtime.crypto.tls.TLSProtocolComparator;
import eu.agno3.runtime.jsf.view.stacking.ViewStackException;


/**
 * @author mbechler
 *
 */
@Named ( "sslEndpointConfigBean" )
@ApplicationScoped
public class SSLEndpointConfigBean {

    private static final Logger log = Logger.getLogger(SSLEndpointConfigBean.class);
    private static List<String> SUPPORTED_PROTOCOLS;
    private static List<String> SUPPORTED_CIPHERS;

    @Inject
    private CoreServiceProvider csp;

    @Inject
    private EffectiveHostConfigProvider hcprov;

    @Inject
    private StructureCacheBean structureCache;


    static {
        try {
            SSLContext instance = SSLContext.getInstance("TLSv1.2");//$NON-NLS-1$
            instance.init(new KeyManager[] {}, new TrustManager[] {}, new SecureRandom());
            SSLParameters supportedSSLParameters = instance.getSupportedSSLParameters();

            SSLEndpointConfigBean.SUPPORTED_CIPHERS = new ArrayList<>(Arrays.asList(supportedSSLParameters.getCipherSuites()));

            Iterator<String> it = SSLEndpointConfigBean.SUPPORTED_CIPHERS.iterator();

            while ( it.hasNext() ) {
                String cipher = it.next();
                if ( cipher.contains("_DSS_") || //$NON-NLS-1$
                        cipher.contains("_ECDSA_") || //$NON-NLS-1$
                        cipher.contains("_anon_") || //$NON-NLS-1$
                        cipher.contains("_KRB5_") || //$NON-NLS-1$
                        cipher.contains("_ECDH_") || //$NON-NLS-1$
                        cipher.contains("_NULL_") //$NON-NLS-1$
                ) {
                    it.remove();
                }
            }

            Collections.sort(SSLEndpointConfigBean.SUPPORTED_CIPHERS, new TLSCipherComparator());

            SSLEndpointConfigBean.SUPPORTED_PROTOCOLS = Arrays.asList(supportedSSLParameters.getProtocols());
            Collections.sort(SSLEndpointConfigBean.SUPPORTED_PROTOCOLS, new TLSProtocolComparator());
        }
        catch (
            NoSuchAlgorithmException |
            KeyManagementException e ) {
            log.error("Failed to get supported ssl parameters", e); //$NON-NLS-1$
        }
    }


    /**
     * @return the sUPPORTED_CIPHERS
     */
    public List<String> getSupportedCiphers () {
        return SUPPORTED_CIPHERS;
    }


    /**
     * @return the sUPPORTED_PROTOCOLS
     */
    public List<String> getSupportedProtocols () {
        return SUPPORTED_PROTOCOLS;
    }


    public ResourceBundle getLocalizationBundle () {
        return this.csp.getLocalizationService()
                .getBundle(WebConfigurationMessages.BASE_PACKAGE, FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }


    public SSLSecurityMode[] getSecurityModes () {
        return SSLSecurityMode.values();
    }


    public String translateSecurityMode ( Object tsm ) {
        return translateEnumValue(SSLSecurityMode.class, tsm);
    }


    public PublicKeyPinMode[] getPublicKeyPinModes () {
        return PublicKeyPinMode.values();
    }


    public String translatePublicKeyPinMode ( Object pkpm ) {
        return translateEnumValue(PublicKeyPinMode.class, pkpm);
    }


    public String translateCipher ( String cipher ) {
        String translated = cipher.substring(4);
        translated = translated.replace("_WITH", StringUtils.EMPTY); //$NON-NLS-1$
        translated = translated.replace('_', '/');
        return translated;
    }


    public boolean isGeneratedAlias ( String alias ) {
        if ( StringUtils.isBlank(alias) ) {
            return false;
        }

        return alias.charAt(0) == '_';
    }


    public String getKeyViewUrl ( OuterWrapper<?> outerWrapper ) {
        String keystore = getKeystoreAlias(outerWrapper);
        String keyAlias = getKeyAlias(outerWrapper);

        if ( keystore == null || keyAlias == null ) {
            return null;
        }

        UUID anchorId;
        try {
            anchorId = this.structureCache.getParentFor(outerWrapper.getContext().getAnchor()).getId();
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return "about:blank"; //$NON-NLS-1$
        }

        FacesContext fc = FacesContext.getCurrentInstance();

        String outcome = "/resourceLibrary/view/truststore.xhtml"; //$NON-NLS-1$
        NavigationCase navigationCase = getNavigationHandler(fc).getNavigationCase(fc, null, outcome);

        if ( navigationCase == null ) {
            log.warn("Failed to locate view for outcome " + outcome); //$NON-NLS-1$
            return null;
        }

        String viewId = navigationCase.getToViewId(fc);
        String protParam = StringUtils.EMPTY;
        if ( ViewProtectionUtils.isViewProtected(fc, viewId) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("View is protected " + viewId); //$NON-NLS-1$
            }
            protParam = String.format(
                "&%s=%s", //$NON-NLS-1$
                ResponseStateManager.NON_POSTBACK_VIEW_TOKEN_PARAM,
                fc.getRenderKit().getResponseStateManager().getCryptographicallyStrongTokenFromSession(fc));
        }

        String ctx = ( (ServletContext) fc.getExternalContext().getContext() ).getContextPath();
        return String.format(
            "%s/crypto/keystores/viewKey.xhtml?at=%s&keystore=%s&key=%s%s", //$NON-NLS-1$
            ctx,
            anchorId.toString(),
            keystore,
            keyAlias,
            protParam);
    }


    /**
     * @param outerWrapper
     * @return
     */
    private static String getKeyAlias ( OuterWrapper<?> outerWrapper ) {
        @SuppressWarnings ( "unchecked" )
        AbstractObjectEditor<SSLEndpointConfiguration> editor = (AbstractObjectEditor<SSLEndpointConfiguration>) outerWrapper.getEditor();
        try {
            String ka = editor.getCurrent().getKeyAlias();
            if ( !StringUtils.isBlank(ka) ) {
                return ka;
            }
            return editor.getDefaults().getKeyAlias();
        }
        catch ( Exception e ) {
            return null;
        }
    }


    /**
     * @param outerWrapper
     * @return
     */
    private static String getKeystoreAlias ( OuterWrapper<?> outerWrapper ) {
        @SuppressWarnings ( "unchecked" )
        AbstractObjectEditor<SSLEndpointConfiguration> editor = (AbstractObjectEditor<SSLEndpointConfiguration>) outerWrapper.getEditor();
        try {
            String ka = editor.getCurrent().getKeystoreAlias();
            if ( !StringUtils.isBlank(ka) ) {
                return ka;
            }
            return editor.getDefaults().getKeystoreAlias();
        }
        catch ( Exception e ) {
            return null;
        }
    }


    private static ConfigurableNavigationHandler getNavigationHandler ( FacesContext context ) {
        NavigationHandler navHandler = context.getApplication().getNavigationHandler();

        while ( navHandler instanceof NavigationHandlerWrapper ) {
            navHandler = ( (NavigationHandlerWrapper) navHandler ).getWrapped();
        }

        if ( ! ( navHandler instanceof ConfigurableNavigationHandler ) ) {
            throw new ViewStackException("Failed to locate proper NavigationHandler"); //$NON-NLS-1$
        }

        return (ConfigurableNavigationHandler) navHandler;
    }


    @SuppressWarnings ( "unchecked" )
    public Completer<String> getKeystoreCompleter ( OuterWrapper<?> outer ) {
        HostConfiguration hc = this.hcprov.getEffectiveHostConfiguration();
        if ( hc == null || hc.getKeystoreConfiguration() == null || hc.getKeystoreConfiguration().getKeystores() == null ) {
            return new EmptyCompleter();
        }

        final Set<KeystoreConfig> keystores = hc.getKeystoreConfiguration().getKeystores();
        return new Completer<String>() {

            @Override
            public List<String> complete ( String query ) {
                List<String> res = new LinkedList<>();
                for ( KeystoreConfig e : keystores ) {
                    String alias = e.getAlias();
                    if ( alias != null && !alias.isEmpty() && alias.startsWith(query) ) {
                        res.add(alias);
                    }
                }
                return res;
            }
        };
    }


    @SuppressWarnings ( "unchecked" )
    public Completer<String> getTruststoreCompleter ( OuterWrapper<?> outer ) {
        HostConfiguration hc = this.hcprov.getEffectiveHostConfiguration();
        if ( hc == null || hc.getTrustConfiguration() == null || hc.getTrustConfiguration().getTruststores() == null ) {
            return new EmptyCompleter();
        }

        final Set<TruststoreConfig> keystores = hc.getTrustConfiguration().getTruststores();
        return new Completer<String>() {

            @Override
            public List<String> complete ( String query ) {
                List<String> res = new LinkedList<>();
                for ( TruststoreConfig e : keystores ) {
                    String alias = e.getAlias();
                    if ( alias != null && !alias.isEmpty() && alias.startsWith(query) ) {
                        res.add(alias);
                    }
                }
                return res;
            }
        };
    }


    @SuppressWarnings ( "unchecked" )
    public Completer<String> getKeyAliasCompleter ( OuterWrapper<?> outer ) {
        HostConfiguration hc = this.hcprov.getEffectiveHostConfiguration();
        if ( hc == null || hc.getKeystoreConfiguration() == null || hc.getKeystoreConfiguration().getKeystores() == null ) {
            return new EmptyCompleter();
        }

        final String selectedKeyStore = getSelectedKeystore(outer);
        final Set<KeystoreConfig> keystores = hc.getKeystoreConfiguration().getKeystores();
        return new Completer<String>() {

            @Override
            public List<String> complete ( String query ) {
                List<String> res = new LinkedList<>();

                for ( KeystoreConfig e : keystores ) {
                    if ( selectedKeyStore != null && !selectedKeyStore.equals(e.getAlias()) ) {
                        continue;
                    }

                    for ( ImportKeyPairEntry importKeyPairEntry : e.getImportKeyPairs() ) {
                        String keyAlias = importKeyPairEntry.getAlias();

                        if ( keyAlias != null && !keyAlias.isEmpty() && keyAlias.startsWith(query) ) {
                            res.add(keyAlias);
                        }
                    }
                }
                return res;
            }
        };
    }


    public ManageReturnListener getManageReturnListener ( OuterWrapper<?> outer ) {
        return new ManageReturnListener(outer);
    }

    public static class ManageReturnListener {

        private OuterWrapper<?> outer;


        /**
         * @param outer
         */
        public ManageReturnListener ( OuterWrapper<?> outer ) {
            this.outer = outer;
        }


        public void returnListener ( SelectEvent ev ) {

            if ( this.outer == null || ! ( ev.getObject() instanceof String ) ) {
                return;
            }
            String selected = (String) ev.getObject();

            int sep = selected.indexOf(':');
            if ( sep < 0 ) {
                return;
            }

            String selectKs = selected.substring(0, sep);
            String selectKey = selected.substring(sep + 1);
            String curKs = getSelectedKeystore(this.outer);
            String curKey = getSelectedKeyAlias(this.outer);

            SSLEndpointConfigurationMutable epcfg;
            try {
                Object current = this.outer.getEditor().getCurrent();
                if ( ! ( current instanceof SSLEndpointConfigurationMutable ) ) {
                    return;
                }
                epcfg = (SSLEndpointConfigurationMutable) current;
            }
            catch ( Exception e ) {
                ExceptionHandler.handle(e);
                return;
            }

            boolean dirty = false;
            if ( !Objects.equals(curKs, selectKs) ) {
                epcfg.setKeystoreAlias(selectKs);
                dirty = true;
            }

            if ( !Objects.equals(curKey, selectKey) ) {
                epcfg.setKeyAlias(selectKey);
                dirty = true;
            }

            RequestContext.getCurrentInstance().addCallbackParam("dirty", dirty); //$NON-NLS-1$
        }

    }


    /**
     * @param outer
     * @return the selected keystore
     */
    public static String getSelectedKeystore ( OuterWrapper<?> outer ) {
        String keyStore = null;
        try {
            Object current = outer.getEditor().getCurrent();
            if ( current instanceof SSLEndpointConfiguration ) {
                keyStore = ( (SSLEndpointConfiguration) current ).getKeystoreAlias();
            }

            if ( keyStore == null ) {
                Object effective = outer.getEditor().getEffective();
                if ( effective instanceof SSLEndpointConfiguration ) {
                    keyStore = ( (SSLEndpointConfiguration) effective ).getKeystoreAlias();
                }
            }
        }
        catch ( Exception e1 ) {
            log.debug("Failed to get keystore", e1); //$NON-NLS-1$
        }

        if ( keyStore != null && log.isDebugEnabled() ) {
            log.debug("Keystore is set to " + keyStore); //$NON-NLS-1$
        }

        return keyStore;
    }


    public static String getSelectedKeyAlias ( OuterWrapper<?> outer ) {
        String keyalias = null;
        try {
            Object current = outer.getEditor().getCurrent();
            if ( current instanceof SSLEndpointConfiguration ) {
                keyalias = ( (SSLEndpointConfiguration) current ).getKeyAlias();
            }

            if ( keyalias == null ) {
                Object effective = outer.getEditor().getEffective();
                if ( effective instanceof SSLEndpointConfiguration ) {
                    keyalias = ( (SSLEndpointConfiguration) effective ).getKeyAlias();
                }
            }
        }
        catch ( Exception e1 ) {
            log.debug("Failed to get key alias", e1); //$NON-NLS-1$
        }

        if ( keyalias != null && log.isDebugEnabled() ) {
            log.debug("Keystore is set to " + keyalias); //$NON-NLS-1$
        }

        return keyalias;
    }


    public static String makeManageParams ( OuterWrapper<?> outer ) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        String cs = "UTF-8"; //$NON-NLS-1$
        sb.append("selectKeystore="); //$NON-NLS-1$
        sb.append(URLEncoder.encode(getSelectedKeystore(outer), cs));
        sb.append('&');
        sb.append("selectKey="); //$NON-NLS-1$
        sb.append(getSelectedKeyAlias(outer));
        sb.append('&');

        Object param = outer.getParameter("sslConfigContext"); //$NON-NLS-1$ #
        if ( param instanceof SSLConfigContext ) {
            SSLConfigContext ctx = (SSLConfigContext) param;
            String subject = ctx.getSubject(outer);
            if ( !StringUtils.isBlank(subject) ) {
                sb.append("suggestSubject="); //$NON-NLS-1$
                sb.append(URLEncoder.encode(subject, cs));
                sb.append('&');
            }

            List<String> sans = ctx.getSANs(outer);
            if ( sans != null && !sans.isEmpty() ) {
                sb.append("suggestSANs="); //$NON-NLS-1$
                sb.append(URLEncoder.encode(StringUtils.join(sans, ','), cs));
                sb.append('&');
            }

            Set<String> ekus = ctx.getEKUs(outer);
            if ( ekus != null ) {
                sb.append("suggestEKUs="); //$NON-NLS-1$
                sb.append(URLEncoder.encode(StringUtils.join(ekus, ','), cs));
                sb.append('&');
            }

            Set<String> kus = ctx.getKeyUsage(outer);
            if ( kus != null ) {
                sb.append("suggestKeyUsage="); //$NON-NLS-1$
                sb.append(URLEncoder.encode(StringUtils.join(ekus, ','), cs));
                sb.append('&');
            }
        }

        if ( sb.charAt(sb.length() - 1) == '&' ) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
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


    /**
     * 
     * @return protocol comparator
     */
    public Comparator<String> getProtocolComparator () {
        return new TLSProtocolComparator();
    }


    public String createProtocol () {
        return StringUtils.EMPTY;
    }
}
