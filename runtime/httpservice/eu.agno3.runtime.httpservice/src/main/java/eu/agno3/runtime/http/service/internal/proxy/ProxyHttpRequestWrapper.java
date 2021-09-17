/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.09.2015 by mbechler
 */
package eu.agno3.runtime.http.service.internal.proxy;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.runtime.http.service.ReverseProxyConfig;
import eu.agno3.runtime.http.service.internal.HostUtil;


/**
 * @author mbechler
 *
 */
public class ProxyHttpRequestWrapper extends HttpServletRequestWrapper {

    /**
     * 
     */
    private static final String CACHED_SSL_CIPHERS_SPECS = "javax.servlet.request.cipher_suite.cached"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(ProxyHttpRequestWrapper.class);

    private final ReverseProxyConfig proxyConfig;


    /**
     * @param proxyConfig
     * @param request
     */
    public ProxyHttpRequestWrapper ( ReverseProxyConfig proxyConfig, HttpServletRequest request ) {
        super(request);
        this.proxyConfig = proxyConfig;
    }


    @Override
    public String getServerName () {
        String localName = this.proxyConfig.getOriginalHost((HttpServletRequest) getRequest());
        if ( !StringUtils.isBlank(localName) && HostUtil.validateHostHeader(localName) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Local name is " + localName); //$NON-NLS-1$
            }
            return localName;
        }
        return super.getServerName();
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.ServletRequestWrapper#getServerPort()
     */
    @Override
    public int getServerPort () {
        Integer forwardedPort = this.proxyConfig.getOriginalPort((HttpServletRequest) getRequest());
        if ( forwardedPort != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Local port is " + forwardedPort); //$NON-NLS-1$
            }
            return forwardedPort;
        }

        return super.getServerPort();
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.ServletRequestWrapper#isSecure()
     */
    @Override
    public boolean isSecure () {
        String scheme = this.proxyConfig.getOriginalScheme((HttpServletRequest) getRequest());
        if ( !StringUtils.isBlank(scheme) ) {
            log.debug("Proxied request is originally secure"); //$NON-NLS-1$
            return "https".equals(scheme); //$NON-NLS-1$
        }

        return super.isSecure();
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.http.HttpServletRequestWrapper#getHeader(java.lang.String)
     */
    @Override
    public String getHeader ( String name ) {
        if ( "host".equalsIgnoreCase(name) ) { //$NON-NLS-1$
            return getServerName();
        }
        return super.getHeader(name);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.ServletRequestWrapper#getRemoteAddr()
     */
    @Override
    public String getRemoteAddr () {
        String remoteAddr = this.proxyConfig.getOriginalRemoteAddr((HttpServletRequest) getRequest());
        if ( !StringUtils.isBlank(remoteAddr) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Original remote address is " + remoteAddr); //$NON-NLS-1$
            }
            return remoteAddr;
        }
        return super.getRemoteAddr();
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.ServletRequestWrapper#getAttribute(java.lang.String)
     */
    @Override
    public Object getAttribute ( String name ) {
        if ( "proxied-via".equals(name) ) { //$NON-NLS-1$
            String via = this.proxyConfig.getProxiedVia((HttpServletRequest) getRequest());
            if ( log.isDebugEnabled() ) {
                log.debug("Intermediate proxies are " + via); //$NON-NLS-1$
            }
            return via;
        }

        if ( "javax.servlet.request.cipher_suite".equals(name) ) { //$NON-NLS-1$
            if ( !isSecure() || !this.proxyConfig.haveOriginalSSLCipherSpec() ) {
                return null;
            }
            Object cipher = super.getAttribute(CACHED_SSL_CIPHERS_SPECS);
            if ( cipher != null ) {
                return cipher;
            }
            cipher = this.proxyConfig.getOriginalSSLCipherSpec((HttpServletRequest) getRequest());
            super.setAttribute(CACHED_SSL_CIPHERS_SPECS, cipher);

            if ( log.isDebugEnabled() ) {
                log.debug("Original SSL ciphers are " + cipher); //$NON-NLS-1$
            }
            return cipher;
        }

        if ( "proxy-javax.servlet.request.cipher_suite".equals(name) ) { //$NON-NLS-1$
            return super.getAttribute("javax.servlet.request.cipher_suite"); //$NON-NLS-1$
        }

        return super.getAttribute(name);
    }
}
