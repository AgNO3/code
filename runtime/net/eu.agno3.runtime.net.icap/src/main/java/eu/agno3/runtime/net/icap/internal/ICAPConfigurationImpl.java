/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.06.2015 by mbechler
 */
package eu.agno3.runtime.net.icap.internal;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.Duration;

import eu.agno3.runtime.net.icap.ICAPConfiguration;
import eu.agno3.runtime.net.icap.ICAPException;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
public class ICAPConfigurationImpl implements ICAPConfiguration {

    private static final Logger log = Logger.getLogger(ICAPConfigurationImpl.class);

    private final Random rand = new Random();

    private Map<String, String> requestHeaders;
    private boolean sendICAPSinRequest;
    private URI overrideRequestURI;
    private boolean requireStartTLS;
    private boolean tryStartTLS;
    private Duration socketTimeout;
    private ArrayList<URI> serverUris;


    /**
     * 
     */
    private ICAPConfigurationImpl () {}


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPConfiguration#getSocketTimeout()
     */
    @Override
    public int getSocketTimeout () {
        return (int) this.socketTimeout.getMillis();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPConfiguration#isTryStartTLS()
     */
    @Override
    public boolean isTryStartTLS () {
        return this.tryStartTLS;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPConfiguration#isRequireStartTLS()
     */
    @Override
    public boolean isRequireStartTLS () {
        return this.requireStartTLS;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPConfiguration#selectServerUri()
     */
    @Override
    public URI selectServerUri () {
        return this.serverUris.get(this.rand.nextInt(this.serverUris.size()));
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPConfiguration#getOverrideRequestURI()
     */
    @Override
    public URI getOverrideRequestURI () {
        return this.overrideRequestURI;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPConfiguration#isSendICAPSInRequest()
     */
    @Override
    public boolean isSendICAPSInRequest () {
        return this.sendICAPSinRequest;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPConfiguration#getRequestHeaders()
     */
    @Override
    public Map<String, String> getRequestHeaders () {
        return this.requestHeaders;
    }


    /**
     * @param cfg
     * @return config from the given properties
     * @throws ICAPException
     */
    public static ICAPConfiguration fromProperties ( Dictionary<String, Object> cfg ) throws ICAPException {
        ICAPConfigurationImpl config = new ICAPConfigurationImpl();
        config.socketTimeout = ConfigUtil.parseDuration(cfg, "socketTimeout", Duration.standardSeconds(5)); //$NON-NLS-1$
        config.tryStartTLS = ConfigUtil.parseBoolean(cfg, "tryStartTLS", true); //$NON-NLS-1$
        config.requireStartTLS = ConfigUtil.parseBoolean(cfg, "requireStartTLS", false); //$NON-NLS-1$
        config.sendICAPSinRequest = ConfigUtil.parseBoolean(cfg, "sendICAPSinRequest", false); //$NON-NLS-1$
        String overrideReqURI = ConfigUtil.parseString(cfg, "overrideReqURI", null); //$NON-NLS-1$
        if ( !StringUtils.isBlank(overrideReqURI) ) {
            try {
                config.overrideRequestURI = new URI(overrideReqURI);
            }
            catch ( URISyntaxException e ) {
                throw new ICAPException("Failed to parse override URI" + overrideReqURI, e); //$NON-NLS-1$
            }
        }
        else {
            config.overrideRequestURI = null;
        }

        Collection<String> servers = ConfigUtil.parseStringCollection(cfg, "servers", Collections.EMPTY_LIST); //$NON-NLS-1$
        config.serverUris = new ArrayList<>();
        for ( String server : servers ) {
            try {
                config.serverUris.add(new URI(server));
            }
            catch ( URISyntaxException e ) {
                log.warn("Failed to parse ICAP URI " + server, e); //$NON-NLS-1$
            }
        }

        if ( config.serverUris.isEmpty() ) {
            throw new ICAPException("No servers configured"); //$NON-NLS-1$
        }

        return config;
    }

}
