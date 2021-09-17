/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web;


/**
 * @author mbechler
 *
 */
public enum SSLClientMode {

    /**
     * Don't use transport security
     */
    DISABLE,

    /**
     * Use SSL
     */
    SSL,

    /**
     * Use startTLS if available
     */
    TRY_STARTTLS,

    /**
     * Require startTLS to succeed
     */
    REQUIRE_STARTTLS;
}
