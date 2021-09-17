/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.02.2015 by mbechler
 */
package eu.agno3.runtime.security.ldap.internal;


import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchResultListener;
import com.unboundid.ldap.sdk.SearchResultReference;

import eu.agno3.runtime.ldap.client.LDAPClient;
import eu.agno3.runtime.security.ldap.LDAPSynchronizationException;
import eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler;
import eu.agno3.runtime.security.ldap.LDAPSynchronizationRuntimeException;


/**
 * @author mbechler
 *
 */
public class UserSearchResultListener implements SearchResultListener {

    private static final Logger log = Logger.getLogger(UserSearchResultListener.class);

    /**
     * 
     */
    private static final long serialVersionUID = 9167667285881684145L;

    private LDAPUserSynchronizerImpl ldapUserSynchronizer;
    private Set<String> handledUserRefs;
    private Set<String> handledGroupRefs;

    private LDAPSynchronizationHandler handler;

    private LDAPClient subClient;

    private final AtomicLong errors = new AtomicLong();


    /**
     * @param ldapUserSynchronizer
     * @param handler
     * @param handledUserRefs
     * @param handledGroupRefs
     * @param subClient
     */
    public UserSearchResultListener ( LDAPUserSynchronizerImpl ldapUserSynchronizer, LDAPSynchronizationHandler handler, Set<String> handledUserRefs,
            Set<String> handledGroupRefs, LDAPClient subClient ) {
        this.ldapUserSynchronizer = ldapUserSynchronizer;
        this.handler = handler;
        this.handledUserRefs = handledUserRefs;
        this.handledGroupRefs = handledGroupRefs;
        this.subClient = subClient;
    }


    /**
     * {@inheritDoc}
     *
     * @see com.unboundid.ldap.sdk.SearchResultListener#searchEntryReturned(com.unboundid.ldap.sdk.SearchResultEntry)
     */
    @Override
    public void searchEntryReturned ( SearchResultEntry entry ) {
        long start = System.currentTimeMillis();
        try {

            DN entryDN = entry.getParsedDN();
            if ( this.handler.getConfig().getStyle().shouldExclude(this.subClient.getBaseDN(), entryDN) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Ignoring DN " + entryDN); //$NON-NLS-1$
                }
            }

            UUID r = this.ldapUserSynchronizer.handleUserResult(entry, this.handler, this.handledUserRefs, this.handledGroupRefs, this.subClient);

            if ( r == null ) {
                this.errors.incrementAndGet();
            }

            if ( log.isDebugEnabled() ) {
                log.debug(String.format("User synchronization took %.2f s for %s", ( System.currentTimeMillis() - start ) / 1000.0f, entry)); //$NON-NLS-1$
            }
        }
        catch (
            LDAPSynchronizationException |
            LDAPException e ) {
            throw new LDAPSynchronizationRuntimeException("Failed to synchronize " + entry, e); //$NON-NLS-1$
        }
    }


    /**
     * @return the errors
     */
    public long getErrors () {
        return this.errors.get();
    }


    /**
     * {@inheritDoc}
     *
     * @see com.unboundid.ldap.sdk.SearchResultListener#searchReferenceReturned(com.unboundid.ldap.sdk.SearchResultReference)
     */
    @Override
    public void searchReferenceReturned ( SearchResultReference ref ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Ignoring referal " + ref); //$NON-NLS-1$
        }
    }

}
