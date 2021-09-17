/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.02.2015 by mbechler
 */
package eu.agno3.runtime.ldap.client.internal;


import javax.net.ssl.SSLSocketFactory;

import org.apache.log4j.Logger;

import com.unboundid.ldap.sdk.ExtendedResult;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionOptions;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.PostConnectProcessor;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.extensions.StartTLSExtendedRequest;


/**
 * @author mbechler
 *
 */
public class TLSContextStartTLSPostConnectProcessor implements PostConnectProcessor {

    private static final Logger log = Logger.getLogger(TLSContextStartTLSPostConnectProcessor.class);

    private SSLSocketFactory socketFactory;


    /**
     * @param socketFactory
     */
    public TLSContextStartTLSPostConnectProcessor ( SSLSocketFactory socketFactory ) {
        this.socketFactory = socketFactory;
    }


    /**
     * {@inheritDoc}
     *
     * @see com.unboundid.ldap.sdk.PostConnectProcessor#processPreAuthenticatedConnection(com.unboundid.ldap.sdk.LDAPConnection)
     */
    @Override
    public void processPreAuthenticatedConnection ( LDAPConnection connection ) throws LDAPException {
        final StartTLSExtendedRequest startTLSRequest = new StartTLSExtendedRequest(this.socketFactory);
        final LDAPConnectionOptions opts = connection.getConnectionOptions();
        startTLSRequest.setResponseTimeoutMillis(opts.getConnectTimeoutMillis());

        log.debug("Doing StartTLS " + connection.synchronousMode()); //$NON-NLS-1$
        final ExtendedResult r = connection.processExtendedOperation(startTLSRequest);

        if ( !r.getResultCode().equals(ResultCode.SUCCESS) ) {
            log.debug("StartTLS failed", r); //$NON-NLS-1$
            throw new LDAPException(r);
        }

        log.debug("StartTLS complete"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see com.unboundid.ldap.sdk.PostConnectProcessor#processPostAuthenticatedConnection(com.unboundid.ldap.sdk.LDAPConnection)
     */
    @Override
    public void processPostAuthenticatedConnection ( LDAPConnection arg0 ) throws LDAPException {
        // nothing to do
    }
}
