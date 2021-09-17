/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2017 by mbechler
 */
package eu.agno3.orchestrator.config.web.validation.icap.internal;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.web.SSLClientMode;
import eu.agno3.runtime.net.icap.ICAPConfiguration;


/**
 * @author mbechler
 *
 */
public class ICAPConfigurationAdapter implements ICAPConfiguration {

    private static final Logger log = Logger.getLogger(ICAPConfigurationAdapter.class);
    private eu.agno3.orchestrator.config.web.ICAPConfiguration cfg;
    private URI selectedServer;


    /**
     * @param config
     * @param selectedServer
     */
    public ICAPConfigurationAdapter ( eu.agno3.orchestrator.config.web.ICAPConfiguration config, URI selectedServer ) {
        this.cfg = config;
        this.selectedServer = selectedServer;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPConfiguration#getSocketTimeout()
     */
    @Override
    public int getSocketTimeout () {
        return (int) this.cfg.getSocketTimeout().getMillis();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPConfiguration#isTryStartTLS()
     */
    @Override
    public boolean isTryStartTLS () {
        return this.cfg.getSslClientMode() == SSLClientMode.TRY_STARTTLS;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPConfiguration#isRequireStartTLS()
     */
    @Override
    public boolean isRequireStartTLS () {
        return this.cfg.getSslClientMode() == SSLClientMode.REQUIRE_STARTTLS;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPConfiguration#selectServerUri()
     */
    @Override
    public URI selectServerUri () {
        return this.selectedServer;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPConfiguration#getOverrideRequestURI()
     */
    @Override
    public URI getOverrideRequestURI () {
        try {
            if ( !StringUtils.isBlank(this.cfg.getOverrideRequestURI()) ) {
                return new URI(this.cfg.getOverrideRequestURI());
            }
        }
        catch ( URISyntaxException e ) {
            log.debug("Failed to parse URI", e); //$NON-NLS-1$
        }
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPConfiguration#isSendICAPSInRequest()
     */
    @Override
    public boolean isSendICAPSInRequest () {
        return this.cfg.getSendICAPSInRequestUri();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPConfiguration#getRequestHeaders()
     */
    @Override
    public Map<String, String> getRequestHeaders () {
        return Collections.EMPTY_MAP;
    }

}
