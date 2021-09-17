/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.02.2015 by mbechler
 */
package eu.agno3.runtime.ldap.client.internal;


import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import javax.naming.NamingException;
import javax.net.SocketFactory;

import org.osgi.service.component.annotations.Component;

import com.unboundid.ldap.sdk.BindRequest;
import com.unboundid.ldap.sdk.DNSSRVRecordServerSet;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionOptions;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPConnectionPoolHealthCheck;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPURL;
import com.unboundid.ldap.sdk.PostConnectProcessor;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.RoundRobinServerSet;
import com.unboundid.ldap.sdk.ServerSet;
import com.unboundid.ldap.sdk.SimpleBindRequest;
import com.unboundid.util.SASLUtils;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.TLSContext;
import eu.agno3.runtime.ldap.client.LDAPClient;
import eu.agno3.runtime.ldap.client.LDAPClientBuilder;
import eu.agno3.runtime.ldap.client.LDAPConfiguration;
import eu.agno3.runtime.net.dns.SRVEntries;
import eu.agno3.runtime.net.dns.SRVEntry;
import eu.agno3.runtime.net.dns.SRVUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = LDAPClientBuilder.class )
public class LDAPClientBuilderImpl implements LDAPClientBuilder {

    private static final Random RANDOM = new Random();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPClientBuilder#createConnectionPool(eu.agno3.runtime.ldap.client.LDAPConfiguration,
     *      eu.agno3.runtime.crypto.tls.TLSContext)
     */
    @Override
    public LDAPConnectionPool createConnectionPool ( LDAPConfiguration cfg, TLSContext tc ) throws LDAPException, CryptoException {
        return new LDAPConnectionPool(
            createServerSet(cfg, tc),
            createBindRequest(cfg),
            cfg.getInitialPoolSize(),
            cfg.getMaxPoolSize(),
            1,
            getPostConnectProcessor(cfg, tc),
            !cfg.ignoreInitialConnectFail(),
            this.getHealthCheck(cfg));
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPClientBuilder#createSingleConnection(eu.agno3.runtime.ldap.client.LDAPConfiguration,
     *      eu.agno3.runtime.crypto.tls.TLSContext)
     */
    @SuppressWarnings ( "resource" )
    @Override
    public LDAPClient createSingleConnection ( LDAPConfiguration cfg, TLSContext tc, BindRequest bindReq ) throws LDAPException, CryptoException {
        LDAPURL serverURL = selectSingleServer(cfg);
        String host = serverURL.getHost();
        int port = getServerPort(serverURL);
        LDAPConnectionOptions connOptions = makeConnectionOptions(tc, cfg);
        LDAPConnection conn = new LDAPConnection(getSocketFactory(cfg, tc), connOptions, host, port);
        PostConnectProcessor proc = getPostConnectProcessor(cfg, tc);
        if ( proc != null ) {
            proc.processPreAuthenticatedConnection(conn);
        }

        conn.setConnectionOptions(connOptions);

        if ( bindReq != null ) {
            conn.bind(bindReq);
        }
        else {
            conn.bind(createBindRequest(cfg));
        }

        return new LDAPClientWrapper(conn, cfg);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPClientBuilder#createSingleConnection(eu.agno3.runtime.ldap.client.LDAPConfiguration,
     *      eu.agno3.runtime.crypto.tls.TLSContext)
     */
    @Override
    public LDAPClient createSingleConnection ( LDAPConfiguration cfg, TLSContext tc ) throws LDAPException, CryptoException {
        return createSingleConnection(cfg, tc, null);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPClientBuilder#wrapPoolConnection(com.unboundid.ldap.sdk.LDAPConnectionPool,
     *      com.unboundid.ldap.sdk.LDAPConnection, eu.agno3.runtime.ldap.client.LDAPConfiguration)
     */
    @Override
    public LDAPClient wrapPoolConnection ( LDAPConnectionPool pool, LDAPConnection conn, LDAPConfiguration cfg ) {
        return new LDAPClientWrapper(pool, conn, cfg);
    }


    /**
     * Randomly selects one servre
     * 
     * @param ldapServers
     * @return
     * @throws LDAPException
     */
    private static LDAPURL selectSingleServer ( LDAPConfiguration cfg ) throws LDAPException {

        if ( cfg.getSRVDomain() != null ) {
            try {
                SRVEntries lookup = SRVUtil.lookup(cfg.getSRVDomain(), cfg.getSRVRecord(), cfg.getSRVTTL());
                List<SRVEntry> entrySelection = lookup.getEntrySelection(1);
                SRVEntry srvEntry = entrySelection.get(0);
                return new LDAPURL("ldap", srvEntry.getName(), srvEntry.getPort(), null, null, null, null); //$NON-NLS-1$
            }
            catch ( NamingException e ) {
                throw new LDAPException(ResultCode.CONNECT_ERROR, "Failed to lookup SRV record", e); //$NON-NLS-1$
            }
        }

        List<LDAPURL> ldapServers = cfg.getLDAPServers();
        if ( ldapServers == null || ldapServers.isEmpty() ) {
            throw new LDAPException(ResultCode.CONNECT_ERROR, "No servers configured"); //$NON-NLS-1$
        }

        return ldapServers.get(RANDOM.nextInt(ldapServers.size()));
    }


    /**
     * @param config2
     * @return
     * @throws CryptoException
     * @throws LDAPException
     */
    private static ServerSet createServerSet ( LDAPConfiguration cfg, TLSContext tc ) throws CryptoException, LDAPException {

        if ( cfg.getSRVRecord() != null ) {
            return new DNSSRVRecordServerSet(
                cfg.getSRVRecord() + "." + cfg.getSRVDomain(), //$NON-NLS-1$
                null,
                cfg.getSRVTTL(),
                getSocketFactory(cfg, tc),
                makeConnectionOptions(tc, cfg));
        }

        List<LDAPURL> servers = cfg.getLDAPServers();

        if ( servers == null ) {
            throw new LDAPException(ResultCode.CONNECT_ERROR, "No servers specified"); //$NON-NLS-1$
        }

        String[] addrs = new String[servers.size()];
        int[] ports = new int[servers.size()];

        int i = 0;
        for ( LDAPURL server : servers ) {
            addrs[ i ] = server.getHost();
            ports[ i ] = getServerPort(server);
            i++;
        }

        return new RoundRobinServerSet(addrs, ports, getSocketFactory(cfg, tc), makeConnectionOptions(tc, cfg));
    }


    /**
     * @param server
     * @return
     */
    private static int getServerPort ( LDAPURL server ) {

        if ( server.portProvided() ) {
            return server.getPort();
        }

        if ( "ldaps".equals(server.getScheme()) ) { //$NON-NLS-1$
            return LDAPURL.DEFAULT_LDAPS_PORT;
        }
        return LDAPURL.DEFAULT_LDAP_PORT;
    }


    /**
     * @param config2
     * @return
     * @throws LDAPException
     */
    private static BindRequest createBindRequest ( LDAPConfiguration cfg ) throws LDAPException {

        if ( cfg.useSASLAuth() ) {
            return createSASLBind(cfg);
        }
        else if ( cfg.isAnonymous() ) {
            return new SimpleBindRequest();
        }

        return new SimpleBindRequest(cfg.getBindDN(), cfg.getBindPassword(), cfg.getAuthControls());
    }


    /**
     * @param cfg
     * @return
     * @throws LDAPException
     */
    private static BindRequest createSASLBind ( LDAPConfiguration cfg ) throws LDAPException {

        if ( GSSAPISubjectBindRequest.GSSAPI_MECHANISM_NAME.equals(cfg.getSASLMechanism()) && cfg.getGSSAPISubjectFactory() != null ) {
            return new GSSAPISubjectBindRequest(cfg.getGSSAPISubjectFactory(), cfg.getSASLOptions());
        }

        List<String> options = new ArrayList<>();
        addSASLOpt(options, SASLUtils.SASL_OPTION_AUTH_ID, cfg.getSASLAuthID());
        addSASLOpt(options, SASLUtils.SASL_OPTION_REALM, cfg.getSASLRealm());
        for ( Entry<String, Object> e : cfg.getSASLOptions().entrySet() ) {
            addSASLOpt(options, e.getKey(), e.getValue());
        }

        return SASLUtils.createBindRequest(
            cfg.getBindDN() != null ? cfg.getBindDN().toString() : null,
            cfg.getBindPassword(),
            cfg.getSASLMechanism(),
            options,
            cfg.getAuthControls());
    }


    /**
     * @param options
     * @param saslOptionAuthId
     * @param saslAuthNID
     */
    private static void addSASLOpt ( List<String> options, String opt, Object val ) {
        if ( val != null ) {
            options.add(String.format("%s=%s", opt, val)); //$NON-NLS-1$
        }
    }


    /**
     * @param cfg
     * @return
     */
    protected LDAPConnectionPoolHealthCheck getHealthCheck ( LDAPConfiguration cfg ) {
        return new LDAPConnectionPoolHealthCheck();
    }


    /**
     * @param config2
     * @return
     * @throws CryptoException
     */
    private static PostConnectProcessor getPostConnectProcessor ( LDAPConfiguration cfg, TLSContext tc ) throws CryptoException {
        if ( cfg.useStartTLS() ) {
            return new TLSContextStartTLSPostConnectProcessor(tc.getSocketFactory());
        }

        return null;
    }


    /**
     * @param cfg
     * @return
     * @throws CryptoException
     */
    private static SocketFactory getSocketFactory ( LDAPConfiguration cfg, TLSContext tc ) throws CryptoException {
        if ( cfg.useSSL() ) {
            return tc.getSocketFactory();
        }

        return SocketFactory.getDefault();
    }


    /**
     * @return
     * @throws CryptoException
     */
    private static LDAPConnectionOptions makeConnectionOptions ( TLSContext tc, LDAPConfiguration cfg ) throws CryptoException {
        LDAPConnectionOptions opt = new LDAPConnectionOptions();
        opt.setUseKeepAlive(true);
        opt.setResponseTimeoutMillis(cfg.getResponseTimeout());
        opt.setConnectTimeoutMillis((int) cfg.getConnectionTimeout());
        if ( tc == null && ( cfg.useSSL() || cfg.useStartTLS() ) ) {
            throw new CryptoException("No TLS context available but configured to use SSL/TLS"); //$NON-NLS-1$
        }
        if ( tc != null ) {
            // TODO: hostname verification for SRV based access should be based on the SRV name
            opt.setSSLSocketVerifier(new DelegatingSSLSocketVerifier(tc.getHostnameVerifier()));
        }
        return opt;
    }
}
