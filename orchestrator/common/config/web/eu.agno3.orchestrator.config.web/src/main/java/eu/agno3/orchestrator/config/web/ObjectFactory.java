/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web;


/**
 * @author mbechler
 *
 */
public class ObjectFactory {

    /**
     * 
     * @return default impl
     */
    public WebEndpointConfig makeWebEndpointConfig () {
        return new WebEndpointConfigImpl();
    }


    /**
     * 
     * @return default impl
     */
    public SSLEndpointConfiguration makeSslEndpointConfig () {
        return new SSLEndpointConfigurationImpl();
    }


    /**
     * 
     * @return default impl
     */
    public SSLClientConfiguration makeSslClientConfig () {
        return new SSLClientConfigurationImpl();
    }


    /**
     * 
     * @return default impl
     */
    public ICAPConfiguration makeICAPConfig () {
        return new ICAPConfigurationImpl();
    }


    /**
     * @return default impl
     */
    public SMTPConfiguration makeSMTPConfig () {
        return new SMTPConfigurationImpl();
    }


    /**
     * @return default impl
     */
    public LDAPConfiguration makeLDAPConfig () {
        return new LDAPConfigurationImpl();
    }


    /**
     * 
     * @return default impl
     */
    public LDAPObjectConfig makeLDAPObjectConfig () {
        return new LDAPObjectConfigImpl();
    }


    /**
     * 
     * @return default impl
     */
    public LDAPObjectAttributeMapping makeLDAPObjectAttributeMapping () {
        return new LDAPObjectAttributeMappingImpl();
    }


    /**
     * 
     * @return default impl
     */
    public RuntimeConfiguration makeRuntimeConfiguration () {
        return new RuntimeConfigurationImpl();
    }


    /**
     * 
     * @return default impl
     */
    public WebReverseProxyConfiguration makeWebReverseProxyConfiguration () {
        return new WebReverseProxyConfigurationImpl();
    }
}
