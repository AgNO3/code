/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jan 19, 2017 by mbechler
 */
package eu.agno3.orchestrator.realms.server.internal;


import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.jws.WebService;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext;
import eu.agno3.orchestrator.config.model.realm.server.service.AgentServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.ObjectAccessControl;
import eu.agno3.orchestrator.config.model.realm.server.util.PersistenceUtil;
import eu.agno3.orchestrator.realms.RealmEntityType;
import eu.agno3.orchestrator.realms.RealmLookupResult;
import eu.agno3.orchestrator.realms.service.RealmLookupService;
import eu.agno3.orchestrator.realms.service.RealmLookupServiceDescriptor;
import eu.agno3.runtime.ldap.client.LDAPClient;
import eu.agno3.runtime.net.ad.ADRealm;
import eu.agno3.runtime.net.ad.ADRealmManager;
import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.util.sid.SID;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.ws.server.RequirePermissions;
import eu.agno3.runtime.ws.server.WebServiceAddress;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    RealmLookupService.class, SOAPWebService.class,
} )
@WebService (
    endpointInterface = "eu.agno3.orchestrator.realms.service.RealmLookupService",
    targetNamespace = RealmLookupServiceDescriptor.NAMESPACE,
    serviceName = RealmLookupServiceDescriptor.DEFAULT_SERVICE_NAME )
@WebServiceAddress ( "/realm/lookup" )
public class RealmLookupServiceImpl implements RealmLookupService {

    private static final Logger log = Logger.getLogger(RealmLookupServiceImpl.class);

    private static final String OBJECT_CLASS = "objectClass"; //$NON-NLS-1$
    private static final String OBJECT_SID = "objectSid"; //$NON-NLS-1$
    private static final String CN = "cn"; //$NON-NLS-1$
    private static final String DISPLAY_NAME = "displayName"; //$NON-NLS-1$
    private static final String SAM_ACCOUNT_NAME = "sAMAccountName"; //$NON-NLS-1$
    private static final String UPN = "userPrincipalName"; //$NON-NLS-1$

    private static final String GROUP_CLASS = "group"; //$NON-NLS-1$
    private static final String USER_CLASS = "user"; //$NON-NLS-1$
    private static final String PERSON_CLASS = "person"; //$NON-NLS-1$

    private static final String TRUSTED_DOMAIN = "trustedDomain"; //$NON-NLS-1$

    private ADRealmManager realmManager;
    private DefaultServerServiceContext sctx;
    private PersistenceUtil persistenceUtil;
    private ObjectAccessControl authz;
    private AgentServerService agentService;

    private Map<String, String> sidToDomainCache = new HashMap<>();
    private Map<String, ADRealm> unboundRealmCache = new ConcurrentHashMap<>();
    private Map<String, ADRealm> boundADRealms = new ConcurrentHashMap<>();


    @Reference
    protected synchronized void setRealmManager ( ADRealmManager rlm ) {
        this.realmManager = rlm;
    }


    protected synchronized void unsetRealmManager ( ADRealmManager rlm ) {
        if ( this.realmManager == rlm ) {
            this.realmManager = null;
        }
    }


    @Reference
    protected synchronized void setContext ( DefaultServerServiceContext ctx ) {
        this.sctx = ctx;
    }


    protected synchronized void unsetContext ( DefaultServerServiceContext ctx ) {
        if ( this.sctx == ctx ) {
            this.sctx = null;
        }
    }


    @Reference
    protected synchronized void setPersistenceUtil ( PersistenceUtil pu ) {
        this.persistenceUtil = pu;
    }


    protected synchronized void unsetPersistenceUtil ( PersistenceUtil pu ) {
        if ( this.persistenceUtil == pu ) {
            this.persistenceUtil = null;
        }
    }


    @Reference
    protected synchronized void setObjectAccessControl ( ObjectAccessControl oac ) {
        this.authz = oac;
    }


    protected synchronized void unsetObjectAccessControl ( ObjectAccessControl oac ) {
        if ( this.authz == oac ) {
            this.authz = null;
        }
    }


    @Reference
    protected synchronized void setAgentService ( AgentServerService ass ) {
        this.agentService = ass;
    }


    protected synchronized void unsetAgentService ( AgentServerService ass ) {
        if ( this.agentService == ass ) {
            this.agentService = null;
        }
    }


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE )
    protected synchronized void bindADRealm ( ADRealm r ) {
        if ( this.boundADRealms.put(r.getDomainName(), r) != null ) {
            log.warn("Multiple instances for " + r.getDomainName()); //$NON-NLS-1$
        }

        if ( r.getDomainSid() != null ) {
            this.sidToDomainCache.put(r.getDomainSid().toString(), r.getDomainName());
        }

        ADRealm unboundCached = this.unboundRealmCache.remove(r.getDomainName());
        if ( unboundCached != null ) {
            unboundCached.close();
        }
    }


    protected synchronized void unbindADRealm ( ADRealm r ) {
        this.boundADRealms.remove(r.getDomainName(), r);
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        for ( ADRealm adRealm : this.unboundRealmCache.values() ) {
            log.debug("Closing realm instance " + adRealm.getDomainName()); //$NON-NLS-1$
            adRealm.close();
        }
        this.unboundRealmCache.clear();
    }


    /**
     * @param realm
     * @return
     * @throws ModelObjectNotFoundException
     * @throws KerberosException
     */
    private ADRealm getRealmManager ( String realm ) throws ModelObjectNotFoundException {
        try {
            ADRealm cached = this.boundADRealms.get(realm);
            if ( cached != null ) {
                log.debug("Realm is bound " + realm); //$NON-NLS-1$
                return cached;
            }

            synchronized ( this.unboundRealmCache ) {
                cached = this.unboundRealmCache.get(realm);
                if ( cached != null ) {
                    if ( !cached.isJoined() ) {
                        return null;
                    }
                    return cached;
                }

                if ( log.isDebugEnabled() ) {
                    log.debug("Creating new realm instance for " + realm); //$NON-NLS-1$
                }
                ADRealm instance = this.realmManager.getRealmInstance(realm);
                if ( !instance.isJoined() ) {
                    return null;
                }

                this.unboundRealmCache.put(realm, instance);
                this.sidToDomainCache.put(instance.getDomainSid().toString(), instance.getDomainName());
                return instance;
            }
        }
        catch ( KerberosException e ) {
            log.debug("Failed to get realm", e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.realms.service.RealmLookupService#getNameForSID(java.lang.String, java.lang.String)
     */
    @Override
    @RequirePermissions ( "realms:lookup" )
    public RealmLookupResult getNameForSID ( String realm, String sid ) throws ModelObjectNotFoundException, ModelServiceException {

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Looking up %s in %s", sid, realm)); //$NON-NLS-1$
        }

        ADRealm instance = getRealmManager(realm);
        if ( instance == null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Realm not configured " + realm); //$NON-NLS-1$
            }
            return null;
        }
        try ( LDAPClient cl = instance.getConnection() ) {

            Filter filter = Filter.createANDFilter(
                Filter.createORFilter(Filter.createEqualityFilter(OBJECT_CLASS, USER_CLASS), Filter.createEqualityFilter(OBJECT_CLASS, GROUP_CLASS) // $NON-NLS-1$
            ), Filter.createEqualityFilter(OBJECT_SID, sid.toString()));

            SearchResult sr = cl.search(null, SearchScope.SUB, filter, OBJECT_CLASS, OBJECT_SID, UPN, SAM_ACCOUNT_NAME, CN, DISPLAY_NAME);

            if ( sr.getEntryCount() == 0 ) {
                return null;
            }
            else if ( sr.getEntryCount() > 1 ) {
                log.warn("Multiple entries found for SID " + sid); //$NON-NLS-1$
            }

            return mapResult(sr.getSearchEntries().get(0), instance.getNetbiosDomainName(), sid);
        }
        catch ( LDAPException e ) {
            log.debug("Failed to get LDAP connection", e); //$NON-NLS-1$
        }
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.realms.service.RealmLookupService#search(java.lang.String, java.lang.String)
     */
    @Override
    @RequirePermissions ( "realms:search" )
    public List<RealmLookupResult> search ( String realm, String filter ) throws ModelObjectNotFoundException, ModelServiceException {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Searching for %s in %s", filter, realm)); //$NON-NLS-1$
        }

        ADRealm instance = getRealmManager(realm);
        if ( instance == null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Realm not configured " + realm); //$NON-NLS-1$
            }
            return Collections.EMPTY_LIST;
        }
        try ( LDAPClient cl = instance.getConnection() ) {

            Filter f = Filter.createANDFilter(
                Filter.createORFilter(
                    Filter.createANDFilter(
                        Filter.createEqualityFilter(OBJECT_CLASS, USER_CLASS),
                        Filter.createEqualityFilter(OBJECT_CLASS, PERSON_CLASS)),
                    Filter.createEqualityFilter(OBJECT_CLASS, GROUP_CLASS) // $NON-NLS-1$
            ), Filter.createPresenceFilter(OBJECT_SID));

            if ( !StringUtils.isBlank(filter) ) {
                f = Filter.createANDFilter(
                    f,
                    Filter.createORFilter(
                        Filter.createSubstringFilter(SAM_ACCOUNT_NAME, filter, null, null),
                        Filter.createSubstringFilter(UPN, filter, null, null),
                        Filter.createSubstringFilter(CN, filter, null, null),
                        Filter.createSubstringFilter(DISPLAY_NAME, filter, null, null)));
            }
            SearchResult sr = cl.search(null, SearchScope.SUB, f, OBJECT_CLASS, OBJECT_SID, UPN, SAM_ACCOUNT_NAME, CN, DISPLAY_NAME);

            if ( log.isDebugEnabled() ) {
                log.debug("Found entries " + sr.getEntryCount()); //$NON-NLS-1$
            }

            List<RealmLookupResult> res = new LinkedList<>();
            for ( SearchResultEntry re : sr.getSearchEntries() ) {
                res.add(mapResult(re, instance.getNetbiosDomainName(), null));
            }

            return res;
        }
        catch ( LDAPException e ) {
            log.debug("Failed to get LDAP connection", e); //$NON-NLS-1$
            return Collections.EMPTY_LIST;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.realms.service.RealmLookupService#lookupDomainByName(java.lang.String,
     *      java.lang.String)
     */
    @Override
    @RequirePermissions ( "realms:lookup" )
    public List<RealmLookupResult> lookupDomainByName ( String origin, String name ) throws ModelObjectNotFoundException, ModelServiceException {
        ADRealm instance = getRealmManager(origin);
        if ( instance == null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Realm not configured " + origin); //$NON-NLS-1$
            }
            return Collections.EMPTY_LIST;
        }
        if ( instance.getDomainName().equals(origin.toLowerCase(Locale.ROOT)) ) {
            return Collections.singletonList(mapDomain(instance));
        }

        try ( LDAPClient cl = instance.getConnection() ) {

            Filter f = Filter.createANDFilter(
                Filter.createEqualityFilter(OBJECT_CLASS, TRUSTED_DOMAIN), // $NON-NLS-1$
                Filter.createPresenceFilter(OBJECT_SID),
                Filter.createSubstringFilter(CN, name, null, null));

            SearchResult sr = cl.search(null, SearchScope.SUB, f, OBJECT_CLASS, OBJECT_SID, CN);

            List<RealmLookupResult> res = new LinkedList<>();
            for ( SearchResultEntry re : sr.getSearchEntries() ) {
                res.add(mapDomainEntry(re));
            }
            return res;
        }
        catch ( LDAPException e ) {
            log.debug("Failed to get LDAP connection", e); //$NON-NLS-1$
            return Collections.EMPTY_LIST;
        }

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.realms.service.RealmLookupService#lookupDomainSID(java.lang.String, java.lang.String)
     */
    @Override
    @RequirePermissions ( "realms:lookup" )
    public RealmLookupResult lookupDomainSID ( String origin, String sid ) throws ModelObjectNotFoundException, ModelServiceException {
        String cached = this.sidToDomainCache.get(sid);
        ADRealm instance = getRealmManager(cached != null ? cached : origin);
        if ( instance == null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Realm not configured " + origin); //$NON-NLS-1$
            }
            return null;
        }
        if ( instance.getDomainSid().toString().equals(sid) ) {
            return mapDomain(instance);
        }

        try ( LDAPClient cl = instance.getConnection() ) {
            Filter filter = Filter.createANDFilter(
                Filter.createEqualityFilter(OBJECT_CLASS, TRUSTED_DOMAIN), // $NON-NLS-1$
                Filter.createEqualityFilter(OBJECT_SID, sid.toString()));

            SearchResult sr = cl.search(null, SearchScope.SUB, filter, OBJECT_CLASS, OBJECT_SID, CN);
            if ( sr.getEntryCount() == 0 ) {
                return null;
            }
            else if ( sr.getEntryCount() > 1 ) {
                log.warn("Multiple entries found for SID " + sid); //$NON-NLS-1$
            }

            SearchResultEntry re = sr.getSearchEntries().get(0);
            return mapDomainEntry(re);
        }
        catch ( LDAPException e ) {
            log.debug("Failed to get LDAP connection", e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * @param re
     * @return
     */
    private static RealmLookupResult mapDomainEntry ( SearchResultEntry re ) {
        return new RealmLookupResult(
            RealmEntityType.DOMAIN,
            SID.fromBinary(re.getAttributeValueBytes(OBJECT_SID)).toString(),
            re.getAttributeValue(CN),
            re.getAttributeValue(CN));
    }


    /**
     * @param instance
     * @return
     */
    private static RealmLookupResult mapDomain ( ADRealm instance ) {
        return new RealmLookupResult(
            RealmEntityType.DOMAIN,
            instance.getDomainSid().toString(),
            instance.getNetbiosDomainName(),
            instance.getDomainName());
    }


    /**
     * @param re
     * @return
     */
    private static final RealmLookupResult mapResult ( SearchResultEntry re, String domain, String sidStr ) {
        RealmEntityType t = getRealmEntityType(re);
        String idName = getEntityIdName(re, domain, t);
        String displayName = getEntityDisplayName(re, idName);

        String sidVal = sidStr;
        if ( sidVal == null ) {
            try {
                sidVal = SID.fromBinary(re.getAttributeValueBytes(OBJECT_SID)).toString();
            }
            catch ( IllegalArgumentException e ) {
                log.warn("Failed to parse SID", e); //$NON-NLS-1$
            }
        }
        return new RealmLookupResult(t, sidVal, idName, displayName);
    }


    private static String getEntityIdName ( SearchResultEntry re, String domain, RealmEntityType t ) {

        if ( t == RealmEntityType.USER ) {
            String id = re.getAttributeValue(UPN);
            if ( !StringUtils.isBlank(id) ) {
                return id;
            }
            id = re.getAttributeValue(SAM_ACCOUNT_NAME);
            if ( !StringUtils.isBlank(id) ) {
                return domain + '\\' + id;
            }
        }
        else if ( t == RealmEntityType.GROUP ) {
            String name = re.getAttributeValue(CN);
            if ( !StringUtils.isBlank(name) ) {
                return domain + '\\' + name;
            }
        }

        if ( log.isDebugEnabled() ) {
            log.debug("No name could be determined for " + re.getDN()); //$NON-NLS-1$
        }
        return null;
    }


    /**
     * @param re
     * @return
     */
    private static String getEntityDisplayName ( SearchResultEntry re, String idName ) {
        String display = re.getAttributeValue(DISPLAY_NAME);
        if ( !StringUtils.isBlank(display) ) {
            return display;
        }
        display = re.getAttributeValue(CN);
        if ( !StringUtils.isBlank(display) ) {
            return display;
        }
        return idName;
    }


    /**
     * @param re
     * @return
     */
    private static RealmEntityType getRealmEntityType ( SearchResultEntry re ) {
        Set<String> objectClasses = new HashSet<>(Arrays.asList(re.getObjectClassValues()));
        if ( objectClasses.contains(GROUP_CLASS) ) {
            return RealmEntityType.GROUP;
        }
        else if ( objectClasses.contains(USER_CLASS) ) {
            return RealmEntityType.USER;
        }
        return RealmEntityType.UNKNOWN;
    }
}
