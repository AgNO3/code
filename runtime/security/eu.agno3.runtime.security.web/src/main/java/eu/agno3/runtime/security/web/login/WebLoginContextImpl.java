/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2015 by mbechler
 */
package eu.agno3.runtime.security.web.login;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * @author mbechler
 *
 */
public class WebLoginContextImpl implements WebLoginContext {

    private String userAgent;
    private boolean secureTransport;
    private String remoteAddr;
    private String localHostname;
    private String localContext;
    private int localPort;
    private String localAddress;
    private boolean httpAuth;
    private Locale locale;
    private String authBase;


    /**
     * 
     * @param config
     * @param userAgent
     * @param remoteAddr
     * @param localHostname
     * @param localAddress
     * @param localContext
     * @param localPort
     * @param secureTransport
     */
    public WebLoginContextImpl ( WebLoginConfig config, String userAgent, String remoteAddr, String localHostname, String localAddress,
            String localContext, int localPort, boolean secureTransport ) {
        this(config, userAgent, remoteAddr, localHostname, localAddress, localContext, localPort, secureTransport, false);
    }


    /**
     * @param config
     * @param userAgent
     * @param remoteAddr
     * @param localHostname
     * @param localAddress
     * @param localContext
     * @param localPort
     * @param secureTransport
     * @param httpAuth
     */
    public WebLoginContextImpl ( WebLoginConfig config, String userAgent, String remoteAddr, String localHostname, String localAddress,
            String localContext, int localPort, boolean secureTransport, boolean httpAuth ) {
        this.authBase = config != null ? config.getAuthBasePath() : null;
        this.userAgent = userAgent;
        this.remoteAddr = remoteAddr;
        this.localHostname = localHostname;
        this.localAddress = localAddress;
        this.localContext = localContext;
        this.localPort = localPort;
        this.secureTransport = secureTransport;
        this.httpAuth = httpAuth;
    }


    /**
     * @return the authBase
     */
    @Override
    public String getAuthBase () {
        return this.authBase;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginContext#getProperties()
     */
    @Override
    public Map<? extends String, ? extends Serializable> getProperties () {
        Map<String, Serializable> res = new HashMap<>();
        res.put("userAgent", this.userAgent); //$NON-NLS-1$
        res.put("remoteAddr", this.remoteAddr); //$NON-NLS-1$
        res.put("localHostname", this.localHostname); //$NON-NLS-1$
        res.put("localAddress", this.localAddress); //$NON-NLS-1$
        res.put("localContext", this.localContext); //$NON-NLS-1$
        res.put("localPort", this.localPort); //$NON-NLS-1$
        res.put("secureTransport", this.secureTransport); //$NON-NLS-1$
        res.put("httpAuth", this.httpAuth); //$NON-NLS-1$
        return res;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.NetworkLoginContext#getRemoteAddress()
     */
    @Override
    public String getRemoteAddress () {
        return this.remoteAddr;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.login.WebLoginContext#getUserAgent()
     */
    @Override
    public String getUserAgent () {
        return this.userAgent;
    }


    /**
     * @return the localContext
     */
    @Override
    public String getLocalContextPath () {
        return this.localContext;
    }


    /**
     * @return the localPort
     */
    @Override
    public int getLocalPort () {
        return this.localPort;
    }


    /**
     * @return the localHostname
     */
    @Override
    public String getLocalHostname () {
        return this.localHostname;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.NetworkLoginContext#getLocalAddress()
     */
    @Override
    public String getLocalAddress () {
        return this.localAddress;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.NetworkLoginContext#isTransportSecure()
     */
    @Override
    public boolean isTransportSecure () {
        return this.secureTransport;
    }


    /**
     * @return the httpAuth
     */
    @Override
    public boolean isHttpAuth () {
        return this.httpAuth;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.login.WebLoginContext#getLocale()
     */
    @Override
    public Locale getLocale () {
        return this.locale != null ? this.locale : Locale.ROOT;
    }


    /**
     * @param locale
     *            the locale to set
     */
    public void setLocale ( Locale locale ) {
        this.locale = locale;
    }

    /**
     * 
     */
    private static final long serialVersionUID = 69809065529949570L;

}
