/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.auth.cas.internal;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.jasig.cas.CentralAuthenticationServiceImpl;
import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.AuthenticationManager;
import org.jasig.cas.authentication.PolicyBasedAuthenticationManager;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.logout.LogoutManagerImpl;
import org.jasig.cas.logout.LogoutMessageCreator;
import org.jasig.cas.logout.SamlCompliantLogoutMessageCreator;
import org.jasig.cas.services.DefaultServicesManagerImpl;
import org.jasig.cas.services.RegexMatchingRegisteredServiceProxyPolicy;
import org.jasig.cas.services.RegexRegisteredService;
import org.jasig.cas.services.ServiceRegistryDao;
import org.jasig.cas.ticket.DefaultProxyGrantingTicketFactory;
import org.jasig.cas.ticket.DefaultProxyTicketFactory;
import org.jasig.cas.ticket.DefaultServiceTicketFactory;
import org.jasig.cas.ticket.DefaultTicketGrantingTicketFactory;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketFactory;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.UniqueTicketIdGenerator;
import org.jasig.cas.ticket.proxy.ProxyGrantingTicket;
import org.jasig.cas.ticket.proxy.ProxyTicket;
import org.jasig.cas.ticket.proxy.support.Cas20ProxyHandler;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import com.google.common.base.Objects;

import eu.agno3.orchestrator.server.auth.cas.ExtendedCASServer;
import eu.agno3.orchestrator.server.auth.cas.MultiTimeUseOrTimeoutExpirationPolicy;
import eu.agno3.orchestrator.server.auth.cas.TGTExpirationPolicy;
import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.ServerPubkeyVerifier;
import eu.agno3.runtime.crypto.tls.TLSContext;
import eu.agno3.runtime.security.DynamicModularRealmAuthorizer;
import eu.agno3.runtime.util.config.ConfigUtil;
import eu.agno3.runtime.util.net.LocalHostUtil;
import eu.agno3.runtime.util.osgi.DsUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = CASServerRegistration.class, immediate = true, configurationPid = "cas.server" )
public class CASServerRegistration {

    private static final Logger log = Logger.getLogger(CASServerRegistration.class);
    private ServiceRegistration<ExtendedCASServer> serviceReg;
    private TLSContext tlsContext;
    private TLSContext serverTlsContext;
    private TicketRegistry ticketRegistry;
    private DynamicModularRealmAuthorizer authorizer;


    @Reference ( target = "(subsystem=auth/casServer)" )
    protected synchronized void setTlsContext ( TLSContext tc ) {
        this.tlsContext = tc;
    }


    protected synchronized void unsetTlsContext ( TLSContext tc ) {
        if ( this.tlsContext == tc ) {
            this.tlsContext = null;
        }
    }


    @Reference ( target = "(subsystem=https)" )
    protected synchronized void setWebServerContext ( TLSContext sc ) {
        this.serverTlsContext = sc;
    }


    protected synchronized void unsetWebServerContext ( TLSContext sc ) {
        if ( this.serverTlsContext == sc ) {
            this.serverTlsContext = null;
        }
    }


    @Reference
    protected synchronized void setTicketRegistry ( TicketRegistry str ) {
        this.ticketRegistry = str;
    }


    protected synchronized void unsetTicketRegistry ( TicketRegistry str ) {
        if ( this.ticketRegistry == str ) {
            this.ticketRegistry = null;
        }
    }


    @Reference
    protected synchronized void setAuthorizer ( DynamicModularRealmAuthorizer a ) {
        this.authorizer = a;
    }


    protected synchronized void unsetAuthorizer ( DynamicModularRealmAuthorizer a ) {
        if ( this.authorizer == a ) {
            this.authorizer = null;
        }
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        log.debug("Setting up CAS server"); //$NON-NLS-1$

        FixedSimpleHttpClient httpClient = new FixedSimpleHttpClient();
        this.setupSSL(httpClient);

        AuthenticationHandler userAuthHandler = new ShiroAuthenticationHandler(this.authorizer, false);
        AuthenticationHandler serviceAuthHandler = new HttpServiceAuthenticationHandler(httpClient);
        AuthenticationManager authenticationManager = new PolicyBasedAuthenticationManager(userAuthHandler, serviceAuthHandler);

        TicketFactoryImpl ticketFactory = setupTicketFactory();

        ServiceRegistryDao serviceRegistry = new StaticServiceRegistryDAO();

        setupAllowedServices(serviceRegistry, ConfigUtil.parseStringMap(ctx.getProperties(), "allowServices", Collections.EMPTY_MAP)); //$NON-NLS-1$

        DefaultServicesManagerImpl servicesManager = new DefaultServicesManagerImpl(serviceRegistry);

        LogoutMessageCreator logoutMessageBuilder = new SamlCompliantLogoutMessageCreator();
        LogoutManagerImpl logoutManager = new LogoutManagerImpl(servicesManager, httpClient, logoutMessageBuilder);

        CentralAuthenticationServiceImpl casServer = new CentralAuthenticationServiceImpl(
            this.ticketRegistry,
            ticketFactory,
            servicesManager,
            logoutManager);

        casServer.setApplicationEventPublisher(new DummyEventPublisher());

        Cas20ProxyHandler proxyHandler = new Cas20ProxyHandler();
        proxyHandler.setUniqueTicketIdGenerator(new DefaultUniqueTicketIdGenerator());
        proxyHandler.setHttpClient(httpClient);
        ExtendedCASServer cas = new ExtendedCASServerImpl(casServer, proxyHandler, servicesManager, authenticationManager);
        this.serviceReg = DsUtil.registerSafe(ctx, ExtendedCASServer.class, cas, null);
    }


    /**
     * @param serviceRegistry
     * @param allowed
     */
    private static void setupAllowedServices ( ServiceRegistryDao serviceRegistry, Map<String, String> allowed ) {
        // TODO: this allows any service to access some user attributes, which is not exactly nice
        // better would be to add some explicitly allowed services
        // for the local GUI this would be the canonical name/address, for remote GUIs this might be a bit trickier
        // even more so for untrusted GUI (installed by the user) instances
        Map<String, String> allowServices = new HashMap<>(allowed);

        Set<String> builtinNames = new HashSet<>();

        String guessPrimaryHostName = LocalHostUtil.guessPrimaryHostName();
        if ( guessPrimaryHostName != null ) {
            builtinNames.add(guessPrimaryHostName);
        }

        String guessPrimaryAddress = LocalHostUtil.guessPrimaryAddress().getHostAddress();
        if ( !Objects.equal(guessPrimaryHostName, guessPrimaryAddress) ) {
            builtinNames.add(guessPrimaryAddress);
        }

        for ( String builtinName : builtinNames ) {
            allowServices.put(
                String.format("https://%s:8443/gui/.*", builtinName), //$NON-NLS-1$
                "Local GUI " + builtinName); //$NON-NLS-1$
        }

        allowServices.put(
            "https://localhost:8444/services/", //$NON-NLS-1$
            "Local Webservice API"); //$NON-NLS-1$

        if ( log.isDebugEnabled() ) {
            log.debug("Allowed services are " + allowServices); //$NON-NLS-1$
        }

        for ( Entry<String, String> e : allowServices.entrySet() ) {
            RegexRegisteredService regService = new RegexRegisteredService();
            regService.setServiceId(e.getKey());
            regService.setName(e.getValue());
            regService.setAttributeReleasePolicy(new AttributeReleasePolicy());
            regService.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy(e.getKey())); // $NON-NLS-1$
            serviceRegistry.save(regService);
        }
    }


    /**
     * @return
     */
    private TicketFactoryImpl setupTicketFactory () {
        TicketFactoryImpl ticketFactory = new TicketFactoryImpl();
        DefaultUniqueTicketIdGenerator idGenerator = new DefaultUniqueTicketIdGenerator();
        TGTExpirationPolicy tgtExpirationPolicy = new TGTExpirationPolicy(3600, 3600, TimeUnit.SECONDS);
        MultiTimeUseOrTimeoutExpirationPolicy stExpirationPolicy = new MultiTimeUseOrTimeoutExpirationPolicy(1, 60 * 1000);
        ticketFactory.tgtFactory.setTicketGrantingTicketUniqueTicketIdGenerator(idGenerator);
        ticketFactory.tgtFactory.setTicketGrantingTicketExpirationPolicy(tgtExpirationPolicy);

        Map<String, UniqueTicketIdGenerator> serivceUniqueTicketIdGen = new HashMap<>();
        serivceUniqueTicketIdGen.put(SimpleWebApplicationServiceImpl.class.getName(), idGenerator);
        ticketFactory.stFactory.setUniqueTicketIdGeneratorsForService(serivceUniqueTicketIdGen);
        ticketFactory.stFactory.setDefaultServiceTicketIdGenerator(idGenerator);

        ticketFactory.stFactory.setServiceTicketExpirationPolicy(stExpirationPolicy);

        ticketFactory.pgtFactory.setTicketGrantingTicketExpirationPolicy(tgtExpirationPolicy);
        ticketFactory.pgtFactory.setTicketGrantingTicketUniqueTicketIdGenerator(idGenerator);

        ticketFactory.ptFactory.setServiceTicketExpirationPolicy(stExpirationPolicy);
        ticketFactory.ptFactory.setUniqueTicketIdGeneratorsForService(serivceUniqueTicketIdGen);
        return ticketFactory;
    }


    /**
     * @param conn
     */
    private void setupSSL ( FixedSimpleHttpClient client ) {
        try {
            client.setSSLSocketFactory(this.tlsContext.getSocketFactory());
            client.setHostnameVerifier(
                new ServerPubkeyVerifier(this.serverTlsContext.getPrimaryCertificatePubKey(), this.tlsContext.getHostnameVerifier()));
        }
        catch ( CryptoException e ) {
            log.warn("Failed to setup SSL for cas proxy verification and single logout", e); //$NON-NLS-1$
        }
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        log.debug("Shutting down CAS server"); //$NON-NLS-1$
        if ( this.serviceReg != null ) {
            DsUtil.unregisterSafe(ctx, this.serviceReg);
        }
    }

    /**
     * @author mbechler
     *
     */
    private static final class DummyEventPublisher implements ApplicationEventPublisher {

        /**
         * 
         */
        public DummyEventPublisher () {}


        @Override
        public void publishEvent ( ApplicationEvent ev ) {

        }


        @Override
        public void publishEvent ( Object ev ) {

        }
    }

    /**
     * @author mbechler
     *
     */
    private final class TicketFactoryImpl implements TicketFactory {

        DefaultProxyGrantingTicketFactory pgtFactory = new DefaultProxyGrantingTicketFactory();
        DefaultTicketGrantingTicketFactory tgtFactory = new DefaultTicketGrantingTicketFactory();
        DefaultProxyTicketFactory ptFactory = new DefaultProxyTicketFactory();
        DefaultServiceTicketFactory stFactory = new DefaultServiceTicketFactory();


        /**
         * 
         */
        public TicketFactoryImpl () {}


        @SuppressWarnings ( "unchecked" )
        @Override
        public <T extends TicketFactory> T get ( Class<? extends Ticket> type ) {
            if ( ProxyGrantingTicket.class.isAssignableFrom(type) ) {
                return (T) this.pgtFactory;
            }
            else if ( TicketGrantingTicket.class.isAssignableFrom(type) ) {
                return (T) this.tgtFactory;
            }
            else if ( ProxyTicket.class.isAssignableFrom(type) ) {
                return (T) this.ptFactory;
            }
            else if ( ServiceTicket.class.isAssignableFrom(type) ) {
                return (T) this.stFactory;
            }
            throw new IllegalArgumentException();
        }
    }

}
