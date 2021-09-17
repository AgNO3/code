/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.auth.cas.internal;


import java.util.Collection;
import java.util.List;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.AuthenticationContext;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.AuthenticationManager;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.logout.LogoutRequest;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.ticket.AbstractTicketException;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.proxy.ProxyGrantingTicket;
import org.jasig.cas.ticket.proxy.ProxyHandler;
import org.jasig.cas.ticket.proxy.ProxyTicket;
import org.jasig.cas.validation.Assertion;

import com.google.common.base.Predicate;

import eu.agno3.orchestrator.server.auth.cas.ExtendedCASServer;


/**
 * @author mbechler
 *
 */
public class ExtendedCASServerImpl implements ExtendedCASServer {

    private CentralAuthenticationService cas;
    private ProxyHandler proxyHandler;
    private ServicesManager serviceRegistry;
    private AuthenticationManager authenticationManager;


    /**
     * @param cas
     * @param proxyHandler
     * @param servicesManager
     * @param authManager
     * 
     */
    public ExtendedCASServerImpl ( CentralAuthenticationService cas, ProxyHandler proxyHandler, ServicesManager servicesManager,
            AuthenticationManager authManager ) {
        this.cas = cas;
        this.proxyHandler = proxyHandler;
        this.serviceRegistry = servicesManager;
        this.authenticationManager = authManager;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.jasig.cas.CentralAuthenticationService#createTicketGrantingTicket(org.jasig.cas.authentication.AuthenticationContext)
     */
    @Override
    public TicketGrantingTicket createTicketGrantingTicket ( AuthenticationContext ctx ) throws AuthenticationException, AbstractTicketException {
        return this.cas.createTicketGrantingTicket(ctx);
    }


    @Override
    public Assertion validateServiceTicket ( String arg0, Service arg1 ) throws AbstractTicketException {
        return this.cas.validateServiceTicket(arg0, arg1);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.jasig.cas.CentralAuthenticationService#destroyTicketGrantingTicket(java.lang.String)
     */
    @Override
    public List<LogoutRequest> destroyTicketGrantingTicket ( String tkt ) {
        return this.cas.destroyTicketGrantingTicket(tkt);
    }


    @Override
    public ProxyGrantingTicket createProxyGrantingTicket ( String tgt, AuthenticationContext ctx )
            throws AuthenticationException, AbstractTicketException {
        return this.cas.createProxyGrantingTicket(tgt, ctx);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.jasig.cas.CentralAuthenticationService#getTickets(com.google.common.base.Predicate)
     */
    @Override
    public Collection<Ticket> getTickets ( Predicate<Ticket> preds ) {
        return this.cas.getTickets(preds);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.jasig.cas.CentralAuthenticationService#grantProxyTicket(java.lang.String,
     *      org.jasig.cas.authentication.principal.Service)
     */
    @Override
    public ProxyTicket grantProxyTicket ( String tkt, Service s ) throws AbstractTicketException {
        return this.cas.grantProxyTicket(tkt, s);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.jasig.cas.CentralAuthenticationService#grantServiceTicket(java.lang.String,
     *      org.jasig.cas.authentication.principal.Service, org.jasig.cas.authentication.AuthenticationContext)
     */
    @Override
    public ServiceTicket grantServiceTicket ( String tkt, Service service, AuthenticationContext ctx )
            throws AuthenticationException, AbstractTicketException {
        return this.cas.grantServiceTicket(tkt, service, ctx);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.jasig.cas.CentralAuthenticationService#getTicket(java.lang.String, java.lang.Class)
     */
    @Override
    public <T extends Ticket> T getTicket ( String arg0, Class<? extends Ticket> arg1 ) throws InvalidTicketException {
        return this.cas.getTicket(arg0, arg1);
    }


    /**
     * @return the serviceRegistry
     */
    @Override
    public ServicesManager getServicesManager () {
        return this.serviceRegistry;
    }


    @Override
    public AuthenticationManager getAuthenticationManager () {
        return this.authenticationManager;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.auth.cas.ExtendedCASServer#getProxyHandler()
     */
    @Override
    public ProxyHandler getProxyHandler () {
        return this.proxyHandler;
    }

}
