/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.11.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.crypto.truststores;


import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
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
import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.myfaces.shared.util.ViewProtectionUtils;

import eu.agno3.orchestrator.config.crypto.i18n.CryptoConfigMessages;
import eu.agno3.orchestrator.config.crypto.truststore.TruststoreConfig;
import eu.agno3.orchestrator.config.crypto.truststore.TruststoreConfigImpl;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.components.AbstractObjectEditor;
import eu.agno3.orchestrator.server.webgui.components.MultiObjectEditor;
import eu.agno3.orchestrator.server.webgui.components.OuterWrapper;
import eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean;
import eu.agno3.orchestrator.server.webgui.config.ConfigContext;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.util.completer.Completer;
import eu.agno3.orchestrator.server.webgui.util.completer.EmptyCompleter;
import eu.agno3.orchestrator.types.entities.crypto.X509CertEntry;
import eu.agno3.runtime.jsf.view.stacking.ViewStackException;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "truststoreConfigBean" )
public class TruststoreConfigBean extends AbstractConfigObjectBean<TruststoreConfig, TruststoreConfigImpl> {

    private static final Logger log = Logger.getLogger(TruststoreConfigBean.class);
    /**
     * 
     */
    private static final String CLIENT_TRUSTSTORE = "client"; //$NON-NLS-1$

    @Inject
    private RevocationConfigBean revConfigBean;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#getMessageBase()
     */
    @Override
    protected String getMessageBase () {
        return CryptoConfigMessages.BASE;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#getObjectType()
     */
    @Override
    protected Class<TruststoreConfig> getObjectType () {
        return TruststoreConfig.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#getInstanceType()
     */
    @Override
    protected Class<TruststoreConfigImpl> getInstanceType () {
        return TruststoreConfigImpl.class;
    }


    public String getTrustLibrary ( OuterWrapper<?> outerWrapper ) {
        if ( outerWrapper == null ) {
            return null;
        }

        OuterWrapper<?> ow = outerWrapper.get("urn:agno3:objects:1.0:crypto:truststores:truststore"); //$NON-NLS-1$
        if ( ow == null ) {
            return null;
        }

        AbstractObjectEditor<?> editor = ow.getEditor();
        if ( editor == null ) {
            return null;
        }

        try {
            Object current = ( editor instanceof MultiObjectEditor ) ? ( (MultiObjectEditor) editor ).getSelectedObject() : editor.getCurrent();
            if ( current instanceof TruststoreConfig ) {
                String tl = ( (TruststoreConfig) current ).getTrustLibrary();
                if ( !StringUtils.isBlank(tl) ) {
                    return tl;
                }
            }

            Object def = ( editor instanceof MultiObjectEditor ) ? ( (MultiObjectEditor) editor ).getSelectedObjectDefaults() : editor.getDefaults();
            if ( def instanceof TruststoreConfig ) {
                String tl = ( (TruststoreConfig) def ).getTrustLibrary();
                if ( !StringUtils.isBlank(tl) ) {
                    return tl;
                }
            }

        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }


    public String getTruststoreViewUrl ( OuterWrapper<?> outerWrapper ) {
        String trustLibrary = getTrustLibrary(outerWrapper);
        UUID anchorId;
        try {
            anchorId = outerWrapper.getContext().getAnchor().getId();
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
            "%s/resourceLibrary/view/truststore.xhtml?at=%s&name=%s&type=truststore%s", //$NON-NLS-1$
            ctx,
            anchorId.toString(),
            trustLibrary,
            protParam);
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


    /**
     * {@inheritDoc}
     * 
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#cloneInternal(eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    protected void cloneInternal ( ConfigContext<?, ?> ctx, TruststoreConfigImpl cloned, TruststoreConfig obj, TruststoreConfig defaults )
            throws ModelServiceException, GuiWebServiceException {
        cloned.setAlias(obj.getAlias());
        super.cloneDefault(ctx, cloned, obj, defaults);
        cloned.setRevocationConfiguration(this.revConfigBean.cloneObject(ctx, obj.getRevocationConfiguration()));
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#labelForInternal(eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    protected String labelForInternal ( TruststoreConfig obj ) {
        return obj.getAlias();
    }


    public Comparator<TruststoreConfig> getComparator () {
        return new TruststoreComparator();
    }


    public X509CertEntry makeTrustedCertEntry () {
        return new X509CertEntry();
    }


    @SuppressWarnings ( "unchecked" )
    public Completer<String> getTruststoreCompleter ( OuterWrapper<?> outer )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {

        AbstractObjectEditor<?> truststoreConfig = outer.resolve(
            "urn:agno3:objects:1.0:hostconfig", //$NON-NLS-1$
            "trustConfiguration/truststores");//$NON-NLS-1$

        if ( truststoreConfig == null ) {
            return new EmptyCompleter();
        }

        final List<TruststoreConfig> truststores = (List<TruststoreConfig>) truststoreConfig.getEffective();

        return new Completer<String>() {

            @Override
            public List<String> complete ( String query ) {
                List<String> res = new LinkedList<>();

                for ( TruststoreConfig e : truststores ) {
                    String alias = e.getAlias();
                    if ( alias != null && !alias.isEmpty() && alias.startsWith(query) ) {
                        res.add(alias);
                    }
                }

                if ( CLIENT_TRUSTSTORE.startsWith(query) ) {
                    res.add(CLIENT_TRUSTSTORE);
                }

                return res;
            }
        };
    }
}
