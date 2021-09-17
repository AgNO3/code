/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.06.2014 by mbechler
 */
package eu.agno3.orchestrator.gui.connector.ws.internal;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import javax.security.auth.login.CredentialExpiredException;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.apache.cxf.jaxws.JaxWsClientProxy;
import org.apache.cxf.transport.http.auth.HttpAuthSupplier;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.log4j.Logger;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import eu.agno3.orchestrator.gui.config.GuiConfig;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.gui.connector.ws.GuiWsClientFactory;
import eu.agno3.orchestrator.gui.connector.ws.GuiWsClientSessionContext;
import eu.agno3.orchestrator.server.session.SessionException;
import eu.agno3.orchestrator.server.session.SessionInfo;
import eu.agno3.orchestrator.server.session.service.SessionService;
import eu.agno3.runtime.update.RefreshListener;
import eu.agno3.runtime.ws.client.ClientBus;
import eu.agno3.runtime.ws.client.SOAPClientFactory;
import eu.agno3.runtime.ws.common.SOAPServiceClientDescriptor;
import eu.agno3.runtime.ws.common.SOAPWebService;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    GuiWsClientFactory.class, EventHandler.class, RefreshListener.class
}, property = {
    EventConstants.EVENT_TOPIC + "=eu/agno3/runtime/xml/binding/*"
} )
public class GuiWsClientFactoryImpl implements GuiWsClientFactory, EventHandler, RefreshListener {

    private static final Logger log = Logger.getLogger(GuiWsClientFactoryImpl.class);

    private SOAPClientFactory soapClientFactory;
    private GuiConfig config;

    private Map<String, SOAPServiceClientDescriptor<?>> descriptors = new HashMap<>();

    private Map<Class<?>, SOAPWebService> serviceProxyCache = Collections.synchronizedMap(new WeakHashMap<Class<?>, SOAPWebService>());

    private GuiWsClientSessionContext guiWsSessionContext;

    private ClientBus clientBus;

    private boolean anyBindingsUpdated = false;


    @Reference
    protected synchronized void setSoapClientFactory ( SOAPClientFactory f ) {
        this.soapClientFactory = f;
    }


    protected synchronized void unsetSoapClientFactory ( SOAPClientFactory f ) {
        if ( this.soapClientFactory == f ) {
            this.soapClientFactory = null;
        }
    }


    @Reference
    protected synchronized void setGuiConfig ( GuiConfig cfg ) {
        this.config = cfg;
    }


    protected synchronized void unsetGuiConfig ( GuiConfig cfg ) {
        if ( this.config == cfg ) {
            this.config = null;
        }
    }


    @Reference
    protected synchronized void setClientBus ( ClientBus b ) {
        this.clientBus = b;
    }


    protected synchronized void unsetClientBus ( ClientBus b ) {
        if ( this.clientBus == b ) {
            this.clientBus = null;
        }
    }


    @Reference
    protected synchronized void setGuiWsSessionContext ( GuiWsClientSessionContext wsCtx ) {
        this.guiWsSessionContext = wsCtx;
    }


    protected synchronized void unsetGuiWsSessionContext ( GuiWsClientSessionContext wsCtx ) {
        if ( this.guiWsSessionContext == wsCtx ) {
            this.guiWsSessionContext = null;
        }
    }


    @Reference ( cardinality = ReferenceCardinality.AT_LEAST_ONE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindDescriptor ( SOAPServiceClientDescriptor<?> descriptor ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Binding descriptor " + descriptor.getServiceClass()); //$NON-NLS-1$
        }
        if ( this.soapClientFactory != null ) {
            this.soapClientFactory.clearCaches(descriptor.getServiceClass());
        }
        this.descriptors.put(getDescriptorSeiName(descriptor), descriptor);
    }


    protected synchronized void unbindDescriptor ( SOAPServiceClientDescriptor<?> descriptor ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Unbinding descriptor " + descriptor.getServiceClass()); //$NON-NLS-1$
        }
        String name = getDescriptorSeiName(descriptor);
        if ( this.descriptors.get(name) == descriptor ) {
            this.descriptors.remove(name);
            this.serviceProxyCache.remove(descriptor.getServiceClass());
        }
    }


    private static String getDescriptorSeiName ( SOAPServiceClientDescriptor<?> descriptor ) {
        return descriptor.getServiceClass().getName();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.gui.connector.ws.GuiWsClientFactory#createService(java.lang.Class)
     */
    @Override
    public <T> T createService ( Class<T> sei ) throws GuiWebServiceException {
        HTTPClientPolicy clientPolicy = this.getClientPolicy();
        HttpAuthSupplier authSupplier = this.getAuthSupplier();
        return createServiceInternal(sei, clientPolicy, authSupplier);
    }


    protected SessionService createSessionService () throws GuiWebServiceException {
        return createServiceInternal(SessionService.class, this.getClientPolicy(), null);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.gui.connector.ws.GuiWsClientFactory#login(org.jasig.cas.client.authentication.AttributePrincipal)
     */
    @Override
    public SessionInfo login ( AttributePrincipal principal ) throws CredentialExpiredException, GuiWebServiceException, SessionException {
        return this.guiWsSessionContext.login(this.createSessionService(), principal);
    }


    /**
     * @param sei
     * @param clientPolicy
     * @param authSupplier
     * @return
     * @throws GuiWebServiceException
     */
    protected <T> T createServiceInternal ( Class<T> sei, HTTPClientPolicy clientPolicy, HttpAuthSupplier authSupplier )
            throws GuiWebServiceException {
        // CXF proxies are not neccessarily thread safe (http://cxf.apache.org/faq.html)
        // caution when session management or authentication comes into play
        @SuppressWarnings ( "unchecked" )
        T cached = (T) this.serviceProxyCache.get(sei);
        if ( cached != null ) {
            if ( log.isTraceEnabled() ) {
                log.trace("Using cached client proxy for " + sei); //$NON-NLS-1$
            }
            return cached;
        }

        URL wsdlUrl = this.getURLFor(sei);

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Creating service %s with WSDL from %s", sei.getName(), wsdlUrl)); //$NON-NLS-1$
        }

        T service = this.soapClientFactory.createClient(clientPolicy, authSupplier, sei, wsdlUrl, this.getServiceName(sei));
        ( (BindingProvider) service ).getRequestContext().put(JaxWsClientProxy.THREAD_LOCAL_REQUEST_CONTEXT, Boolean.TRUE.toString());
        this.serviceProxyCache.put(sei, (SOAPWebService) service);
        return service;
    }


    /**
     * @return the http client policy
     */
    protected HTTPClientPolicy getClientPolicy () {
        HTTPClientPolicy policy = new GUISessionHttpClientPolicy(this.guiWsSessionContext.getSessionCookieName());
        policy.setChunkingThreshold(16 * 1024);
        policy.setChunkLength(8192);
        policy.setAllowChunking(true);
        return policy;
    }


    protected HttpAuthSupplier getAuthSupplier () throws GuiWebServiceException {
        return new GUISessionHttpAuthSupplier(this.createSessionService(), this.guiWsSessionContext);
    }


    private URL getURLFor ( Class<?> sei ) throws GuiWebServiceException {
        try {
            return this.getDescriptor(sei).buildWsdlUrl(this.config.getWebServiceBaseAddress()).toURL();
        }
        catch ( MalformedURLException e ) {
            throw new GuiWebServiceException("Failed to construct WS URL", e); //$NON-NLS-1$
        }
    }


    private QName getServiceName ( Class<?> sei ) throws GuiWebServiceException {
        return this.getDescriptor(sei).getServiceName();
    }


    private SOAPServiceClientDescriptor<?> getDescriptor ( Class<?> sei ) throws GuiWebServiceException {
        SOAPServiceClientDescriptor<?> descriptor = this.descriptors.get(sei.getName());

        if ( descriptor == null ) {
            throw new GuiWebServiceException("No descriptor found for service " + sei.getName()); //$NON-NLS-1$
        }

        return descriptor;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.gui.connector.ws.GuiWsClientFactory#createAllServices()
     */
    @Override
    public void createAllServices () {
        Collection<SOAPServiceClientDescriptor<?>> values;
        synchronized ( this ) {
            values = new ArrayList<>(this.descriptors.values());
        }
        for ( SOAPServiceClientDescriptor<?> desc : values ) {
            try {
                if ( desc.isEagerInitialize() ) {
                    createService(desc.getServiceClass());
                }
            }
            catch ( Exception e ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Failed to create webservice " + desc.getServiceClass().getName(), e); //$NON-NLS-1$
                }
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.service.event.EventHandler#handleEvent(org.osgi.service.event.Event)
     */
    @Override
    public void handleEvent ( Event ev ) {
        if ( "eu/agno3/runtime/xml/binding/PACKAGE_UPDATED".equals(ev.getTopic()) ) { //$NON-NLS-1$
            String packageName = (String) ev.getProperty("package"); //$NON-NLS-1$
            if ( log.isDebugEnabled() ) {
                log.debug("Package updated " + packageName); //$NON-NLS-1$
            }

            this.anyBindingsUpdated = true;
        }
        else if ( "eu/agno3/runtime/xml/binding/NAMESPACE_UPDATED".equals(ev.getTopic()) ) { //$NON-NLS-1$
            String namespace = (String) ev.getProperty("namespace"); //$NON-NLS-1$
            if ( log.isDebugEnabled() ) {
                log.debug("Namespace updated " + namespace); //$NON-NLS-1$
            }

            this.anyBindingsUpdated = true;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.RefreshListener#startBundleUpdate()
     */
    @Override
    public void startBundleUpdate () {
        // ignore
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.RefreshListener#bundlesRefreshed()
     */
    @Override
    public void bundlesRefreshed () {
        // ignore
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.RefreshListener#bundlesStarted()
     */
    @Override
    public void bundlesStarted () {
        // ignore
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.RefreshListener#bundlesRefreshed()
     */
    @Override
    public void bundlesUpdated () {
        if ( this.anyBindingsUpdated ) {
            this.anyBindingsUpdated = false;
            log.debug("Bundle update complete, clearing service proxies"); //$NON-NLS-1$
            this.serviceProxyCache.clear();
        }
    }

}
