/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package eu.agno3.runtime.security.cas.client;


import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;


/**
 * CAS Authentication filter from shiro-cas
 * 
 * This filter validates the CAS service ticket to authenticate the user. It must be configured on the URL recognized
 * by the CAS server. For example, in {@code shiro.ini}:
 * 
 * <pre>
 * [main]
 * casFilter = org.apache.shiro.cas.CasFilter
 * ...
 * 
 * [urls]
 * /shiro-cas = casFilter
 * ...
 * </pre>
 * 
 * (example : http://host:port/mycontextpath/shiro-cas)
 *
 * @since 1.2
 */
public class CasAuthenticationFilter extends AuthenticatingFilter {

    private static final Logger log = Logger.getLogger(CasAuthenticationFilter.class);

    // the name of the parameter service ticket in url
    private static final String TICKET_PARAMETER = "ticket"; //$NON-NLS-1$

    // the url where the application is redirected if the CAS service ticket validation failed (example :
    // /mycontextpatch/cas_error.jsp)
    private String failureUrl;


    /**
     * The token created for this authentication is a CasToken containing the CAS service ticket received on the CAS
     * service url (on which
     * the filter must be configured).
     * 
     * @param request
     *            the incoming request
     * @param response
     *            the outgoing response
     * @throws Exception
     *             if there is an error processing the request.
     */
    @Override
    protected AuthenticationToken createToken ( ServletRequest request, ServletResponse response ) throws Exception {
        log.debug("Creating token from ticket"); //$NON-NLS-1$
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String ticket = httpRequest.getParameter(TICKET_PARAMETER);
        return new CasToken(ticket);
    }


    /**
     * Execute login by creating {@link #createToken(javax.servlet.ServletRequest, javax.servlet.ServletResponse) token}
     * and logging subject
     * with this token.
     * 
     * @param request
     *            the incoming request
     * @param response
     *            the outgoing response
     * @throws Exception
     *             if there is an error processing the request.
     */
    @Override
    protected boolean onAccessDenied ( ServletRequest request, ServletResponse response ) throws Exception {
        return executeLogin(request, response);
    }


    /**
     * Returns <code>false</code> to always force authentication (user is never considered authenticated by this
     * filter).
     * 
     * @param request
     *            the incoming request
     * @param response
     *            the outgoing response
     * @param mappedValue
     *            the filter-specific config value mapped to this filter in the URL rules mappings.
     * @return <code>false</code>
     */
    @Override
    protected boolean isAccessAllowed ( ServletRequest request, ServletResponse response, Object mappedValue ) {
        return false;
    }


    /**
     * If login has been successful, redirect user to the original protected url.
     * 
     * @param token
     *            the token representing the current authentication
     * @param subject
     *            the current authenticated subjet
     * @param request
     *            the incoming request
     * @param response
     *            the outgoing response
     * @throws Exception
     *             if there is an error processing the request.
     */
    @Override
    protected boolean onLoginSuccess ( AuthenticationToken token, Subject subject, ServletRequest request, ServletResponse response )
            throws Exception {
        issueSuccessRedirect(request, response);
        return false;
    }


    /**
     * If login has failed, redirect user to the CAS error page (no ticket or ticket validation failed) except if the
     * user is already
     * authenticated, in which case redirect to the default success url.
     * 
     * @param token
     *            the token representing the current authentication
     * @param ae
     *            the current authentication exception
     * @param request
     *            the incoming request
     * @param response
     *            the outgoing response
     */
    @Override
    protected boolean onLoginFailure ( AuthenticationToken token, AuthenticationException ae, ServletRequest request, ServletResponse response ) {
        // is user authenticated or in remember me mode ?
        Subject subject = getSubject(request, response);
        if ( subject.isAuthenticated() || subject.isRemembered() ) {
            try {
                issueSuccessRedirect(request, response);
            }
            catch ( Exception e ) {
                log.error("Cannot redirect to the default success url", e); //$NON-NLS-1$
            }
        }
        else {
            try {
                WebUtils.issueRedirect(request, response, this.failureUrl);
            }
            catch ( IOException e ) {
                log.error("Cannot redirect to failure url : " + this.failureUrl, e); //$NON-NLS-1$
            }
        }
        return false;
    }


    /**
     * @param failureUrl
     */
    public void setFailureUrl ( String failureUrl ) {
        this.failureUrl = failureUrl;
    }
}
