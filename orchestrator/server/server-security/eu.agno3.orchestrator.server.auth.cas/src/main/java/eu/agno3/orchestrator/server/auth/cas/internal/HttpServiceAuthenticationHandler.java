/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.auth.cas.internal;


import java.net.URL;
import java.security.GeneralSecurityException;

import javax.security.auth.login.FailedLoginException;

import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.DefaultHandlerResult;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.HttpBasedServiceCredential;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.util.http.HttpClient;

import eu.agno3.orchestrator.server.auth.cas.SimplePrincipal;


/**
 * @author mbechler
 *
 */
public class HttpServiceAuthenticationHandler implements AuthenticationHandler {

    private HttpClient httpClient;


    /**
     * @param httpClient
     */
    public HttpServiceAuthenticationHandler ( HttpClient httpClient ) {
        this.httpClient = httpClient;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.jasig.cas.authentication.AuthenticationHandler#authenticate(org.jasig.cas.authentication.Credential)
     */
    @Override
    public HandlerResult authenticate ( Credential cred ) throws GeneralSecurityException, PreventedException {
        final HttpBasedServiceCredential httpCred = (HttpBasedServiceCredential) cred;

        URL callbackUrl = httpCred.getCallbackUrl();

        if ( !this.isValidCallbackUrl(callbackUrl) ) {
            throw new FailedLoginException("Invalid callback URL"); //$NON-NLS-1$
        }

        if ( !this.httpClient.isValidEndPoint(callbackUrl) ) {
            throw new FailedLoginException("Could not validate proxy callback URL"); //$NON-NLS-1$
        }

        return new DefaultHandlerResult(this, httpCred, new SimplePrincipal(httpCred.getId()));
    }


    /**
     * @param callbackUrl
     * @return
     */
    protected boolean isValidCallbackUrl ( URL callbackUrl ) {
        if ( !"https".equals(callbackUrl.getProtocol()) ) { //$NON-NLS-1$
            return false;
        }
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.jasig.cas.authentication.AuthenticationHandler#getName()
     */
    @Override
    public String getName () {
        return "HttpService"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see org.jasig.cas.authentication.AuthenticationHandler#supports(org.jasig.cas.authentication.Credential)
     */
    @Override
    public boolean supports ( Credential cred ) {
        return cred instanceof HttpBasedServiceCredential;
    }

}
