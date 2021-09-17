/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.07.2015 by mbechler
 */
package eu.agno3.orchestrator.server.auth.cas.internal;


import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.AbstractDistributedTicketRegistry;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.caching.CacheService;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;


/**
 * 
 * 
 * 
 * Based on JASIG CAS Ehcache, License:
 * 
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at the following location:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 * 
 * @author mbechler
 *
 */
@Component ( service = TicketRegistry.class )
public class EHCacheTicketRegistry extends AbstractDistributedTicketRegistry {

    private static final String TGT_CACHE = "auth-cas-tgt"; //$NON-NLS-1$
    private static final String SERVICE_TKT_CACHE = "auth-cas-service"; //$NON-NLS-1$

    private CacheService cacheService;


    @Reference
    protected synchronized void setCacheService ( CacheService cs ) {
        this.cacheService = cs;
    }


    protected synchronized void unsetCacheService ( CacheService cs ) {
        if ( this.cacheService == cs ) {
            this.cacheService = null;
        }
    }

    private Ehcache serviceTicketsCache;
    private Ehcache ticketGrantingTicketsCache;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.ticketGrantingTicketsCache = this.cacheService.getCache(TGT_CACHE);
        this.serviceTicketsCache = this.cacheService.getCache(SERVICE_TKT_CACHE);
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        this.ticketGrantingTicketsCache = null;
        this.serviceTicketsCache = null;
    }


    private Ehcache getTGTCache () {
        if ( this.ticketGrantingTicketsCache == null ) {
            throw new IllegalStateException("Not initialized"); //$NON-NLS-1$
        }
        return this.ticketGrantingTicketsCache;
    }


    private Ehcache getServiceTktCache () {
        if ( this.serviceTicketsCache == null ) {
            throw new IllegalStateException("Not initialized"); //$NON-NLS-1$
        }
        return this.serviceTicketsCache;
    }


    @Override
    public void addTicket ( final Ticket ticket ) {
        final Element element = new Element(ticket.getId(), ticket);
        if ( ticket instanceof ServiceTicket ) {
            getServiceTktCache().put(element);
        }
        else if ( ticket instanceof TicketGrantingTicket ) {
            getTGTCache().put(element);
        }
        else {
            throw new IllegalArgumentException("Invalid type " + ticket); //$NON-NLS-1$
        }
    }


    @Override
    public boolean deleteSingleTicket ( String ticketId ) {
        if ( StringUtils.isBlank(ticketId) ) {
            return false;
        }
        return getServiceTktCache().remove(ticketId) || getTGTCache().remove(ticketId);
    }


    @Override
    public Ticket getTicket ( final String ticketId ) {
        if ( ticketId == null ) {
            return null;
        }

        Element element = getServiceTktCache().get(ticketId);
        if ( element == null ) {
            element = getTGTCache().get(ticketId);
        }
        return element == null ? null : getProxiedTicketInstance((Ticket) element.getObjectValue());
    }


    @Override
    public Collection<Ticket> getTickets () {
        final Collection<Element> serviceTickets = getServiceTktCache().getAll(getServiceTktCache().getKeysWithExpiryCheck()).values();
        final Collection<Element> tgtTicketsTickets = getTGTCache().getAll(getTGTCache().getKeysWithExpiryCheck()).values();

        final Collection<Ticket> allTickets = new HashSet<>(serviceTickets.size() + tgtTicketsTickets.size());

        for ( final Element ticket : serviceTickets ) {
            allTickets.add((Ticket) ticket.getObjectValue());
        }

        for ( final Element ticket : tgtTicketsTickets ) {
            allTickets.add((Ticket) ticket.getObjectValue());
        }

        return allTickets;
    }


    @Override
    protected void updateTicket ( final Ticket ticket ) {
        addTicket(ticket);
    }


    @Override
    protected boolean needsCallback () {
        return false;
    }


    @Override
    public int sessionCount () {
        return getTGTCache().getKeysWithExpiryCheck().size();
    }


    @Override
    public int serviceTicketCount () {
        return getServiceTktCache().getKeysWithExpiryCheck().size();
    }

}
