/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 18, 2016 by mbechler
 */
package eu.agno3.runtime.security.web;


/**
 * @author mbechler
 *
 */
public interface SecurityHeadersFilterConfig {

    /**
     * 
     * @return whether to deny search engine indexing
     */
    public boolean isDenyIndex ();


    /**
     * 
     * @return CSP header value to use by default
     */
    public String getDefaultCSPHeader ();


    /**
     * @return whether to send a HSTS header
     */
    public boolean isHSTSEnabled ();


    /**
     * 
     * @return HSTS maxAge to send
     */
    public long getHSTSMaxAge ();


    /**
     * 
     * @return whether to send includeSubDomains
     */
    public boolean isHSTSIncludeSubdomains ();


    /**
     * @return whether to send the preload flag in HSTS
     */
    boolean isHSTSPreload ();
}
