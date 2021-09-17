/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.04.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.internal;


import java.io.IOException;
import java.security.SecureRandom;

import javax.security.auth.Subject;

import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.crypto.random.SecureRandomProvider;
import eu.agno3.runtime.net.ad.ADConnector;
import eu.agno3.runtime.net.ad.ADException;
import eu.agno3.runtime.net.ad.ADRealm;
import eu.agno3.runtime.net.ad.NetlogonConnection;
import eu.agno3.runtime.net.dns.SRVEntry;
import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.net.krb5.Krb5SubjectUtil;

import jcifs.CIFSContext;
import jcifs.dcerpc.DcerpcHandle;
import jcifs.smb.Kerb5Authenticator;


/**
 * @author mbechler
 *
 */
@Component ( service = ADConnector.class )
public class ADConnectorImpl implements ADConnector {

    private static final Logger log = Logger.getLogger(ADConnectorImpl.class);

    private SecureRandom secureRandom;
    private SecureRandomProvider secureRandomProvider;


    @Reference
    protected synchronized void setSecureRandomProvider ( SecureRandomProvider srp ) {
        this.secureRandomProvider = srp;
    }


    protected synchronized void unsetSecureRandomProvider ( SecureRandomProvider srp ) {
        if ( this.secureRandomProvider == srp ) {
            this.secureRandomProvider = null;
        }
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.secureRandom = this.secureRandomProvider.getSecureRandom();
    }


    /**
     * 
     * @param realm
     * @param ctx
     * @return a netlogon connection
     * @throws ADException
     * @throws IOException
     */
    @Override
    public NetlogonConnection getNetlogonConnection ( ADRealm realm, CIFSContext ctx ) throws ADException, IOException {
        NetlogonConnection netlogon = new NetlogonConnectionImpl(getNetlogonEndpoint(realm, ctx), realm, this.secureRandom);
        try {
            netlogon.init();
        }
        catch ( ADException e ) {
            log.trace("Initialization failed", e); //$NON-NLS-1$
            if ( log.isDebugEnabled() ) {
                log.debug("Falling back to server side flags " + netlogon.getNegotiatedFlags()); //$NON-NLS-1$
            }

            netlogon = new NetlogonConnectionImpl(netlogon.getDcerpcHandle(), realm, netlogon.getNegotiatedFlags(), this.secureRandom);
            netlogon.init();
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Negotiated flags " + netlogon.getNegotiatedFlags()); //$NON-NLS-1$
        }
        return netlogon;
    }


    /**
     * 
     * @param realm
     * @param username
     * @param password
     * @return a SAMR dcerpc handle
     * @throws ADException
     */
    @Override
    public DcerpcHandle getSAMREndpointWithPassword ( ADRealm realm, String username, String password ) throws ADException {
        Subject subject;
        try {
            subject = Krb5SubjectUtil.getInitiatorSubject(username, password, realm.getKrbRealm(), null, false);
        }
        catch ( KerberosException e ) {
            throw new ADException("Failed to authenticate", e); //$NON-NLS-1$
        }
        CIFSContext tc = realm.getCIFSContext();
        return getSAMREndpoint(realm, tc.withCredentials(new Kerb5Authenticator(subject)));
    }


    /**
     * 
     * @param realm
     * @param ctx
     * @return a SAMR dcerpc handle
     * @throws ADException
     */
    @Override
    public DcerpcHandle getSAMREndpoint ( ADRealm realm, CIFSContext ctx ) throws ADException {
        return getEndpoint(realm, ctx, "SAMR", false); //$NON-NLS-1$
    }


    @Override
    public DcerpcHandle getNetlogonEndpoint ( ADRealm realm, CIFSContext ctx ) throws ADException {
        return getEndpoint(realm, ctx, "NETLOGON", true); //$NON-NLS-1$
    }


    @Override
    public DcerpcHandle getEndpoint ( ADRealm realm, CIFSContext ctx, String pipeName, boolean unshared ) throws ADException {
        for ( SRVEntry e : realm.getDomainControllers().getEntrySelection(-1) ) {
            String endpoint = String.format("ncacn_np:%s[\\PIPE\\%s]", e.getName(), pipeName); //$NON-NLS-1$
            try {
                DcerpcHandle dcerpcHandle = DcerpcHandle.getHandle(endpoint, ctx, unshared);
                dcerpcHandle.bind();
                return dcerpcHandle;
            }
            catch ( IOException ex ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Failed to contact domain controller " + e.getName(), ex); //$NON-NLS-1$
                }
            }
        }
        throw new ADException("Failed to contact any domain controllers for " + realm.getDomainName()); //$NON-NLS-1$
    }
}
