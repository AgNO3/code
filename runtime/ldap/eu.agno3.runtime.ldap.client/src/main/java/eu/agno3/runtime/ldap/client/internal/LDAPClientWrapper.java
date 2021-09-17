/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2015 by mbechler
 */
package eu.agno3.runtime.ldap.client.internal;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.unboundid.ldap.sdk.AddRequest;
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.CompareRequest;
import com.unboundid.ldap.sdk.CompareResult;
import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.DeleteRequest;
import com.unboundid.ldap.sdk.DereferencePolicy;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.ExtendedRequest;
import com.unboundid.ldap.sdk.ExtendedResult;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.LDAPSearchException;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModifyDNRequest;
import com.unboundid.ldap.sdk.ModifyRequest;
import com.unboundid.ldap.sdk.RDN;
import com.unboundid.ldap.sdk.ReadOnlyAddRequest;
import com.unboundid.ldap.sdk.ReadOnlyCompareRequest;
import com.unboundid.ldap.sdk.ReadOnlyDeleteRequest;
import com.unboundid.ldap.sdk.ReadOnlyModifyDNRequest;
import com.unboundid.ldap.sdk.ReadOnlyModifyRequest;
import com.unboundid.ldap.sdk.ReadOnlySearchRequest;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.RootDSE;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchResultListener;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.ldap.sdk.schema.Schema;
import com.unboundid.ldif.LDIFException;

import eu.agno3.runtime.ldap.client.LDAPClient;
import eu.agno3.runtime.ldap.client.LDAPConfiguration;


/**
 * @author mbechler
 *
 */
public class LDAPClientWrapper implements LDAPClient {

    private final LDAPConnection delegate;
    private final LDAPConnectionPool pool;

    private final LDAPConfiguration cfg;
    private RootDSE cachedRootDSE;
    private DN cachedBaseDN;
    private Set<String> cachedControls;


    /**
     * @param connection
     * @param cfg
     * 
     */
    public LDAPClientWrapper ( LDAPConnection connection, LDAPConfiguration cfg ) {
        this.delegate = connection;
        this.cfg = cfg;
        this.pool = null;
    }


    /**
     * @param pool
     * @param connection
     * @param cfg
     */
    public LDAPClientWrapper ( LDAPConnectionPool pool, LDAPConnection connection, LDAPConfiguration cfg ) {
        this.pool = pool;
        this.cfg = cfg;
        this.delegate = connection;
    }


    /**
     * @return the delegate object for advanced usage
     */
    public LDAPConnection getDelegate () {
        return this.delegate;
    }


    @Override
    public DN getBaseDN () throws LDAPException {

        if ( this.cfg.getDefaultBaseDN() != null ) {
            return this.cfg.getDefaultBaseDN();
        }

        if ( this.cachedBaseDN != null ) {
            return this.cachedBaseDN;
        }

        RootDSE rootDse = this.getCachedRootDSE();
        String[] namingContexts = rootDse.getNamingContextDNs();

        if ( namingContexts != null && namingContexts.length == 1 && !StringUtils.isBlank(namingContexts[ 0 ]) ) {
            this.cachedBaseDN = new DN(namingContexts[ 0 ]);
            return this.cachedBaseDN;
        }

        throw new LDAPException(ResultCode.NO_SUCH_OBJECT, "No root DN is configured and it could not be automatically determined"); //$NON-NLS-1$
    }


    @Override
    public DN relativeDN ( String relative ) throws LDAPException {
        List<RDN> fullRnds = new ArrayList<>();
        RDN[] baseRdns = this.getBaseDN().getRDNs();
        RDN[] otherRdns = ( new DN(relative) ).getRDNs();

        if ( otherRdns != null ) {
            fullRnds.addAll(Arrays.asList(otherRdns));
        }

        if ( baseRdns != null ) {
            fullRnds.addAll(Arrays.asList(baseRdns));
        }

        return new DN(fullRnds);
    }


    @Override
    public synchronized RootDSE getCachedRootDSE () throws LDAPException {
        if ( this.cachedRootDSE == null ) {
            this.cachedRootDSE = this.getRootDSE();
        }
        return this.cachedRootDSE;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws LDAPException
     *
     * @see eu.agno3.runtime.ldap.client.LDAPClient#isControlSupported(java.lang.String)
     */
    @Override
    public boolean isControlSupported ( String oid ) throws LDAPException {

        if ( this.cachedControls == null ) {
            this.cachedControls = new HashSet<>(Arrays.asList(this.getCachedRootDSE().getSupportedControlOIDs()));
        }

        return this.cachedControls.contains(oid);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPClient#close()
     */
    @Override
    public void close () {
        if ( this.pool != null ) {
            this.pool.releaseConnection(this.delegate);
        }
        else {
            this.delegate.close();
        }
    }


    @Override
    public LDAPResult add ( AddRequest arg0 ) throws LDAPException {
        return this.delegate.add(arg0);
    }


    @Override
    public LDAPResult add ( Entry arg0 ) throws LDAPException {
        return this.delegate.add(arg0);
    }


    @Override
    public LDAPResult add ( ReadOnlyAddRequest arg0 ) throws LDAPException {
        return this.delegate.add(arg0);
    }


    @Override
    public LDAPResult add ( String arg0, Attribute... arg1 ) throws LDAPException {
        return this.delegate.add(arg0, arg1);
    }


    @Override
    public LDAPResult add ( String arg0, Collection<Attribute> arg1 ) throws LDAPException {
        return this.delegate.add(arg0, arg1);
    }


    @Override
    public LDAPResult add ( String... arg0 ) throws LDIFException, LDAPException {
        return this.delegate.add(arg0);
    }


    @Override
    public CompareResult compare ( CompareRequest arg0 ) throws LDAPException {
        return this.delegate.compare(arg0);
    }


    @Override
    public CompareResult compare ( ReadOnlyCompareRequest arg0 ) throws LDAPException {
        return this.delegate.compare(arg0);
    }


    @Override
    public CompareResult compare ( String arg0, String arg1, String arg2 ) throws LDAPException {
        return this.delegate.compare(arg0, arg1, arg2);
    }


    @Override
    public LDAPResult delete ( DeleteRequest arg0 ) throws LDAPException {
        return this.delegate.delete(arg0);
    }


    @Override
    public LDAPResult delete ( ReadOnlyDeleteRequest arg0 ) throws LDAPException {
        return this.delegate.delete(arg0);
    }


    @Override
    public LDAPResult delete ( String arg0 ) throws LDAPException {
        return this.delegate.delete(arg0);
    }


    @Override
    public SearchResultEntry getEntry ( String arg0, String... arg1 ) throws LDAPException {
        return this.delegate.getEntry(arg0, arg1);
    }


    @Override
    public SearchResultEntry getEntry ( String arg0 ) throws LDAPException {
        return this.delegate.getEntry(arg0);
    }


    @Override
    public RootDSE getRootDSE () throws LDAPException {
        return this.delegate.getRootDSE();
    }


    @Override
    public Schema getSchema () throws LDAPException {
        return this.delegate.getSchema();
    }


    @Override
    public Schema getSchema ( String arg0 ) throws LDAPException {
        return this.delegate.getSchema(arg0);
    }


    @Override
    public LDAPResult modify ( ModifyRequest arg0 ) throws LDAPException {
        return this.delegate.modify(arg0);
    }


    @Override
    public LDAPResult modify ( ReadOnlyModifyRequest arg0 ) throws LDAPException {
        return this.delegate.modify(arg0);
    }


    @Override
    public LDAPResult modify ( String arg0, List<Modification> arg1 ) throws LDAPException {
        return this.delegate.modify(arg0, arg1);
    }


    @Override
    public LDAPResult modify ( String arg0, Modification... arg1 ) throws LDAPException {
        return this.delegate.modify(arg0, arg1);
    }


    @Override
    public LDAPResult modify ( String arg0, Modification arg1 ) throws LDAPException {
        return this.delegate.modify(arg0, arg1);
    }


    @Override
    public LDAPResult modify ( String... arg0 ) throws LDIFException, LDAPException {
        return this.delegate.modify(arg0);
    }


    @Override
    public LDAPResult modifyDN ( ModifyDNRequest arg0 ) throws LDAPException {
        return this.delegate.modifyDN(arg0);
    }


    @Override
    public LDAPResult modifyDN ( ReadOnlyModifyDNRequest arg0 ) throws LDAPException {
        return this.delegate.modifyDN(arg0);
    }


    @Override
    public LDAPResult modifyDN ( String arg0, String arg1, boolean arg2, String arg3 ) throws LDAPException {
        return this.delegate.modifyDN(arg0, arg1, arg2, arg3);
    }


    @Override
    public LDAPResult modifyDN ( String arg0, String arg1, boolean arg2 ) throws LDAPException {
        return this.delegate.modifyDN(arg0, arg1, arg2);
    }


    @Override
    public SearchResult search ( ReadOnlySearchRequest req ) throws LDAPSearchException {
        return this.delegate.search(req);
    }


    @Override
    public SearchResult search ( SearchRequest req ) throws LDAPSearchException {
        addBaseDN(req);
        return this.delegate.search(req);
    }


    @Override
    public SearchResult search ( SearchResultListener l, String base, SearchScope scope, DereferencePolicy derefPolicy, int arg4, int arg5,
            boolean arg6, Filter arg7, String... arg8 ) throws LDAPSearchException {
        return this.delegate.search(l, this.wrapBaseDn(base), scope, derefPolicy, arg4, arg5, arg6, arg7, arg8);
    }


    @Override
    public SearchResult search ( SearchResultListener arg0, String baseDn, SearchScope arg2, DereferencePolicy arg3, int arg4, int arg5,
            boolean arg6, String arg7, String... arg8 ) throws LDAPSearchException {
        return this.delegate.search(arg0, this.wrapBaseDn(baseDn), arg2, arg3, arg4, arg5, arg6, arg7, arg8);
    }


    @Override
    public SearchResult search ( SearchResultListener arg0, String baseDn, SearchScope arg2, Filter arg3, String... arg4 ) throws LDAPSearchException {
        return this.delegate.search(arg0, this.wrapBaseDn(baseDn), arg2, arg3, arg4);
    }


    @Override
    public SearchResult search ( SearchResultListener arg0, String baseDn, SearchScope arg2, String arg3, String... arg4 ) throws LDAPSearchException {
        return this.delegate.search(arg0, this.wrapBaseDn(baseDn), arg2, arg3, arg4);
    }


    @Override
    public SearchResult search ( String baseDn, SearchScope arg1, DereferencePolicy arg2, int arg3, int arg4, boolean arg5, Filter arg6,
            String... arg7 ) throws LDAPSearchException {
        return this.delegate.search(this.wrapBaseDn(baseDn), arg1, arg2, arg3, arg4, arg5, arg6, arg7);
    }


    @Override
    public SearchResult search ( String baseDn, SearchScope arg1, DereferencePolicy arg2, int arg3, int arg4, boolean arg5, String arg6,
            String... arg7 ) throws LDAPSearchException {
        return this.delegate.search(this.wrapBaseDn(baseDn), arg1, arg2, arg3, arg4, arg5, arg6, arg7);
    }


    @Override
    public SearchResult search ( String baseDn, SearchScope arg1, Filter arg2, String... arg3 ) throws LDAPSearchException {
        return this.delegate.search(this.wrapBaseDn(baseDn), arg1, arg2, arg3);
    }


    @Override
    public SearchResult search ( String baseDn, SearchScope arg1, String arg2, String... arg3 ) throws LDAPSearchException {
        return this.delegate.search(this.wrapBaseDn(baseDn), arg1, arg2, arg3);
    }


    @Override
    public SearchResultEntry searchForEntry ( ReadOnlySearchRequest req ) throws LDAPSearchException {
        return this.delegate.searchForEntry(req);
    }


    @Override
    public SearchResultEntry searchForEntry ( SearchRequest req ) throws LDAPSearchException {
        this.addBaseDN(req);
        return this.delegate.searchForEntry(req);
    }


    @Override
    public SearchResultEntry searchForEntry ( String baseDn, SearchScope arg1, DereferencePolicy arg2, int arg3, boolean arg4, Filter arg5,
            String... arg6 ) throws LDAPSearchException {
        return this.delegate.searchForEntry(this.wrapBaseDn(baseDn), arg1, arg2, arg3, arg4, arg5, arg6);
    }


    @Override
    public SearchResultEntry searchForEntry ( String baseDn, SearchScope arg1, DereferencePolicy arg2, int arg3, boolean arg4, String arg5,
            String... arg6 ) throws LDAPSearchException {
        return this.delegate.searchForEntry(this.wrapBaseDn(baseDn), arg1, arg2, arg3, arg4, arg5, arg6);
    }


    @Override
    public SearchResultEntry searchForEntry ( String baseDn, SearchScope arg1, Filter arg2, String... arg3 ) throws LDAPSearchException {
        return this.delegate.searchForEntry(this.wrapBaseDn(baseDn), arg1, arg2, arg3);
    }


    @Override
    public SearchResultEntry searchForEntry ( String baseDn, SearchScope arg1, String arg2, String... arg3 ) throws LDAPSearchException {
        return this.delegate.searchForEntry(this.wrapBaseDn(baseDn), arg1, arg2, arg3);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws LDAPException
     *
     * @see eu.agno3.runtime.ldap.client.LDAPClient#processExtendedOperation(com.unboundid.ldap.sdk.ExtendedRequest)
     */
    @Override
    public ExtendedResult processExtendedOperation ( ExtendedRequest extendedRequest ) throws LDAPException {
        return this.delegate.processExtendedOperation(extendedRequest);
    }


    /**
     * @param baseDn
     * @return
     * @throws LDAPSearchException
     */
    private String wrapBaseDn ( String baseDn ) throws LDAPSearchException {
        if ( StringUtils.isBlank(baseDn) ) {
            try {
                return this.getBaseDN().toString();
            }
            catch ( LDAPException e ) {
                throw new LDAPSearchException(e);
            }
        }

        return baseDn;
    }


    /**
     * @param req
     * @throws LDAPSearchException
     */
    private void addBaseDN ( SearchRequest req ) throws LDAPSearchException {
        if ( StringUtils.isBlank(req.getBaseDN()) ) {
            try {
                req.setBaseDN(this.getBaseDN());
            }
            catch ( LDAPException e ) {
                throw new LDAPSearchException(e);
            }
        }
    }
}
